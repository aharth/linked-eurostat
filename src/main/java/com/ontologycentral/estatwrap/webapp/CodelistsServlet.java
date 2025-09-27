package com.ontologycentral.estatwrap.webapp;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

@SuppressWarnings("serial")
public class CodelistsServlet extends HttpServlet {
    Logger _log = Logger.getLogger(this.getClass().getName());

    private static final String CODELISTS_INVENTORY_URL = "https://ec.europa.eu/eurostat/api/dissemination/files/inventory?type=codelist";

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        // Handle appspot redirect
        if (req.getServerName().contains("estatwrap.appspot.com")) {
            try {
                URI re = new URI("http://estatwrap.ontologycentral.com/" + req.getRequestURI());
                re = re.normalize();
                resp.sendRedirect(re.toString());
            } catch (URISyntaxException e) {
                resp.sendError(500, e.getMessage());
            }
            return;
        }

        // Determine format from request path
        String requestURI = req.getRequestURI();
        String format;
        String contentType;

        if (requestURI.endsWith(".html")) {
            format = "html";
            contentType = "text/html;charset=UTF-8";
        } else if (requestURI.endsWith(".rdf")) {
            format = "rdf";
            contentType = "application/rdf+xml;charset=UTF-8";
        } else {
            resp.sendError(404, "Format not supported");
            return;
        }

        resp.setContentType(contentType);
        resp.setHeader("Cache-Control", "public,max-age=3600"); // Cache for 1 hour

        try {
            // Fetch CSV from Eurostat API
            URL inventoryUrl = new URL(CODELISTS_INVENTORY_URL);
            HttpURLConnection conn = (HttpURLConnection) inventoryUrl.openConnection();
            conn.setRequestProperty("User-Agent", "LinkedEurostat/1.0");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                _log.log(Level.SEVERE, "Failed to fetch codelists inventory: HTTP {0}", responseCode);
                resp.sendError(502, "Failed to fetch codelists inventory from Eurostat API");
                return;
            }

            InputStream csvStream = conn.getInputStream();

            // Convert CSV to XML for XSLT processing
            String xmlContent = convertCsvToXml(csvStream);
            csvStream.close();

            // Transform using appropriate XSLT
            TransformerFactory tf = TransformerFactory.newInstance(
                "net.sf.saxon.TransformerFactoryImpl",
                Thread.currentThread().getContextClassLoader());

            InputStream xslStream = getServletContext().getResourceAsStream("/WEB-INF/codelists-" + format + ".xsl");
            if (xslStream == null) {
                _log.log(Level.SEVERE, "XSLT stylesheet not found: /WEB-INF/codelists-{0}.xsl", format);
                resp.sendError(500, "XSLT stylesheet not found");
                return;
            }

            Transformer transformer = tf.newTransformer(new StreamSource(xslStream));

            PrintWriter writer = resp.getWriter();
            StreamSource source = new StreamSource(new StringReader(xmlContent));
            StreamResult result = new StreamResult(writer);

            _log.log(Level.INFO, "Generating codelists catalog in {0} format", format);
            transformer.transform(source, result);

            xslStream.close();
            writer.flush();

        } catch (TransformerException e) {
            _log.log(Level.SEVERE, "XSLT transformation failed", e);
            resp.sendError(500, "Failed to transform codelists catalog: " + e.getMessage());
        } catch (IOException e) {
            _log.log(Level.SEVERE, "I/O error while generating codelists catalog", e);
            resp.sendError(502, "Error accessing Eurostat API: " + e.getMessage());
        }
    }

    private String convertCsvToXml(InputStream csvStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(csvStream, "UTF-8"));

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<codelists>\n");

        String headerLine = reader.readLine();
        if (headerLine == null) {
            throw new IOException("Empty CSV file");
        }

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            String[] fields = line.split("\t", -1);
            if (fields.length >= 4) {
                xml.append("  <codelist>\n");
                xml.append("    <code>").append(escapeXml(fields[0])).append("</code>\n");
                xml.append("    <source>").append(escapeXml(fields[1])).append("</source>\n");
                xml.append("    <version>").append(escapeXml(fields[2])).append("</version>\n");
                xml.append("    <label>").append(escapeXml(fields[3])).append("</label>\n");
                if (fields.length > 4 && !fields[4].trim().isEmpty()) {
                    xml.append("    <tsv_url>").append(escapeXml(fields[4])).append("</tsv_url>\n");
                }
                if (fields.length > 5 && !fields[5].trim().isEmpty()) {
                    xml.append("    <sdmx_url>").append(escapeXml(fields[5])).append("</sdmx_url>\n");
                }
                if (fields.length > 6 && !fields[6].trim().isEmpty()) {
                    xml.append("    <latest_tsv_url>").append(escapeXml(fields[6])).append("</latest_tsv_url>\n");
                }
                if (fields.length > 7 && !fields[7].trim().isEmpty()) {
                    xml.append("    <latest_sdmx_url>").append(escapeXml(fields[7])).append("</latest_sdmx_url>\n");
                }
                if (fields.length > 8 && !fields[8].trim().isEmpty()) {
                    xml.append("    <mapping_file>").append(escapeXml(fields[8])).append("</mapping_file>\n");
                }
                xml.append("  </codelist>\n");
            }
        }

        xml.append("</codelists>\n");
        return xml.toString();
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }
}