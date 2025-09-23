package com.ontologycentral.estatwrap.webapp;

import com.ontologycentral.estatwrap.convert.ToC;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
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
    public static String CACHE = "c";

    public static String CS_T = "cs";
    public static String CL_T = "cl";
    public static String DS_T = "ds";
    public static String DF_T = "df";
    public static String DC_T = "dc";

    public static String NUTS = "nuts";

    public static String DEFAULT_ENCODING = "utf-8";

    public void contextInitialized(ServletContextEvent event) {
        ServletContext ctx = event.getServletContext();

        XMLOutputFactory factory = XMLOutputFactory.newInstance();

        ctx.setAttribute(FACTORY, factory);

        Cache cache = null;

        try {
            //          CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
            CacheManager cmanager = Caching.getCachingProvider().getCacheManager();

            MutableConfiguration<String, Date> config = new MutableConfiguration<String, Date>();
            config.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.ONE_DAY))
                    .setStatisticsEnabled(true);

            cache = cmanager.createCache("cache", config);
            ctx.setAttribute(CACHE, cache);
        } catch (CacheException e) {
            e.printStackTrace();
        }

        // cache.clear();

        TransformerFactory tf =
                TransformerFactory.newInstance(
                        "net.sf.saxon.TransformerFactoryImpl",
                        Thread.currentThread().getContextClassLoader());

        //		javax.xml.transform.TransformerFactory tf =
        //			javax.xml.transform.TransformerFactory.newInstance();
        // //"org.apache.xalan.processor.TransformerFactoryImpl", this.getClass().getClassLoader()
        // );

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

        Map<String, String> map = null;

        //	    if (cache.containsKey(TOC)) {
        //	    	map = (Map<String, String>)cache.get(TOC);
        //	    }

        if (map == null) {
            try {
                // Use new XML API endpoint instead of old text format
                URL u =
                        new URL(
                                "https://ec.europa.eu/eurostat/api/dissemination/catalogue/toc/xml");
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                InputStream is = conn.getInputStream();

                // Parse XML ToC using existing ToC class (will need updating)
                ToC toc = new ToC(is, "utf-8");
                map = toc.convert();
                //	    		cache.put(TOC, map);
            } catch (MalformedURLException e) {
                _log.severe(e.getMessage());
                e.printStackTrace();
                map = new HashMap<String, String>();
            } catch (IOException e) {
                _log.severe(e.getMessage());
                e.printStackTrace();
                map = new HashMap<String, String>();
            } catch (XMLStreamException e) {
                _log.severe(e.getMessage());
                e.printStackTrace();
                map = new HashMap<String, String>();
            }
        }
        // map = new HashMap<String, String>();
        ctx.setAttribute(TOC, map);

        InputStream in = null;
        BufferedReader br = null;

        try {
            Set<String> nuts = new HashSet<String>();
            in = new FileInputStream(ctx.getRealPath("/WEB-INF/nuts.txt"));
            br = new BufferedReader(new InputStreamReader(in));

            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String nutscode = line.trim();

                if (!nutscode.isEmpty()) {
                    nuts.add(nutscode);
                }
            }
            ctx.setAttribute(NUTS, nuts);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {
                br.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void contextDestroyed(ServletContextEvent event) {
        // TODO Auto-generated method stub
    }
}
