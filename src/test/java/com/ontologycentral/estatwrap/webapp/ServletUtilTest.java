package com.ontologycentral.estatwrap.webapp;

import static org.junit.Assert.assertEquals;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/** Errors are one plain-text line, not the container's HTML. No container. */
public class ServletUtilTest {

    @Test
    public void errorIsPlainTextNotCachedAndKeepsOtherHeaders() throws Exception {
        Map<String, Object> state = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Cache-Control", "max-age=86400");
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        ServletOutputStream os = new ServletOutputStream() {
            @Override public void write(int b) { body.write(b); }
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener l) { }
        };
        HttpServletResponse resp = (HttpServletResponse) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[] {HttpServletResponse.class}, (p, m, a) -> {
                    switch (m.getName()) {
                        case "isCommitted": return false;
                        case "setStatus": state.put("status", a[0]); return null;
                        case "setContentType": state.put("type", a[0]); return null;
                        case "setHeader": headers.put((String) a[0], (String) a[1]); return null;
                        case "getOutputStream": return os;
                        default: return null;
                    }
                });
        ServletUtil.sendError(resp, 400, "value of geo does not match ^[A-Za-z0-9_-]+$: DE2*");
        assertEquals(400, state.get("status"));
        assertEquals("text/plain;charset=UTF-8", state.get("type"));
        assertEquals("no-store", headers.get("Cache-Control"));
        assertEquals("*", headers.get("Access-Control-Allow-Origin"));
        assertEquals("400 Bad Request: value of geo does not match ^[A-Za-z0-9_-]+$: DE2*\n",
                body.toString(StandardCharsets.UTF_8));
    }
}
