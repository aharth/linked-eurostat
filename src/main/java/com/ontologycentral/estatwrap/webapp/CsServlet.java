package com.ontologycentral.estatwrap.webapp;

import com.ontologycentral.estatwrap.Main;
import com.ontologycentral.estatwrap.convert.Cs;
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
import java.net.URL;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

@SuppressWarnings("serial")
public class CsServlet extends HttpServlet {
    Logger _log = Logger.getLogger(this.getClass().getName());

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        OutputStream os = resp.getOutputStream();

        String id = req.getRequestURI();
        int csIndex = id.indexOf("/cs/");
        if (csIndex == -1) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        id = id.substring(csIndex + "/cs/".length());

        URL url = new URL(Main.URI_PREFIX_3 + "/structure/conceptscheme/ESTAT/" + id);

        ServletContext ctx = getServletContext();
        Transformer t = (Transformer) ctx.getAttribute(Listener.CS_T);

        resp.setContentType("application/rdf+xml");
        // 1 day
        resp.setHeader("Cache-Control", "max-age=86400");

        try {
            Cs cs = new Cs();

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(55 * 1000);
            conn.setReadTimeout(55 * 1000);
            conn.setUseCaches(true);

            conn.setRequestProperty("User-Agent", "estatwrap.ontologycentral.com");

            InputStream is = null;
            if (url.toString().contains("compressed=true") || url.toString().contains("compress=true")) {
                is = new GZIPInputStream(conn.getInputStream());
            } else {
                is = conn.getInputStream();
            }

            if (conn.getResponseCode() != 200) {
                resp.sendError(conn.getResponseCode());
            }

            String encoding = "UTF-8"; // Default to UTF-8 for SDMX API responses
            String contentType = conn.getContentType();
            if (contentType != null && contentType.contains("charset=")) {
                encoding = contentType.substring(contentType.indexOf("charset=") + 8);
                if (encoding.contains(";")) {
                    encoding = encoding.substring(0, encoding.indexOf(";"));
                }
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(is, encoding));

            cs.convert(in, os, t);
        } catch (TransformerException e) {
            e.printStackTrace();
            resp.sendError(500, e.getMessage());
            return;
        } catch (IOException e) {
            resp.sendError(500, "Error processing concept scheme for " + id + ": " + e.getMessage());
            e.printStackTrace();
            return;
        } catch (RuntimeException e) {
            resp.sendError(500, "Error processing concept scheme for " + id + ": " + e.getMessage());
            e.printStackTrace();
            return;
        }

        os.close();
    }
}