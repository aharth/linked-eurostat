package com.ontologycentral.estatwrap.webapp;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

final class ServletUtil {
    private static final Logger _log = Logger.getLogger(ServletUtil.class.getName());

    private ServletUtil() {
    }

    /**
     * Sends an error as a one-line {@code text/plain} body ({@code 400 Bad Request: value of geo
     * does not match ...}) instead of the container's HTML page, so a script can read it.
     *
     * <p>Headers already set stay (CORS from the filter), except the content type, which becomes
     * {@code text/plain}, and {@code Cache-Control}, which becomes {@code no-store}: some servlets
     * set a day's {@code max-age} before fetching, and an error must not be cached that long.
     *
     * <p>Unless the response is already committed (output was streamed before the failure, or
     * the client disconnected mid-stream): then the status line is on the wire and only a log
     * line is possible.
     */
    static void sendError(HttpServletResponse resp, int code, String msg) throws IOException {
        if (resp.isCommitted()) {
            _log.warning("Response already committed, cannot send " + code + ": " + msg);
            return;
        }
        resp.resetBuffer();
        resp.setStatus(code);
        resp.setContentType("text/plain;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        String body = code + " " + reason(code) + (msg == null || msg.isEmpty() ? "" : ": " + msg) + "\n";
        try {
            resp.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
        } catch (IllegalStateException e) {
            // the servlet already took the writer
            resp.getWriter().write(body);
        }
        resp.flushBuffer();
    }

    static void sendError(HttpServletResponse resp, int code) throws IOException {
        sendError(resp, code, null);
    }

    static String reason(int code) {
        return switch (code) {
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 406 -> "Not Acceptable";
            case 413 -> "Content Too Large";
            case 429 -> "Too Many Requests";
            case 500 -> "Internal Server Error";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            case 504 -> "Gateway Timeout";
            default -> "Error";
        };
    }
}
