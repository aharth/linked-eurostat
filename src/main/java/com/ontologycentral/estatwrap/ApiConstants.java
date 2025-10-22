package com.ontologycentral.estatwrap;

/**
 * Shared API constants for Eurostat SDMX services.
 */
public final class ApiConstants {

    public static final String URI_PREFIX_21 = "https://ec.europa.eu/eurostat/api/dissemination/sdmx/2.1";
    public static final String URI_PREFIX_3 = "https://ec.europa.eu/eurostat/api/dissemination/sdmx/3.0";
    public static final String TOC_XML_URL = "https://ec.europa.eu/eurostat/api/dissemination/catalogue/toc/xml";

    public static final int DEFAULT_CONNECT_TIMEOUT = 55000;
    public static final int DEFAULT_READ_TIMEOUT = 55000;

    private ApiConstants() {
        // Utility class
    }
}