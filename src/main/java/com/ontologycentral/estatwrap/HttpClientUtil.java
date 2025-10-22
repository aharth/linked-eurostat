package com.ontologycentral.estatwrap;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * Shared HTTP client utilities for Eurostat SDMX API access.
 */
public final class HttpClientUtil {

    /**
     * Creates an HTTP connection with standard Eurostat API settings.
     *
     * @param url the URL to connect to
     * @return configured HttpURLConnection
     * @throws IOException if connection creation fails
     */
    public static HttpURLConnection createConnection(String url) throws IOException {
        return createConnection(url, ApiConstants.DEFAULT_CONNECT_TIMEOUT, ApiConstants.DEFAULT_READ_TIMEOUT);
    }

    /**
     * Creates an HTTP connection with custom timeout settings.
     *
     * @param url the URL to connect to
     * @param connectTimeout connection timeout in milliseconds
     * @param readTimeout read timeout in milliseconds
     * @return configured HttpURLConnection
     * @throws IOException if connection creation fails
     */
    public static HttpURLConnection createConnection(String url, int connectTimeout, int readTimeout) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);
        conn.setUseCaches(true);
        conn.setRequestProperty("User-Agent", BuildInfo.getUserAgent());
        return conn;
    }

    /**
     * Gets appropriate InputStream handling GZIP compression automatically.
     *
     * @param connection the HTTP connection
     * @return InputStream (possibly wrapped with GZIPInputStream)
     * @throws IOException if stream creation fails
     */
    public static InputStream getInputStream(HttpURLConnection connection) throws IOException {
        String url = connection.getURL().toString();
        InputStream is = connection.getInputStream();

        if (url.contains("compressed=true") || url.contains("compress=true")) {
            return new GZIPInputStream(is);
        }
        return is;
    }

    /**
     * Extracts character encoding from HTTP Content-Type header.
     *
     * @param connection the HTTP connection
     * @param defaultEncoding default encoding if none specified
     * @return character encoding name
     */
    public static String getEncoding(HttpURLConnection connection, String defaultEncoding) {
        String contentType = connection.getContentType();
        if (contentType != null && contentType.contains("charset=")) {
            String encoding = contentType.substring(contentType.indexOf("charset=") + 8);
            if (encoding.contains(";")) {
                encoding = encoding.substring(0, encoding.indexOf(";"));
            }
            return encoding;
        }
        return defaultEncoding;
    }

    /**
     * Checks HTTP response code and throws IOException for non-200 responses.
     *
     * @param connection the HTTP connection to check
     * @param context descriptive context for error messages
     * @throws IOException if response code is not 200
     */
    public static void checkResponseCode(HttpURLConnection connection, String context) throws IOException {
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("HTTP " + responseCode + " from " + context + ": " + connection.getResponseMessage());
        }
    }

    private HttpClientUtil() {
        // Utility class
    }
}