package com.ontologycentral.estatwrap.webapp;

import com.ontologycentral.estatwrap.UpstreamHealth;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.jena.atlas.json.JsonObject;

/**
 * {@code /status} and {@code /status.json}: what this wrapper has SEEN of Eurostat, never a live
 * call, so it is safe to poll. Per kind of upstream call ({@code data}, {@code structure},
 * {@code catalogue}, and {@code self} for the SPARQL endpoint's own fetches; see
 * {@link UpstreamHealth}): state, last success, last failure and its error. Also the table of
 * contents loaded at startup (dataset count, when, or why it failed).
 *
 * <p>200 when data can be served (the last {@code data} call did not fail), else 503; the body
 * says which. The form is negotiated as in the siblings (linked-regionalstatistik, linked-bnetza's
 * {@code DumpStatus}): {@code .json} or no preference is JSON, {@code text/plain} when the header
 * ranks it above JSON, anything else a 406.
 */
@SuppressWarnings("serial")
public class StatusServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(StatusServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String form = form(req.getServletPath(), req.getHeader("Accept"));
        resp.setHeader("Vary", "Accept");
        if (form == null) {
            ServletUtil.sendError(resp, HttpServletResponse.SC_NOT_ACCEPTABLE,
                    "Not acceptable. This resource has: application/json (.json), text/plain");
            return;
        }
        var ctx = getServletContext();
        JsonObject o = status(ctx.getAttribute(Listener.TOC), ctx.getAttribute(Listener.TOC_TIME),
                ctx.getAttribute(Listener.TOC_ERROR));
        resp.setStatus(servable() ? HttpServletResponse.SC_OK
                : HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        resp.setHeader("Cache-Control", "no-store");
        resp.setContentType(("text".equals(form) ? "text/plain" : "application/json")
                + ";charset=UTF-8");
        String body = "text".equals(form) ? text(o) : o.toString() + "\n";
        resp.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
        resp.getOutputStream().flush();
    }

    static boolean servable() {
        return !"failing".equals(UpstreamHealth.get(UpstreamHealth.DATA).state());
    }

    static JsonObject status(Object toc, Object tocTime, Object tocError) {
        JsonObject o = new JsonObject();
        o.put("servable", servable());
        JsonObject eurostat = new JsonObject();
        eurostat.put(UpstreamHealth.DATA, kind(UpstreamHealth.DATA));
        eurostat.put(UpstreamHealth.STRUCTURE, kind(UpstreamHealth.STRUCTURE));
        eurostat.put(UpstreamHealth.CATALOGUE, kind(UpstreamHealth.CATALOGUE));
        o.put("eurostat", eurostat);
        // Not Eurostat: the SPARQL endpoint fetching its FROM graphs back via the public base.
        o.put(UpstreamHealth.SELF, kind(UpstreamHealth.SELF));

        JsonObject t = new JsonObject();
        boolean loaded = toc instanceof Map<?, ?> m && !m.isEmpty();
        t.put("loaded", loaded);
        if (toc instanceof Map<?, ?> m) {
            t.put("datasets", m.size());
        }
        if (tocTime instanceof Long ms) {
            put(t, "built", Instant.ofEpochMilli(ms));
        }
        if (tocError instanceof String err) {
            t.put("lastError", err);
        }
        o.put("toc", t);
        return o;
    }

    private static JsonObject kind(String kind) {
        UpstreamHealth.Kind k = UpstreamHealth.get(kind);
        JsonObject j = new JsonObject();
        j.put("state", k.state());
        put(j, "lastSuccess", k.lastSuccess());
        put(j, "lastFailure", k.lastFailure());
        if (k.lastError() != null) {
            j.put("lastError", k.lastError());
            j.put("lastFailedUrl", k.lastFailedUrl());
        }
        j.put("failuresInRow", k.failuresInRow());
        return j;
    }

    /** The JSON flattened to {@code path: value} lines. */
    static String text(JsonObject o) {
        StringBuilder sb = new StringBuilder();
        flatten("", o, sb);
        return sb.toString();
    }

    private static void flatten(String prefix, JsonObject o, StringBuilder sb) {
        for (String k : o.keys()) {
            var v = o.get(k);
            if (v.isObject()) {
                flatten(prefix + k + ".", v.getAsObject(), sb);
            } else {
                String s = v.isString() ? v.getAsString().value() : v.toString();
                sb.append(prefix).append(k).append(": ").append(s).append('\n');
            }
        }
    }

    private static void put(JsonObject j, String key, Instant t) {
        if (t != null) {
            j.put(key, t.toString());
        }
    }

    /** {@code json}, {@code text}, or {@code null} for a 406. */
    static String form(String servletPath, String accept) {
        if (servletPath != null && servletPath.endsWith(".json")) {
            return "json";
        }
        if (accept == null || accept.isBlank()) {
            return "json";
        }
        double json = q(accept, "application", "json");
        double text = q(accept, "text", "plain");
        if (json <= 0 && text <= 0) {
            return null;
        }
        return text > json ? "text" : "json";
    }

    private static double q(String accept, String type, String sub) {
        double best = 0;
        for (String range : accept.split(",")) {
            String[] parts = range.trim().split(";");
            String[] ts = parts[0].trim().toLowerCase(Locale.ROOT).split("/");
            if (ts.length != 2 || !(ts[0].equals("*") && ts[1].equals("*")
                    || ts[0].equals(type) && (ts[1].equals("*") || ts[1].equals(sub)))) {
                continue;
            }
            double q = 1;
            for (int i = 1; i < parts.length; i++) {
                String p = parts[i].trim();
                if (p.startsWith("q=")) {
                    try {
                        q = Double.parseDouble(p.substring(2));
                    } catch (NumberFormatException e) {
                        LOG.warning("status: malformed q in Accept \"" + accept
                                + "\", range counted as not acceptable: " + e.getMessage());
                        q = 0;
                    }
                }
            }
            best = Math.max(best, q);
        }
        return best;
    }
}
