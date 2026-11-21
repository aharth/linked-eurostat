package com.ontologycentral.estatwrap.webapp;

import com.ontologycentral.estatwrap.HttpClientUtil;
import com.ontologycentral.estatwrap.convert.ToC;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

public class Listener implements ServletContextListener {
    Logger _log = Logger.getLogger(this.getClass().getName());

    public static String FACTORY = "f";
    public static String TOC = "t";
    /** When the TOC was loaded (epoch millis), absent if it never was. */
    public static String TOC_TIME = "t.time";
    /** Why the TOC failed to load, absent if it did not fail. */
    public static String TOC_ERROR = "t.error";
    public static String CS_T = "cs";
    public static String CL_T = "cl";
    public static String DS_T = "ds";
    public static String DF_T = "df";
    public static String DC_T = "dc";


    public static String DEFAULT_ENCODING = "utf-8";

    public void contextInitialized(ServletContextEvent event) {
        ServletContext ctx = event.getServletContext();

        XMLOutputFactory factory = XMLOutputFactory.newInstance();

        ctx.setAttribute(FACTORY, factory);

        TransformerFactory tf =
                TransformerFactory.newInstance(
                        "net.sf.saxon.TransformerFactoryImpl",
                        Thread.currentThread().getContextClassLoader());

        try {
Transformer csT =
                    tf.newTransformer(new StreamSource(ctx.getRealPath("/WEB-INF/cs2rdf.xsl")));
            ctx.setAttribute(CS_T, csT);

            Transformer clT =
                    tf.newTransformer(new StreamSource(ctx.getRealPath("/WEB-INF/cl2rdf.xsl")));
            ctx.setAttribute(CL_T, clT);

            Transformer dsT =
                    tf.newTransformer(new StreamSource(ctx.getRealPath("/WEB-INF/ds2rdf.xsl")));
            ctx.setAttribute(DS_T, dsT);

            Transformer dfT =
                    tf.newTransformer(new StreamSource(ctx.getRealPath("/WEB-INF/df2rdf.xsl")));
            ctx.setAttribute(DF_T, dfT);

            Transformer dcT =
                    tf.newTransformer(new StreamSource(ctx.getRealPath("/WEB-INF/dc2rdf.xsl")));
            ctx.setAttribute(DC_T, dcT);
        } catch (TransformerConfigurationException e) {
            _log.severe(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // The dataset titles, fetched once at startup. On failure the wrapper runs with an
        // empty map; /status says so (TOC_ERROR) instead of leaving it to be noticed.
        Map<String, String> map;
        String tocUrl = "https://ec.europa.eu/eurostat/api/dissemination/catalogue/toc/xml";
        try {
            HttpURLConnection conn = HttpClientUtil.createConnection(tocUrl);
            HttpClientUtil.checkResponseCode(conn, tocUrl);
            InputStream is = conn.getInputStream();

            // Parse XML ToC using existing ToC class
            ToC toc = new ToC(is, "utf-8");
            map = toc.convert();
            ctx.setAttribute(TOC_TIME, System.currentTimeMillis());
        } catch (IOException | XMLStreamException e) {
            _log.severe("table of contents not loaded, dataset titles missing until restart: " + e);
            e.printStackTrace();
            map = new HashMap<String, String>();
            ctx.setAttribute(TOC_ERROR, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        ctx.setAttribute(TOC, map);
    }

    public void contextDestroyed(ServletContextEvent event) {
        // Nothing to clean up
    }
}
