package com.ontologycentral.estatwrap.convert;

import com.ontologycentral.estatwrap.Main;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import junit.framework.TestCase;

public class DataTest extends TestCase {
    static final Logger _log = Logger.getLogger(DataTest.class.getName());

    public void testData() throws Exception {
        String id = "teimf040"; // "dt_oth_3d51_03"; //"earn_ses_actrl";

        URL url = new URL(Main.URI_PREFIX_21 + "/data/" + id + "/?format=TSV&compressed=true");
        // URL url = new URL("http://europa.eu/estatref/download/everybody/data/" + id + ".tsv.gz");

        _log.log(Level.INFO, "{0}", url);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        _log.log(Level.INFO, "{0}", conn.getRequestProperties());

        conn.addRequestProperty("User-Agent", "wget");
        InputStream is = new GZIPInputStream(conn.getInputStream());

        _log.log(Level.INFO, "{0}", conn.getHeaderFields());

        //		resp.setHeader("Cache-Control", "public");
        //		Calendar c = Calendar.getInstance();
        //		c.add(Calendar.DATE, 1);
        //		resp.setHeader("Expires", Listener.RFC822.format(c.getTime()));

        Data d = new Data(new InputStreamReader(is));

        XMLOutputFactory factory = XMLOutputFactory.newInstance();

        XMLStreamWriter ch = factory.createXMLStreamWriter(System.out);

        ch.writeStartDocument("utf-8", "1.0");

        ch.writeStartElement("rdf:RDF");
        ch.writeNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        ch.writeNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        ch.writeDefaultNamespace(Data.PREFIX);

        ch.writeStartElement("rdf:Description");
        ch.writeAttribute("rdf:about", "");
        ch.writeStartElement("rdfs:comment");
        ch.writeCharacters(
                "Source: Eurostat (http://epp.eurostat.ec.europa.eu/) via estatwrap (http://estatwrap.ontologycentral.com/).");
        ch.writeEndElement();
        ch.writeStartElement("rdfs:seeAlso");
        ch.writeAttribute(
                "rdf:resource",
                "http://epp.eurostat.ec.europa.eu/portal/page/portal/about_eurostat/corporate/copyright_licence_policy");
        ch.writeEndElement();
        ch.writeEndElement();

        d.convert(ch, id);

        ch.writeEndElement();
        ch.writeEndDocument();

        ch.close();
    }
}
