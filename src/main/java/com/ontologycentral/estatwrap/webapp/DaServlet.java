package com.ontologycentral.estatwrap.webapp;

import com.ontologycentral.estatwrap.DataFilter;
import com.ontologycentral.estatwrap.HttpClientUtil;
import com.ontologycentral.estatwrap.UrlBuilder;
import com.ontologycentral.estatwrap.convert.Da;
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
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;
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
                ServletUtil.sendError(resp, 500, e.getMessage());
            }
            return;
        }

        resp.setContentType("text/turtle");

        OutputStream os = resp.getOutputStream();

        String id = req.getRequestURI();
        int daIndex = id.indexOf("/da/");
        if (daIndex == -1) {
            ServletUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        id = id.substring(daIndex + "/da/".length());

        ServletContext ctx = getServletContext();

        DataFilter filter;
        try {
            filter = DataFilter.parse(req.getParameterMap());
        } catch (IllegalArgumentException e) {
            ServletUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        // Use SDMX 3.0 API for data observations with format=tsv&compress=false;
        // dimension filters are applied by Eurostat
        String urlString = UrlBuilder.buildDataUrl(id, filter);
        URL url = new URL(urlString);

        try {
            HttpURLConnection conn = HttpClientUtil.createConnection(urlString);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                String fault = upstreamFault(conn);
                _log.warning(urlString + " -> " + responseCode + " " + fault);
                ServletUtil.sendError(resp, responseCode, "Upstream API returned: " + fault);
                return;
            }

            InputStream is = HttpClientUtil.getInputStream(conn);
            String encoding = HttpClientUtil.getEncoding(conn, Listener.DEFAULT_ENCODING);

            BufferedReader in = new BufferedReader(new InputStreamReader(is, encoding));

            // 10 minutes cache
            resp.setHeader("Cache-Control", "max-age=600");

            Map<String, String> toc = (Map<String, String>) ctx.getAttribute(Listener.TOC);

            // Convert SDMX 3.0 TSV data to Turtle using SDMX 3.0 identifiers
            Da.convertWithSdmx3IdentifiersToTurtle(os, toc, id, in, urlString);
        } catch (IOException e) {
            ServletUtil.sendError(resp, 500, url + ": " + e.getMessage());
            e.printStackTrace();
            return;
        } catch (RuntimeException e) {
            ServletUtil.sendError(resp, 500, url + ": " + e.getMessage());
            e.printStackTrace();
            return;
        }

        os.close();
    }

    /**
     * Eurostat explains a rejected query in a SOAP fault's {@code faultstring} (e.g. "The
     * following values for dimension are not allowed: GEO=XX999"); the client needs that
     * text to fix its filter. Falls back to the HTTP reason phrase.
     */
    static String upstreamFault(HttpURLConnection conn) {
        String reason;
        try {
            reason = conn.getResponseMessage();
        } catch (IOException e) {
            reason = e.toString();
        }
        InputStream es = conn.getErrorStream();
        if (es == null) {
            return reason;
        }
        try (es) {
            String body = new String(es.readNBytes(16 * 1024), StandardCharsets.UTF_8);
            return faultString(body, reason);
        } catch (IOException e) {
            Logger.getLogger(DaServlet.class.getName()).warning("could not read upstream error body: " + e);
            return reason;
        }
    }

    static String faultString(String body, String fallback) {
        Matcher m = FAULT.matcher(body);
        if (!m.find()) {
            return fallback;
        }
        return m.group(1).replace("&apos;", "'").replace("&quot;", "\"").replace("&lt;", "<")
                .replace("&gt;", ">").replace("&amp;", "&");
    }

    private static final Pattern FAULT = Pattern.compile("<faultstring>([^<]*)</faultstring>");
}
