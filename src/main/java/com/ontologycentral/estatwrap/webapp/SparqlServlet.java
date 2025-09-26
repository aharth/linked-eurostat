package com.ontologycentral.estatwrap.webapp;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.logging.Logger;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;

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

            _log.info("Executing SPARQL query: " + queryString);

            // Parse the SPARQL query
            Query query = QueryFactory.create(queryString);

            // Create a dataset to hold the RDF data
            Dataset dataset = DatasetFactory.create();

            // Load data from FROM clauses
            for (String fromUri : query.getGraphURIs()) {
                _log.info("Loading graph from URI: " + fromUri);
                try {
                    // Convert relative URIs to absolute URIs based on the request
                    String absoluteUri = resolveUri(fromUri, req);
                    _log.info("Resolved URI: " + absoluteUri);

                    Model model = ModelFactory.createDefaultModel();
                    RDFDataMgr.read(model, absoluteUri);
                    dataset.getDefaultModel().add(model);
                } catch (Exception e) {
                    _log.warning("Failed to load graph from " + fromUri + ": " + e.getMessage());
                    // Continue with other graphs even if one fails
                }
            }

            // Load data from FROM NAMED clauses
            for (String namedGraphUri : query.getNamedGraphURIs()) {
                _log.info("Loading named graph from URI: " + namedGraphUri);
                try {
                    String absoluteUri = resolveUri(namedGraphUri, req);
                    _log.info("Resolved named graph URI: " + absoluteUri);

                    Model model = ModelFactory.createDefaultModel();
                    RDFDataMgr.read(model, absoluteUri);
                    dataset.addNamedModel(namedGraphUri, model);
                } catch (Exception e) {
                    _log.warning("Failed to load named graph from " + namedGraphUri + ": " + e.getMessage());
                    // Continue with other graphs even if one fails
                }
            }

            // Execute the query
            try (QueryExecution qexec = QueryExecutionFactory.create(query, dataset)) {
                // Determine output format
                String acceptHeader = req.getHeader("Accept");
                String format = getOutputFormat(acceptHeader, req.getParameter("format"));

                resp.setCharacterEncoding("UTF-8");

                if (query.isSelectType()) {
                    ResultSet results = qexec.execSelect();

                    if ("json".equals(format)) {
                        resp.setContentType("application/sparql-results+json");
                        ResultSetFormatter.outputAsJSON(resp.getOutputStream(), results);
                    } else if ("xml".equals(format)) {
                        resp.setContentType("application/sparql-results+xml");
                        ResultSetFormatter.outputAsXML(resp.getOutputStream(), results);
                    } else {
                        // Default to TSV
                        resp.setContentType("text/tab-separated-values");
                        ResultSetFormatter.outputAsTSV(resp.getOutputStream(), results);
                    }
                } else if (query.isConstructType()) {
                    Model result = qexec.execConstruct();
                    resp.setContentType("text/turtle");
                    result.write(resp.getOutputStream(), "TTL");
                } else if (query.isDescribeType()) {
                    Model result = qexec.execDescribe();
                    resp.setContentType("text/turtle");
                    result.write(resp.getOutputStream(), "TTL");
                } else if (query.isAskType()) {
                    boolean result = qexec.execAsk();
                    resp.setContentType("application/sparql-results+json");
                    PrintWriter out = resp.getWriter();
                    out.println("{\"boolean\": " + result + "}");
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