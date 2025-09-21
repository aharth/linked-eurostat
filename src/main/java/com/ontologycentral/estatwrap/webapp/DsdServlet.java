package com.ontologycentral.estatwrap.webapp;

import com.ontologycentral.estatwrap.convert.Dsd;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

@SuppressWarnings("serial")
public class DsdServlet extends HttpServlet {
    Logger _log = Logger.getLogger(this.getClass().getName());

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        OutputStream os = resp.getOutputStream();
        // OutputStreamWriter osw = new OutputStreamWriter(os , "UTF-8");

        String id = req.getRequestURI();
        int dsdIndex = id.indexOf("/dsd/");
        if (dsdIndex == -1) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        id = id.substring(dsdIndex + "/dsd/".length());

        ServletContext ctx = getServletContext();
        Transformer t = (Transformer) ctx.getAttribute(Listener.SDMX_T);

        resp.setContentType("application/rdf+xml");
        // 1 day
        resp.setHeader("Cache-Control", "max-age=86400");

        try {
            Dsd dsd = new Dsd(id);
            dsd.convert(os, t);
        } catch (TransformerException e) {
            e.printStackTrace();
            resp.sendError(500, e.getMessage());
            return;
        } catch (IOException e) {
            resp.sendError(500, "Error processing DSD for " + id + ": " + e.getMessage());
            e.printStackTrace();
            return;
        } catch (RuntimeException e) {
            resp.sendError(500, "Error processing DSD for " + id + ": " + e.getMessage());
            e.printStackTrace();
            return;
        }

        os.close();
    }
}
