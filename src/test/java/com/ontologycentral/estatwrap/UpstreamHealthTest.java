package com.ontologycentral.estatwrap;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.junit.Test;

/** Upstream outcomes per kind, as /status reports them. No network. */
public class UpstreamHealthTest {

    static final String E = "https://ec.europa.eu/eurostat/api/dissemination/";

    @Test
    public void kindsComeFromTheUrl() {
        assertEquals("data", UpstreamHealth.kindOf(E + "sdmx/3.0/data/dataflow/ESTAT/tag00038/1.0?format=tsv"));
        assertEquals("structure", UpstreamHealth.kindOf(E + "sdmx/3.0/structure/codelist/ESTAT/GEO"));
        assertEquals("catalogue", UpstreamHealth.kindOf(E + "catalogue/toc/xml"));
        assertEquals("catalogue", UpstreamHealth.kindOf(E + "files/inventory?type=codelist"));
        assertEquals("self", UpstreamHealth.kindOf("https://estatwrap.ontologycentral.com/da/tag00038"));
    }

    @Test
    public void kindsFailApart() throws InterruptedException {
        UpstreamHealth.reset();
        String data = E + "sdmx/3.0/data/dataflow/ESTAT/x/1.0";
        assertEquals("unknown", UpstreamHealth.get("data").state());
        UpstreamHealth.success(E + "catalogue/toc/xml");
        UpstreamHealth.success(data);
        Thread.sleep(2);
        UpstreamHealth.failure(data, new IOException("Read timed out"));
        UpstreamHealth.failure(data, new IOException("Read timed out"));
        assertEquals("failing", UpstreamHealth.get("data").state());
        assertEquals(2, UpstreamHealth.get("data").failuresInRow());
        assertEquals("IOException: Read timed out", UpstreamHealth.get("data").lastError());
        assertEquals(data, UpstreamHealth.get("data").lastFailedUrl());
        assertEquals("ok", UpstreamHealth.get("catalogue").state());
        assertEquals("unknown", UpstreamHealth.get("structure").state());
        Thread.sleep(2);
        UpstreamHealth.success(data);
        assertEquals("ok", UpstreamHealth.get("data").state());
        assertEquals(0, UpstreamHealth.get("data").failuresInRow());
        UpstreamHealth.reset();
    }
}
