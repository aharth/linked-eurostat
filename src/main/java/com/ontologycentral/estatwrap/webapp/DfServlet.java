package com.ontologycentral.estatwrap.webapp;

import com.ontologycentral.estatwrap.HttpClientUtil;
import com.ontologycentral.estatwrap.UrlBuilder;
import com.ontologycentral.estatwrap.convert.Df;
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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

@SuppressWarnings("serial")
public class DfServlet extends HttpServlet {
    Logger _log = Logger.getLogger(this.getClass().getName());

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        OutputStream os = resp.getOutputStream();

        String id = req.getRequestURI();
        int dfIndex = id.indexOf("/df/");
        if (dfIndex == -1) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        id = id.substring(dfIndex + "/df/".length());

        String urlString = UrlBuilder.buildDataflowUrl(id);
        URL url = new URL(urlString);

        ServletContext ctx = getServletContext();
        Transformer t = (Transformer) ctx.getAttribute(Listener.DF_T);

        resp.setContentType("application/rdf+xml");
        // 1 day
        resp.setHeader("Cache-Control", "max-age=86400");

        try {
            Df df = new Df();

            HttpURLConnection conn = HttpClientUtil.createConnection(urlString);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                resp.sendError(responseCode);
                return;
            }

            InputStream is = HttpClientUtil.getInputStream(conn);
            String encoding = HttpClientUtil.getEncoding(conn, "UTF-8");

            BufferedReader in = new BufferedReader(new InputStreamReader(is, encoding));

            df.convert(in, os, t);
        } catch (TransformerException e) {
            e.printStackTrace();
            resp.sendError(500, e.getMessage());
            return;
        } catch (IOException e) {
            resp.sendError(500, "Error processing dataflow for " + id + ": " + e.getMessage());
            e.printStackTrace();
            return;
        } catch (RuntimeException e) {
            resp.sendError(500, "Error processing dataflow for " + id + ": " + e.getMessage());
            e.printStackTrace();
            return;
        }

        os.close();
    }
}