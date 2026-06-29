package com.ontologycentral.estatwrap.convert;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 * @author aharth
 */
public class ToCTest extends TestCase {
    Logger _log = Logger.getLogger(this.getClass().getName());

    public void testData() throws Exception {
        URL u = new URL(com.ontologycentral.estatwrap.ApiConstants.TOC_XML_URL);

        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        InputStream is = conn.getInputStream();

        ToC toc = new ToC(is, "utf-8");

        Map<String, String> map = toc.convert();

        // _log.log(Level.INFO, "{0}", map);

        FileOutputStream fos = new FileOutputStream("/tmp/foo");
        PrintWriter pw = new PrintWriter(fos);
        pw.print(map);
        pw.close();
        fos.close();

        for (String key : map.keySet()) {
            // System.out.println(key + ": " + map.get(key));
            if (key.equals("tec00114")) {
                _log.log(Level.INFO, "bingo");
            }
        }
    }
}
