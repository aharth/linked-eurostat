package com.ontologycentral.estatwrap.webapp;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class IdentifierServlet extends HttpServlet {
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

        resp.setContentType("text/html");

        OutputStream os = resp.getOutputStream();
        // OutputStreamWriter osw = new OutputStreamWriter(os , "UTF-8");

        String id = req.getRequestURI();
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        int idIndex = id.indexOf("/id/");
        if (idIndex == -1) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        id = id.substring(idIndex + "/id/".length());

        //		ServletContext ctx = getServletContext();

        String accept = req.getHeader("accept");

        // EXPENSIVE!
        //		Map<String, String> toc = (Map<String, String>)ctx.getAttribute(Listener.TOC);
        //		if (toc != null && !toc.containsKey(id)) {
        //			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        //			return;
        //		}

        if (accept != null && accept.contains("application/rdf+xml")) {
            // out.println(path + ".rdf");
            resp.setStatus(HttpServletResponse.SC_SEE_OTHER);
            resp.setHeader("Location", "../da/" + id);
            return;
        } else {
            // out.println(path + ".html");
            resp.setStatus(HttpServletResponse.SC_SEE_OTHER);
            resp.setHeader("Location", "../page/" + id);
            return;
        }
    }
}
