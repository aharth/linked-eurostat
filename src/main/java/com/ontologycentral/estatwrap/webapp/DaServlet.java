package com.ontologycentral.estatwrap.webapp;

import com.ontologycentral.estatwrap.Main;
import com.ontologycentral.estatwrap.convert.DataSdmx3Turtle;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

@SuppressWarnings("serial")
public class DaServlet extends HttpServlet {
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

        resp.setContentType("text/turtle");

        OutputStream os = resp.getOutputStream();

        String id = req.getRequestURI();
        int daIndex = id.indexOf("/da/");
        if (daIndex == -1) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        id = id.substring(daIndex + "/da/".length());

        ServletContext ctx = getServletContext();

        // Use SDMX 3.0 API for data observations with format=tsv&compress=false
        URL url = new URL(Main.URI_PREFIX_3 + "/data/dataflow/ESTAT/" + id + "/1.0?format=tsv&compress=false");

        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(55 * 1000);
            conn.setReadTimeout(55 * 1000);
            conn.setUseCaches(true);
            conn.setRequestProperty("User-Agent", "estatwrap.ontologycentral.com");

            InputStream is;
            // Check if response is compressed despite compress=false parameter
            String contentEncoding = conn.getHeaderField("Content-Encoding");
            if ("gzip".equals(contentEncoding)) {
                is = new GZIPInputStream(conn.getInputStream());
            } else {
                is = conn.getInputStream();
            }

            if (conn.getResponseCode() != 200) {
                resp.sendError(conn.getResponseCode());
                return;
            }

            String encoding = conn.getContentEncoding();
            if (encoding == null) {
                encoding = Listener.DEFAULT_ENCODING;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(is, encoding));

            // 10 minutes cache
            resp.setHeader("Cache-Control", "max-age=600");

            Map<String, String> toc = (Map<String, String>) ctx.getAttribute(Listener.TOC);

            // Convert SDMX 3.0 TSV data to Turtle using SDMX 3.0 identifiers
            DataSdmx3Turtle.convertWithSdmx3IdentifiersToTurtle(os, toc, id, in);
        } catch (IOException e) {
            resp.sendError(500, url + ": " + e.getMessage());
            e.printStackTrace();
            return;
        } catch (RuntimeException e) {
            resp.sendError(500, url + ": " + e.getMessage());
            e.printStackTrace();
            return;
        }

        os.close();
    }
}