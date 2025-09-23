package com.ontologycentral.estatwrap.convert;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.util.logging.Logger;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Download and convert CS (Concept Scheme) files.
 *
 * @author aharth
 */
public class Cs {
    Logger _log = Logger.getLogger(this.getClass().getName());

    /**
     * Convert Concept Scheme to RDF using XSLT transformation
     *
     * @param out Output stream to write the RDF to
     * @param xslPath Path to the XSLT stylesheet (e.g., "/WEB-INF/conceptscheme2rdf.xsl")
     * @throws IOException
     * @throws TransformerException
     */
    public void convert(Reader in, OutputStream out, String xslPath)
            throws IOException, TransformerException {
        // Apply XSLT transformation directly to the XML stream
        TransformerFactory tf =
                TransformerFactory.newInstance(
                        "net.sf.saxon.TransformerFactoryImpl",
                        Thread.currentThread().getContextClassLoader());
        Transformer t = tf.newTransformer(new StreamSource(xslPath));

        StreamSource ssource = new StreamSource(in);
        StreamResult sresult = new StreamResult(out);

        _log.info("applying XSLT transformation for concept scheme");
        t.transform(ssource, sresult);
    }

    /**
     * Convert Concept Scheme to RDF using provided transformer
     *
     * @param out Output stream to write the RDF to
     * @param transformer Pre-configured XSLT transformer
     * @throws IOException
     * @throws TransformerException
     */
    public void convert(Reader in, OutputStream out, Transformer transformer)
            throws IOException, TransformerException {
        StreamSource ssource = new StreamSource(in);
        StreamResult sresult = new StreamResult(out);

        _log.info("applying XSLT transformation for concept scheme");
        transformer.transform(ssource, sresult);
    }
}