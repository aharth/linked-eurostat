package com.ontologycentral.estatwrap.webapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.Test;

/** Size cap on FROM graph fetches. */
public class SparqlServletTest {

    @Test
    public void streamUpToLimitPassesThrough() throws IOException {
        byte[] data = new byte[100];
        try (SparqlServlet.CappedInputStream is =
                new SparqlServlet.CappedInputStream(new ByteArrayInputStream(data), 100, "http://x/g")) {
            byte[] buf = new byte[64];
            int total = 0;
            int n;
            while ((n = is.read(buf, 0, buf.length)) > 0) {
                total += n;
            }
            assertEquals(100, total);
        }
    }

    @Test
    public void streamBeyondLimitThrowsDescriptiveError() throws IOException {
        byte[] data = new byte[101];
        try (SparqlServlet.CappedInputStream is =
                new SparqlServlet.CappedInputStream(new ByteArrayInputStream(data), 100, "http://x/g")) {
            byte[] buf = new byte[64];
            try {
                while (is.read(buf, 0, buf.length) > 0) {
                    // consume
                }
                fail("expected GraphTooLargeException");
            } catch (SparqlServlet.GraphTooLargeException e) {
                assertTrue(e.getMessage().contains("http://x/g"));
                assertTrue(e.getMessage().contains("too large"));
            }
        }
    }

    @Test
    public void singleByteReadsAreCounted() throws IOException {
        byte[] data = new byte[3];
        try (SparqlServlet.CappedInputStream is =
                new SparqlServlet.CappedInputStream(new ByteArrayInputStream(data), 2, "http://x/g")) {
            is.read();
            is.read();
            try {
                is.read();
                fail("expected GraphTooLargeException");
            } catch (SparqlServlet.GraphTooLargeException expected) {
                // third byte exceeds the 2-byte limit
            }
        }
    }
}
