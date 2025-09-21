package com.ontologycentral.estatwrap.convert;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.ontologycentral.estatwrap.Main;

/**
 * Download and convert DSD (Data Structure Definition) files.
 *
 * @author aharth
 */
public class Dsd {
	Logger _log = Logger.getLogger(this.getClass().getName());

	private String _id;

	public Dsd(String id) {
		_id = id;
	}

	/**
	 * Convert DSD to RDF using XSLT transformation
	 *
	 * @param out Output stream to write the RDF to
	 * @param xslPath Path to the XSLT stylesheet (e.g., "/WEB-INF/dsd2rdf.xsl")
	 * @throws IOException
	 * @throws TransformerException
	 */
	public void convert(OutputStream out, String xslPath) throws IOException, TransformerException {
		URL u = new URL(Main.URI_PREFIX_3 + "/dataflow/ESTAT/" + _id);

		_log.info("retrieving " + u);

		HttpURLConnection conn = (HttpURLConnection)u.openConnection();
		conn.setConnectTimeout(8*1000);
		conn.setReadTimeout(8*1000);
		conn.setUseCaches(true);
		conn.setRequestProperty("User-Agent", "estatwrap.ontologycentral.com");

		if (conn.getResponseCode() != 200) {
			throw new IOException("lookup on " + u + " resulted HTTP in status code " + conn.getResponseCode());
		}

		InputStream is = conn.getInputStream();

		// Apply XSLT transformation directly to the XML stream
		TransformerFactory tf = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl",
			Thread.currentThread().getContextClassLoader());
		Transformer t = tf.newTransformer(new StreamSource(xslPath));

		StreamSource ssource = new StreamSource(is);
		StreamResult sresult = new StreamResult(out);

		_log.info("applying XSLT transformation");
		t.transform(ssource, sresult);

		is.close();
	}

	/**
	 * Convert DSD to RDF using provided transformer
	 *
	 * @param out Output stream to write the RDF to
	 * @param transformer Pre-configured XSLT transformer
	 * @throws IOException
	 * @throws TransformerException
	 */
	public void convert(OutputStream out, Transformer transformer) throws IOException, TransformerException {
		URL u = new URL(Main.URI_PREFIX_3 + "/dataflow/ESTAT/" + _id);

		_log.info("retrieving " + u);

		HttpURLConnection conn = (HttpURLConnection)u.openConnection();
		conn.setConnectTimeout(8*1000);
		conn.setReadTimeout(8*1000);
		conn.setUseCaches(true);
		conn.setRequestProperty("User-Agent", "estatwrap.ontologycentral.com");

		if (conn.getResponseCode() != 200) {
			throw new IOException("lookup on " + u + " resulted HTTP in status code " + conn.getResponseCode());
		}

		InputStream is = conn.getInputStream();

		StreamSource ssource = new StreamSource(is);
		StreamResult sresult = new StreamResult(out);

		_log.info("applying XSLT transformation");
		transformer.transform(ssource, sresult);

		is.close();
	}
}