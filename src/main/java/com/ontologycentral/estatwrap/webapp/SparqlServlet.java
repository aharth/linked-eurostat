package com.ontologycentral.estatwrap.webapp;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.logging.Logger;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.util.DatasetUtils;
import org.apache.jena.query.DatasetFactory;

@SuppressWarnings("serial")
public class SparqlServlet extends HttpServlet {
    Logger _log = Logger.getLogger(this.getClass().getName());

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

            Dataset dataset;
            if (!defaultGraphList.isEmpty() || !namedGraphList.isEmpty()) {
                dataset = DatasetUtils.createDataset(defaultGraphList, namedGraphList);
            } else {
                dataset = DatasetFactory.create();
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

        } catch (Exception e) {
            _log.severe("Error executing SPARQL query: " + e.getMessage());
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error executing query: " + e.getMessage());
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
        out.println("    ?s qb:dataSet <id/tag00038#ds> .");
        out.println("    ?s dim:dim-TIME_PERIOD ?time .");
        out.println("    ?s dim:dim-geo ?geo .");
        out.println("    ?s <ds/tag00038#measure-OBS_VALUE> ?value .");
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