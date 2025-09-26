package com.ontologycentral.estatwrap.webapp;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
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
public class TocServlet extends HttpServlet {
    Logger _log = Logger.getLogger(this.getClass().getName());

    private static final String TOC_XML_URL = "https://ec.europa.eu/eurostat/api/dissemination/catalogue/toc/xml";

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
            // Fetch XML from Eurostat API
            URL tocUrl = new URL(TOC_XML_URL);
            HttpURLConnection conn = (HttpURLConnection) tocUrl.openConnection();
            conn.setRequestProperty("User-Agent", "LinkedEurostat/1.0");

            if (conn.getResponseCode() != 200) {
                _log.log(Level.SEVERE, "Failed to fetch TOC XML: HTTP {0}", conn.getResponseCode());
                resp.sendError(502, "Failed to fetch table of contents from Eurostat API");
                return;
            }

            InputStream xmlStream = conn.getInputStream();

            // Transform using appropriate XSLT
            TransformerFactory tf = TransformerFactory.newInstance(
                "net.sf.saxon.TransformerFactoryImpl",
                Thread.currentThread().getContextClassLoader());

            InputStream xslStream = getServletContext().getResourceAsStream("/WEB-INF/toc-" + format + ".xsl");
            if (xslStream == null) {
                _log.log(Level.SEVERE, "XSLT stylesheet not found: /WEB-INF/toc-{0}.xsl", format);
                resp.sendError(500, "XSLT stylesheet not found");
                return;
            }

            Transformer transformer = tf.newTransformer(new StreamSource(xslStream));

            PrintWriter writer = resp.getWriter();
            StreamSource source = new StreamSource(xmlStream);
            StreamResult result = new StreamResult(writer);

            _log.log(Level.INFO, "Generating table of contents in {0} format", format);
            transformer.transform(source, result);

            xmlStream.close();
            xslStream.close();
            writer.flush();

        } catch (TransformerException e) {
            _log.log(Level.SEVERE, "XSLT transformation failed", e);
            resp.sendError(500, "Failed to transform table of contents: " + e.getMessage());
        } catch (IOException e) {
            _log.log(Level.SEVERE, "I/O error while generating table of contents", e);
            resp.sendError(502, "Error accessing Eurostat API: " + e.getMessage());
        }
    }
}