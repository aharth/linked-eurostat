package com.ontologycentral.estatwrap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

/** Dimension filters on /da/{id}, passed through to Eurostat. No network. */
public class DataFilterTest {

    private static Map<String, String[]> params(String... kv) {
        Map<String, String[]> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put(kv[i], new String[] {kv[i + 1]});
        }
        return m;
    }

    @Test
    public void codesAndTimeRangeBecomeUpstreamFilters() {
        DataFilter f = DataFilter.parse(params("geo", "DE254,DE255", "TIME_PERIOD", "ge:2023+le:2024"));
        assertEquals("&c%5Bgeo%5D=DE254%2CDE255&c%5BTIME_PERIOD%5D=ge%3A2023%2Ble%3A2024",
                f.toUpstreamQuery());
        assertTrue(UrlBuilder.buildDataUrl("demo_r_pjanaggr3", f)
                .endsWith("/demo_r_pjanaggr3/1.0?format=tsv&compress=false" + f.toUpstreamQuery()));
    }

    @Test
    public void unencodedPlusInTimeArrivesAsSpaceAndIsRestored() {
        DataFilter f = DataFilter.parse(params("TIME_PERIOD", "ge:2015 le:2020"));
        assertEquals("ge:2015+le:2020", f.filters().get("TIME_PERIOD"));
    }

    @Test
    public void repeatedParameterIsAList() {
        Map<String, String[]> m = new LinkedHashMap<>();
        m.put("cities", new String[] {"DE014C", "DE003C"});
        assertEquals("DE014C,DE003C", DataFilter.parse(m).filters().get("cities"));
    }

    @Test
    public void noParametersMeansNoFilter() {
        assertTrue(DataFilter.parse(new LinkedHashMap<>()).isEmpty());
        assertEquals("", DataFilter.none().toUpstreamQuery());
    }

    @Test
    public void prefixesValueFiltersAndInjectionAreRejected() {
        String[][] bad = {
            {"geo", "DE2*"}, {"geo", "sw:DE2"}, {"OBS_VALUE", "ge:500000"}, {"geo", "DE254&c[x]=1"},
            {"geo", ""}, {"c[geo]", "DE254"}, {"TIME_PERIOD", "last"},
        };
        for (String[] b : bad) {
            try {
                DataFilter.parse(params(b[0], b[1]));
                fail("accepted " + b[0] + "=" + b[1]);
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage(), expected.getMessage().contains(b[0]));
            }
        }
    }
}
