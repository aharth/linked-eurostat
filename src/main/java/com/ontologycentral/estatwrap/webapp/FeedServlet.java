package com.ontologycentral.estatwrap.webapp;

import jakarta.servlet.ServletContext;
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
import java.util.logging.Logger;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

@SuppressWarnings("serial")
public class FeedServlet extends HttpServlet {
    Logger _log = Logger.getLogger(this.getClass().getName());


    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

        resp.setContentType("application/rdf+xml");
        PrintWriter pw = resp.getWriter();


        // Use official Eurostat RSS feed for dataset updates
        URL url = new URL("https://ec.europa.eu/eurostat/api/dissemination/catalogue/rss/en/statistics-update.rss");

        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "LinkedEurostat/1.0");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                resp.sendError(responseCode);
                return;
            }

            InputStream is = conn.getInputStream();

            String encoding = conn.getContentEncoding();
            if (encoding == null) {
                encoding = Listener.DEFAULT_ENCODING;
            }

            // 10 min
            resp.setHeader("Cache-Control", "public,max-age=600");

            // Transform RSS feed to RDF using XSLT
            try {
                TransformerFactory tf = TransformerFactory.newInstance(
                    "net.sf.saxon.TransformerFactoryImpl",
                    Thread.currentThread().getContextClassLoader());

                InputStream xslStream = getServletContext().getResourceAsStream("/WEB-INF/feed-rss2rdf.xsl");
                if (xslStream == null) {
                    _log.severe("XSLT stylesheet not found: /WEB-INF/feed-rss2rdf.xsl");
                    resp.sendError(500, "XSLT stylesheet not found");
                    return;
                }

                Transformer transformer = tf.newTransformer(new StreamSource(xslStream));

                PrintWriter writer = resp.getWriter();
                StreamSource source = new StreamSource(is);
                StreamResult result = new StreamResult(writer);

                transformer.transform(source, result);

                xslStream.close();
                writer.flush();

            } catch (TransformerException e) {
                _log.severe("XSLT transformation failed: " + e.getMessage());
                resp.sendError(500, "Failed to transform RSS feed: " + e.getMessage());
            }


        } catch (IOException e) {
            resp.sendError(500, url + ": " + e.getMessage());
            e.printStackTrace();
            return;
        } catch (RuntimeException e) {
            resp.sendError(500, url + ": " + e.getMessage());
            e.printStackTrace();
            return;
        }
    }
}

