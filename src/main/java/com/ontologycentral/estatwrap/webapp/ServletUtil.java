package com.ontologycentral.estatwrap.webapp;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

final class ServletUtil {
    private static final Logger _log = Logger.getLogger(ServletUtil.class.getName());

    private ServletUtil() {
    }

    /**
     * Sends an error response unless the response is already committed (e.g. output was
     * streamed before the failure, or the client disconnected mid-stream). In that case the
     * status line is already on the wire and sendError() would throw IllegalStateException,
     * so the failure is only logged.
     */
    static void sendError(HttpServletResponse resp, int code, String msg) throws IOException {
        if (resp.isCommitted()) {
            _log.warning("Response already committed, cannot send " + code + ": " + msg);
            return;
        }
        resp.sendError(code, msg);
    }

    static void sendError(HttpServletResponse resp, int code) throws IOException {
        if (resp.isCommitted()) {
            _log.warning("Response already committed, cannot send " + code);
            return;
        }
        resp.sendError(code);
    }
}
