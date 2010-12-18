package com.ontologycentral.estatwrap.convert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class Data {
	Logger _log = Logger.getLogger(this.getClass().getName());

	public static String PREFIX = "http://ontologycentral.com/2009/01/eurostat/ns#";
	
	BufferedReader _in;
	
	// here, use a threshold to limit the amount of data converted (GAE limitations)
	public static int MAX_COLS = 24;
	public static int MAX_ROWS = 1024;

	public Data(Reader sr) throws IOException {
		_in = new BufferedReader(sr);
	}
	
	public void convert(XMLStreamWriter out, String id) throws IOException, XMLStreamException {
		String line = null;

		int rows = 0;
		Header h = null;
		Line l = null;

		if ((line = _in.readLine()) != null) {
			++rows;
			line = line.trim();
			if (line.length() <= 0) {
				throw new IOException("could not read header!");
			}

			h = new Header(line);
		}
		
		while ((line = _in.readLine()) != null) {
			++rows;
			line = line.trim();
			if (line.length() <= 0) {
				continue;
			}

			l = new Line(line);

			printTriple(h, l, out, rows, id);
			
			if (rows > MAX_ROWS) {
				break;
			}
		}

		_in.close();
	}

	public void printTriple(Header h, Line l, XMLStreamWriter out, int bnodeid, String id) throws XMLStreamException {
		List hd1 = h.getDim1();
		List ld1 = l.getDim1();

		if (hd1.size() != ld1.size()) {
			System.err.println("header dimensions and line dimensions don't match!");
		}

		List hcol = h.getCols();
		List lcol = l.getCols();

		if (hcol.size() != lcol.size()) {
			System.err.println("header columns and line columns don't match!");
		}
		
		int start = 0;
		int end = Math.min(hcol.size(), MAX_COLS);
		
		// hack - some stats are sorted from oldest to newest, some the other way round
		// check if the last entry contains year 200x or 201x
		String last = (String)hcol.get(hcol.size()-1);
		//System.out.println(last);
		if (last.contains("200") || last.contains("201")) {
			start = hcol.size()-MAX_COLS;
			if (start < 0) {
				start = 0;
			}
			end = hcol.size();
		}

		for (int i = start; i < end; ++i)
		{
			if (((String)lcol.get(i)).equals(":")) {
				continue;
			}
			
    		out.writeStartElement("qb:Observation");
    		
    		out.writeStartElement("qb:dataset");
    		out.writeAttribute("rdf:resource", "/id/" + id + "#ds");
    		// @@@ workaround to get query processor to function
    		//out.writeAttribute("rdf:resource", id + "#ds");

    		out.writeEndElement();

			for (int j = 0; j < hd1.size(); ++j) {
	    		out.writeStartElement((String)hd1.get(j));
	    		out.writeAttribute("rdf:resource", Dictionary.PREFIX + (String)hd1.get(j) + "#" + (String)ld1.get(j));
	    		out.writeEndElement();
			}

    		out.writeStartElement((String)h.getDim2());
    		out.writeAttribute("rdf:resource", Dictionary.PREFIX + (String)h.getDim2() + "#" + (String)hcol.get(i));
    		out.writeEndElement();

    		//http://purl.org/linked-data/sdmx/2009/measure#obsValue
    		out.writeStartElement("sdmx-measure:obsValue");
    		String val = (String)lcol.get(i);
    		String note = null;
    		if (val.indexOf(' ') > 0) {
    			note = val.substring(val.indexOf(' ')+1);
    			val = val.substring(0, val.indexOf(' '));
    			out.writeAttribute("rdf:datatype", Dictionary.PREFIX + "note#" + note);
    		}
    		out.writeCharacters(val);
    		out.writeEndElement();

    		out.writeEndElement();
		}
	}
}