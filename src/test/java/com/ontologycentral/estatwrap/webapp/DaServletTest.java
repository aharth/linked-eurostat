package com.ontologycentral.estatwrap.webapp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Eurostat's fault text reaches the client. No network. */
public class DaServletTest {

    @Test
    public void faultStringIsExtractedAndUnescaped() {
        String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><S:Fault xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<faultcode>150</faultcode><faultstring>INVALID_QUERY_DIMENSION_VALUE: Query is invalid as per its"
                + " structure&apos;s definition. The following values for dimension are not allowed: GEO=XX999."
                + "</faultstring></S:Fault>";
        assertEquals("INVALID_QUERY_DIMENSION_VALUE: Query is invalid as per its structure's definition."
                + " The following values for dimension are not allowed: GEO=XX999.",
                DaServlet.faultString(body, "Bad Request"));
    }

    @Test
    public void withoutFaultTheReasonPhraseIsKept() {
        assertEquals("Bad Request", DaServlet.faultString("<html>oops</html>", "Bad Request"));
    }
}
