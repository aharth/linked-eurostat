package com.ontologycentral.estatwrap.webapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.ontologycentral.estatwrap.UpstreamHealth;
import java.util.HashMap;
import java.util.Map;
import org.apache.jena.atlas.json.JsonObject;
import org.junit.Test;

/** /status from recorded outcomes only. No network. */
public class StatusServletTest {

    static final String DATA = "https://ec.europa.eu/eurostat/api/dissemination/sdmx/3.0/data/dataflow/ESTAT/x/1.0";

    @Test
    public void negotiatesLikeTheSiblings() {
        assertEquals("json", StatusServlet.form("/status", null));
        assertEquals("json", StatusServlet.form("/status.json", "text/plain"));
        assertEquals("text", StatusServlet.form("/status", "text/plain, application/json;q=0.5"));
        assertEquals("json", StatusServlet.form("/status", "*/*"));
        assertNull(StatusServlet.form("/status", "text/html"));
    }

    @Test
    public void aFailedDataCallMakesItUnservable() {
        UpstreamHealth.reset();
        UpstreamHealth.success("https://ec.europa.eu/eurostat/api/dissemination/catalogue/toc/xml");
        UpstreamHealth.failure(DATA, new java.net.SocketTimeoutException("Read timed out"));
        JsonObject o = StatusServlet.status(null, null, null);
        assertFalse(o.get("servable").getAsBoolean().value());
        String t = StatusServlet.text(o);
        assertTrue(t, t.contains("eurostat.data.state: failing"));
        assertTrue(t, t.contains("eurostat.data.lastError: SocketTimeoutException: Read timed out"));
        assertTrue(t, t.contains("eurostat.data.lastFailedUrl: " + DATA));
        assertTrue(t, t.contains("eurostat.catalogue.state: ok"));
        assertTrue(t, t.contains("eurostat.structure.state: unknown"));
        assertTrue(t, t.contains("self.state: unknown"));
        UpstreamHealth.success(DATA);
        assertTrue(StatusServlet.status(null, null, null).get("servable").getAsBoolean().value());
        UpstreamHealth.reset();
    }

    @Test
    public void reportsTheTableOfContents() {
        Map<String, String> toc = new HashMap<>();
        toc.put("tag00038", "GDP");
        String ok = StatusServlet.text(StatusServlet.status(toc, 0L, null));
        assertTrue(ok, ok.contains("toc.loaded: true"));
        assertTrue(ok, ok.contains("toc.datasets: 1"));
        assertTrue(ok, ok.contains("toc.built: 1970-01-01T00:00:00Z"));
        String failed = StatusServlet.text(StatusServlet.status(new HashMap<>(), null, "IOException: HTTP 503"));
        assertTrue(failed, failed.contains("toc.loaded: false"));
        assertTrue(failed, failed.contains("toc.lastError: IOException: HTTP 503"));
    }
}
