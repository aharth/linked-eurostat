package com.ontologycentral.estatwrap.convert;

import com.ontologycentral.estatwrap.HttpClientUtil;
import com.ontologycentral.estatwrap.UrlBuilder;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import org.junit.Test;

/**
 * Test handling of encoding.
 *
 * @author aharth
 */
public class TestEncoding {

    @Test
    public void test() throws Exception {
        String id = "cities";

        String url = UrlBuilder.buildCodeListUrl(id);

        System.out.println("looking up " + url);

        HttpURLConnection conn = HttpClientUtil.createConnection(url);

        if (conn.getResponseCode() != 200) {
            System.err.println("Error: " + conn.getResponseCode());
            return;
        }

        InputStream is = HttpClientUtil.getInputStream(conn);
        String encoding = HttpClientUtil.getEncoding(conn, "UTF-8");

        System.err.println("Encoding: " + encoding);

        BufferedReader in = new BufferedReader(new InputStreamReader(is, encoding));
        String l;
        StringBuilder sb = new StringBuilder();

        while ((l = in.readLine()) != null) {
            sb.append(l);
            sb.append('\n');
        }
        in.close();

        String str = sb.toString();
        System.out.println(str);
    }
}
