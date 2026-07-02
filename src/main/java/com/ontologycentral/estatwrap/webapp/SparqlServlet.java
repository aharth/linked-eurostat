package com.ontologycentral.estatwrap.webapp;

import com.ontologycentral.estatwrap.HttpClientUtil;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.query.DatasetFactory;

@SuppressWarnings("serial")
public class SparqlServlet extends HttpServlet {
    Logger _log = Logger.getLogger(this.getClass().getName());

    // Queries load entire FROM graphs into heap; one at a time keeps memory bounded.
    private static final Semaphore QUERY_SLOT = new Semaphore(1);

    // Per-graph cap; enforced via Content-Length when present and while streaming otherwise.
    static final long MAX_GRAPH_BYTES = 10L * 1024 * 1024;

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleSparqlRequest(req, resp);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleSparqlRequest(req, resp);
    }

    private void handleSparqlRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // Get the query parameter
            String queryString = req.getParameter("query");
            if (queryString == null || queryString.trim().isEmpty()) {
                // If no query parameter, show HTML form for GET requests
                if ("GET".equals(req.getMethod())) {
                    showSparqlForm(req, resp);
                    return;
                } else {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing 'query' parameter");
                    return;
                }
            }

            // URL decode the query if needed
            queryString = URLDecoder.decode(queryString, "UTF-8");

            if (!QUERY_SLOT.tryAcquire()) {
                resp.setHeader("Retry-After", "10");
                resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Another query is currently executing, please retry shortly");
                return;
            }
            try {
                executeQuery(queryString, req, resp);
            } finally {
                QUERY_SLOT.release();
            }

        } catch (Exception e) {
            GraphTooLargeException tooLarge = findGraphTooLarge(e);
            if (tooLarge != null) {
                _log.warning(tooLarge.getMessage());
                resp.sendError(413, tooLarge.getMessage());
                return;
            }
            _log.severe("Error executing SPARQL query: " + e.getMessage());
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error executing query: " + e.getMessage());
        }
    }

    /** Jena may wrap stream errors (e.g. in RiotException); walk the cause chain. */
    private static GraphTooLargeException findGraphTooLarge(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof GraphTooLargeException) {
                return (GraphTooLargeException) t;
            }
        }
        return null;
    }

    private void executeQuery(String queryString, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Parse the SPARQL query
        Query query = QueryFactory.create(queryString);

        // Resolve relative URIs in the query string itself
        String resolvedQueryString = queryString;
        for (String fromUri : query.getGraphURIs()) {
            String absoluteUri = resolveUri(fromUri, req);
            resolvedQueryString = resolvedQueryString.replace("<" + fromUri + ">", "<" + absoluteUri + ">");
        }
        for (String namedGraphUri : query.getNamedGraphURIs()) {
            String absoluteUri = resolveUri(namedGraphUri, req);
            resolvedQueryString = resolvedQueryString.replace("<" + namedGraphUri + ">", "<" + absoluteUri + ">");
        }

        // Reparse the query with resolved URIs
        query = QueryFactory.create(resolvedQueryString);

        // Go back to DatasetUtils approach since it loads data correctly
        java.util.List<String> defaultGraphList = new java.util.ArrayList<>();
        java.util.List<String> namedGraphList = new java.util.ArrayList<>();

        // Convert relative URIs to absolute URIs for FROM clauses
        for (String fromUri : query.getGraphURIs()) {
            String absoluteUri = resolveUri(fromUri, req);
            defaultGraphList.add(absoluteUri);
        }

        // Convert relative URIs to absolute URIs for FROM NAMED clauses
        for (String namedGraphUri : query.getNamedGraphURIs()) {
            String absoluteUri = resolveUri(namedGraphUri, req);
            namedGraphList.add(absoluteUri);
        }

        // Load graphs ourselves (instead of DatasetUtils) so each fetch is size-capped.
        Dataset dataset = DatasetFactory.create();
        for (String uri : defaultGraphList) {
            dataset.getDefaultModel().add(loadGraphCapped(uri));
        }
        for (String uri : namedGraphList) {
            dataset.addNamedModel(uri, loadGraphCapped(uri));
        }


        // Remove FROM clauses from query since we pre-loaded the data into the dataset
        String queryStringForExecution = resolvedQueryString;
        if (!query.getGraphURIs().isEmpty()) {
            // Remove all FROM clauses since data is pre-loaded
            queryStringForExecution = queryStringForExecution.replaceAll("FROM\\s+<[^>]+>", "");
        }
        Query queryForExecution = QueryFactory.create(queryStringForExecution);

        try (QueryExecution qexec = QueryExecutionFactory.create(queryForExecution, dataset)) {
            // Determine output format
            String acceptHeader = req.getHeader("Accept");
            String format = getOutputFormat(acceptHeader, req.getParameter("format"));

            resp.setCharacterEncoding("UTF-8");

            if (queryForExecution.isSelectType()) {
                ResultSet results = qexec.execSelect();

                java.io.OutputStream outputStream = resp.getOutputStream();
                if ("json".equals(format)) {
                    resp.setContentType("application/sparql-results+json");
                    ResultSetFormatter.outputAsJSON(outputStream, results);
                } else if ("xml".equals(format)) {
                    resp.setContentType("application/sparql-results+xml");
                    ResultSetFormatter.outputAsXML(outputStream, results);
                } else {
                    // Default to TSV
                    resp.setContentType("text/tab-separated-values");
                    ResultSetFormatter.outputAsTSV(outputStream, results);
                }
                outputStream.flush();
            } else if (queryForExecution.isConstructType()) {
                Model result = qexec.execConstruct();
                resp.setContentType("text/turtle");
                java.io.OutputStream outputStream = resp.getOutputStream();
                result.write(outputStream, "TTL");
                outputStream.flush();
            } else if (queryForExecution.isDescribeType()) {
                Model result = qexec.execDescribe();
                resp.setContentType("text/turtle");
                java.io.OutputStream outputStream = resp.getOutputStream();
                result.write(outputStream, "TTL");
                outputStream.flush();
            } else if (queryForExecution.isAskType()) {
                boolean result = qexec.execAsk();
                resp.setContentType("application/sparql-results+json");
                PrintWriter out = resp.getWriter();
                out.println("{\"boolean\": " + result + "}");
                out.flush();
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported query type");
            }
        }
    }

    /**
     * Fetches a FROM graph and parses it into a Model, enforcing MAX_GRAPH_BYTES.
     * Rejects via Content-Length when the server provides one, and by counting
     * bytes while streaming otherwise (chunked responses).
     */
    private Model loadGraphCapped(String uri) throws IOException {
        HttpURLConnection conn = HttpClientUtil.createConnection(uri);
        conn.setRequestProperty("Accept",
                "application/rdf+xml, text/turtle;q=0.9, application/n-triples;q=0.8");
        HttpClientUtil.checkResponseCode(conn, uri);

        long contentLength = conn.getContentLengthLong();
        if (contentLength > MAX_GRAPH_BYTES) {
            throw new GraphTooLargeException("Graph <" + uri + "> is too large to query: "
                    + contentLength + " bytes (Content-Length) exceeds the limit of "
                    + MAX_GRAPH_BYTES + " bytes (10 MB). Query a smaller dataset or slice.");
        }

        String contentType = conn.getContentType();
        if (contentType != null && contentType.indexOf(';') >= 0) {
            contentType = contentType.substring(0, contentType.indexOf(';')).trim();
        }
        Lang lang = contentType == null ? null : RDFLanguages.contentTypeToLang(contentType);

        Model model = ModelFactory.createDefaultModel();
        try (InputStream is = new CappedInputStream(HttpClientUtil.getInputStream(conn), MAX_GRAPH_BYTES, uri)) {
            RDFParser.create()
                    .source(is)
                    .lang(lang != null ? lang : Lang.RDFXML)
                    .base(uri)
                    .parse(model);
        }
        return model;
    }

    /** A FROM graph exceeded MAX_GRAPH_BYTES. */
    static class GraphTooLargeException extends IOException {
        GraphTooLargeException(String message) {
            super(message);
        }
    }

    /** Throws GraphTooLargeException once more than {@code limit} bytes have been read. */
    static class CappedInputStream extends FilterInputStream {
        private final long limit;
        private final String uri;
        private long count;

        CappedInputStream(InputStream in, long limit, String uri) {
            super(in);
            this.limit = limit;
            this.uri = uri;
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            if (b >= 0) {
                bump(1);
            }
            return b;
        }

        @Override
        public int read(byte[] buf, int off, int len) throws IOException {
            int n = super.read(buf, off, len);
            if (n > 0) {
                bump(n);
            }
            return n;
        }

        private void bump(int n) throws IOException {
            count += n;
            if (count > limit) {
                throw new GraphTooLargeException("Graph <" + uri + "> is too large to query: "
                        + "response exceeded the limit of " + limit
                        + " bytes (10 MB) while streaming. Query a smaller dataset or slice.");
            }
        }
    }

    private String resolveUri(String uri, HttpServletRequest req) {
        if (uri.startsWith("http://") || uri.startsWith("https://")) {
            return uri;
        }

        // Handle relative URIs - resolve against the request base
        String scheme = req.getScheme();
        String serverName = req.getServerName();
        int serverPort = req.getServerPort();
        String contextPath = req.getContextPath();

        String baseUrl = scheme + "://" + serverName;
        if ((scheme.equals("http") && serverPort != 80) ||
            (scheme.equals("https") && serverPort != 443)) {
            baseUrl += ":" + serverPort;
        }
        baseUrl += contextPath + "/";

        return baseUrl + uri;
    }

    private String getOutputFormat(String acceptHeader, String formatParam) {
        // Format parameter takes precedence
        if (formatParam != null) {
            return formatParam.toLowerCase();
        }

        // Check Accept header
        if (acceptHeader != null) {
            acceptHeader = acceptHeader.toLowerCase();
            if (acceptHeader.contains("application/sparql-results+json")) {
                return "json";
            } else if (acceptHeader.contains("application/sparql-results+xml")) {
                return "xml";
            } else if (acceptHeader.contains("text/tab-separated-values")) {
                return "tsv";
            }
        }

        // Default format
        return "json";
    }

    private void showSparqlForm(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter out = resp.getWriter();

        out.println("<html>");
        out.println("<head>");
        out.println("    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">");
        out.println("    <title>SPARQL</title>");
        out.println("</head>");
        out.println("<body>");

        out.println("    <p>");
        out.println("      <a href=\"/\">Eurostat Wrapper Home</a>");
        out.println("    </p>");

        out.println("    <h1>SPARQL</h1>");

        out.println("    <p>");
        out.println("      Execute SPARQL queries against the Linked Eurostat dataset.");
        out.println("    </p>");

        out.println("    <form method=\"GET\" action=\"sparql\">");
        out.println("      <div>");
        out.println("      <textarea cols=\"120\" rows=\"25\" name=\"query\">BASE <https://estatwrap.ontologycentral.com/>");
        out.println("PREFIX dim: <ds/tag00038#>");
        out.println("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
        out.println("PREFIX qb: <http://purl.org/linked-data/cube#>");
        out.println("");
        out.println("SELECT ?time ?value ?geo");
        out.println("FROM <da/tag00038>");
        out.println("WHERE {");
        out.println("    ?obs qb:dataSet <da/tag00038#ds> .");
        out.println("    ?obs dim:dim-TIME_PERIOD ?time .");
        out.println("    ?obs dim:dim-geo ?geo .");
        out.println("    ?obs <ds/tag00038#measure-OBS_VALUE> ?value .");
        out.println("}");
        out.println("LIMIT 10</textarea>");
        out.println("      </div>");
        out.println("      <div>");
        out.println("      <input type=\"radio\" name=\"format\" value=\"tsv\"> TSV");
        out.println("      <input type=\"radio\" name=\"format\" value=\"json\" checked> JSON");
        out.println("      <input type=\"radio\" name=\"format\" value=\"xml\"> XML");
        out.println("      </div>");
        out.println("      <div>");
        out.println("      <input type=\"reset\"/> <input type=\"submit\"/>");
        out.println("      </div>");
        out.println("    </form>");

        out.println("</body>");
        out.println("</html>");

        out.close();
    }
}