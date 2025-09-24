package com.ontologycentral.estatwrap.convert;

import com.ontologycentral.estatwrap.Main;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class Da {
    Logger _log = Logger.getLogger(this.getClass().getName());

    BufferedReader _in;

    public static int MAX_COLS = 8 * 10;
    public static int MAX_ROWS = 1024 * 8 * 10;

    public Da(Reader sr) throws IOException {
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

            if (rows > MAX_ROWS) {
                System.err.println("MAX_ROWS limit reached: " + MAX_ROWS);
                break;
            }

            l = new Line(line);

            List<String> hd1 = h.getDim1();
            List<String> ld1 = l.getDim1();

            List<String> hcol = h.getCols();
            List<String> lcol = l.getCols();

            if (hcol.size() > MAX_COLS) {
                System.err.println("MAX_COLS limit reached: " + MAX_COLS);
                hcol = hcol.subList(0, MAX_COLS);
                lcol = lcol.subList(0, MAX_COLS);
            }

            for (int i = 0; i < hcol.size(); i++) {
                if (lcol.get(i).toString().trim().length() <= 0) {
                    continue;
                }

                out.writeStartElement("qb:Observation");

                out.writeStartElement("qb:dataSet");
                out.writeAttribute("rdf:resource", "../id/" + id + "#ds");
                out.writeEndElement();

                for (int j = 0; j < hd1.size(); j++) {
                    out.writeStartElement("dim:dim-" + hd1.get(j));
                    if (hd1.get(j).equals("time")) {
                        String time = convertTime(ld1.get(j));
                        out.writeCharacters(time);
                    } else if (hd1.get(j).equals("TIME_PERIOD")) {
                        out.writeCharacters(ld1.get(j));
                    } else {
                        out.writeAttribute("rdf:resource", getSdmx3IdentifierUri(hd1.get(j), ld1.get(j)));
                    }
                    out.writeEndElement();
                }

                out.writeStartElement("dim:dim-" + h.getDim2());
                if (h.getDim2().equals("time")) {
                    String time = convertTime(hcol.get(i));
                    out.writeCharacters(time);
                } else if (h.getDim2().equals("TIME_PERIOD")) {
                    out.writeCharacters(hcol.get(i));
                } else {
                    out.writeAttribute("rdf:resource", getSdmx3IdentifierUri(h.getDim2(), hcol.get(i)));
                }
                out.writeEndElement();

                String val = (String) lcol.get(i).trim();
                if (!val.equals("")) {
                    out.writeStartElement("sdmx-measure:obsValue");
                    if (isNumeric(val)) {
                        out.writeAttribute("rdf:datatype", "http://www.w3.org/2001/XMLSchema#decimal");
                    }
                    out.writeCharacters(val);
                    out.writeEndElement();
                }

                String status = extractStatusFlag(line, i);
                if (status != null && !status.isEmpty()) {
                    out.writeStartElement("rdfs:comment");
                    out.writeCharacters(getStatusDescription(status));
                    out.writeEndElement();
                }

                out.writeEndElement();
            }
        }
    }

    public void convert(Writer out, String id) throws IOException {
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

        int obsCount = 0;
        while ((line = _in.readLine()) != null) {
            ++rows;
            line = line.trim();
            if (line.length() <= 0) {
                continue;
            }

            if (rows > MAX_ROWS) {
                System.err.println("MAX_ROWS limit reached: " + MAX_ROWS);
                break;
            }

            l = new Line(line);

            List<String> hd1 = h.getDim1();
            List<String> ld1 = l.getDim1();

            List<String> hcol = h.getCols();
            List<String> lcol = l.getCols();

            if (hcol.size() > MAX_COLS) {
                System.err.println("MAX_COLS limit reached: " + MAX_COLS);
                hcol = hcol.subList(0, MAX_COLS);
                lcol = lcol.subList(0, MAX_COLS);
            }

            for (int i = 0; i < hcol.size(); i++) {
                if (lcol.get(i).toString().trim().length() <= 0) {
                    continue;
                }

                String obsId = "_:obs" + (++obsCount);

                out.write(obsId + "\n");
                out.write("    a qb:Observation ;\n");
                out.write("    qb:dataSet <../id/" + id + "#ds> ;\n");

                for (int j = 0; j < hd1.size(); j++) {
                    out.write("    dim:dim-" + hd1.get(j) + " ");
                    if (hd1.get(j).equals("time")) {
                        String time = convertTime(ld1.get(j));
                        out.write("\"" + escapeString(time) + "\" ;\n");
                    } else if (hd1.get(j).equals("TIME_PERIOD")) {
                        out.write("\"" + escapeString(ld1.get(j)) + "\" ;\n");
                    } else {
                        out.write("<" + getSdmx3IdentifierUri(hd1.get(j), ld1.get(j)) + "> ;\n");
                    }
                }

                out.write("    dim:dim-" + h.getDim2() + " ");
                if (h.getDim2().equals("time")) {
                    String time = convertTime(hcol.get(i));
                    out.write("\"" + escapeString(time) + "\" ;\n");
                } else if (h.getDim2().equals("TIME_PERIOD")) {
                    out.write("\"" + escapeString(hcol.get(i)) + "\" ;\n");
                } else {
                    out.write("<" + getSdmx3IdentifierUri(h.getDim2(), hcol.get(i)) + "> ;\n");
                }

                String val = (String) lcol.get(i).trim();
                if (!val.equals("")) {
                    out.write("    sdmx-measure:obsValue ");
                    if (isNumeric(val)) {
                        out.write("\"" + escapeString(val) + "\"^^<http://www.w3.org/2001/XMLSchema#decimal>");
                    } else {
                        out.write("\"" + escapeString(val) + "\"");
                    }

                    String status = extractStatusFlag(line, i);
                    if (status != null && !status.isEmpty()) {
                        out.write(" ;\n");
                        out.write("    rdfs:comment \"" + escapeString(getStatusDescription(status)) + "\"");
                    }
                    out.write(" .\n");
                } else {
                    out.write("\n");
                }

                out.write("\n");
            }
        }
    }

    private String getSdmx3IdentifierUri(String dimension, String value) {
        String codeListId = switch (dimension.toLowerCase()) {
            case "c_birth" -> "c_birth";
            case "na_item" -> "na_item";
            case "ppp_cat" -> "ppp_cat";
            case "obs_flag" -> "obs_flag";
            case "conf_status" -> "conf_status";
            default -> dimension.toLowerCase();
        };

        return "../cl/" + codeListId + "#code-" + value;
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String extractStatusFlag(String line, int columnIndex) {
        String[] parts = line.split("\t");
        if (parts.length > columnIndex + 1) {
            String cellValue = parts[columnIndex + 1];
            if (cellValue.contains(" ")) {
                String[] valueParts = cellValue.split(" ");
                if (valueParts.length > 1) {
                    return valueParts[1].trim();
                }
            }
        }
        return null;
    }

    private String getStatusDescription(String flag) {
        switch (flag) {
            case "p": return "p (provisional)";
            case "e": return "e (estimated)";
            case "b": return "b (break in time series)";
            case "f": return "f (forecast)";
            case "s": return "s (Eurostat estimate)";
            case "c": return "c (confidential)";
            case "u": return "u (unreliable)";
            case "n": return "n (not significant)";
            case "z": return "z (not applicable)";
            default: return flag;
        }
    }

    private String convertTime(String time) {
        if (time.equals(":")) {
            return "";
        }

        if (time.length() == 4) {
            return time + "-01-01T00:00:00";
        }

        if (time.length() == 5) {
            if (time.charAt(4) == 'Q') {
                return time.substring(0, 4) + "-01-01T00:00:00";
            }
        }

        if (time.length() == 6) {
            if (time.charAt(4) == 'Q') {
                String year = time.substring(0, 4);
                String quarter = time.substring(5, 6);

                if (quarter.equals("1")) {
                    return year + "-01-01T00:00:00";
                } else if (quarter.equals("2")) {
                    return year + "-04-01T00:00:00";
                } else if (quarter.equals("3")) {
                    return year + "-07-01T00:00:00";
                } else if (quarter.equals("4")) {
                    return year + "-10-01T00:00:00";
                }
            }
        }

        if (time.length() == 7 && time.charAt(4) == 'M') {
            String year = time.substring(0, 4);
            String month = time.substring(5, 7);

            if (month.length() == 2) {
                return year + "-" + month + "-01T00:00:00";
            }
        }

        return time;
    }

    private String escapeString(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    public static void convertWithSdmx3Identifiers(XMLStreamWriter ch, Map<String, String> toc, String id, Reader in)
            throws XMLStreamException, IOException {
        ch.writeStartDocument("utf-8", "1.0");

        ch.writeStartElement("rdf:RDF");
        ch.writeNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        ch.writeNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        ch.writeNamespace("foaf", "http://xmlns.com/foaf/0.1/");
        ch.writeNamespace("qb", "http://purl.org/linked-data/cube#");
        ch.writeNamespace("sdmx-measure", "http://purl.org/linked-data/sdmx/2009/measure#");
        ch.writeNamespace("dcterms", "http://purl.org/dc/terms/");
        ch.writeNamespace("dim", "../ds/" + id + "#");

        ch.writeStartElement("rdf:Description");
        ch.writeAttribute("rdf:about", "");

        ch.writeStartElement("rdfs:comment");
        ch.writeCharacters("No guarantee of correctness! USE AT YOUR OWN RISK!");
        ch.writeEndElement();

        ch.writeStartElement("dcterms:publisher");
        ch.writeCharacters(
                "Eurostat (http://epp.eurostat.ec.europa.eu/) via Linked Eurostat (http://estatwrap.ontologycentral.com/)");
        ch.writeEndElement();

        ch.writeStartElement("rdfs:seeAlso");
        ch.writeAttribute(
                "rdf:resource", "http://estatwrap.ontologycentral.com/table_of_contents.rdf");
        ch.writeEndElement();

        ch.writeStartElement("dcterms:date");
        ch.writeCharacters(Main.ISO8601.format(new java.util.Date()));
        ch.writeEndElement();

        ch.writeEndElement();

        ch.writeStartElement("qb:DataSet");
        ch.writeAttribute("rdf:about", "../id/" + id + "#ds");

        ch.writeStartElement("rdfs:comment");
        ch.writeCharacters(
                "Source: Eurostat (http://epp.eurostat.ec.europa.eu/) via Linked Eurostat (http://estatwrap.ontologycentral.com/).");
        ch.writeEndElement();

        ch.writeStartElement("rdfs:seeAlso");
        ch.writeAttribute(
                "rdf:resource",
                "http://epp.eurostat.ec.europa.eu/portal/page/portal/about_eurostat/corporate/copyright_licence_policy");
        ch.writeEndElement();

        ch.writeStartElement("rdfs:seeAlso");
        ch.writeAttribute("rdf:resource", "http://ontologycentral.com/2009/01/eurostat/");
        ch.writeEndElement();

        ch.writeStartElement("foaf:page");
        ch.writeAttribute("rdf:resource", "");
        ch.writeEndElement();

        ch.writeStartElement("qb:structure");
        ch.writeAttribute("rdf:resource", "../ds/" + id + "#dsd");
        ch.writeEndElement();

        ch.writeEndElement();

        Da d = new Da(in);
        d.convert(ch, id);

        ch.writeEndElement();
        ch.writeEndDocument();
    }

    public static void convertWithSdmx3IdentifiersToTurtle(OutputStream os, Map<String, String> toc, String id, Reader in)
            throws IOException {
        Writer writer = new OutputStreamWriter(os, "utf-8");

        writer.write("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n");
        writer.write("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n");
        writer.write("@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n");
        writer.write("@prefix qb: <http://purl.org/linked-data/cube#> .\n");
        writer.write("@prefix sdmx-measure: <http://purl.org/linked-data/sdmx/2009/measure#> .\n");
        writer.write("@prefix dcterms: <http://purl.org/dc/terms/> .\n");
        writer.write("@prefix prov: <http://www.w3.org/ns/prov#> .\n");
        writer.write("@prefix dim: <../ds/" + id + "#> .\n");
        writer.write("\n");

        writer.write("<>\n");
        writer.write("    rdfs:comment \"No guarantee of correctness! USE AT YOUR OWN RISK!\" ;\n");
        writer.write("    dcterms:publisher \"Eurostat (http://epp.eurostat.ec.europa.eu/) via Linked Eurostat (http://estatwrap.ontologycentral.com/)\" ;\n");
        writer.write("    rdfs:seeAlso <http://estatwrap.ontologycentral.com/table_of_contents.rdf> ;\n");
        writer.write("    dcterms:date \"" + Main.ISO8601.format(new java.util.Date()) + "\" ;\n");
        writer.write("    prov:wasGeneratedBy <#transformation> .\n");
        writer.write("\n");

        writer.write("<../id/" + id + "#ds>\n");
        writer.write("    a qb:DataSet ;\n");
        writer.write("    rdfs:comment \"Source: Eurostat (http://epp.eurostat.ec.europa.eu/) via Linked Eurostat (http://estatwrap.ontologycentral.com/).\" ;\n");
        writer.write("    rdfs:seeAlso <http://epp.eurostat.ec.europa.eu/portal/page/portal/about_eurostat/corporate/copyright_licence_policy> ;\n");
        writer.write("    rdfs:seeAlso <../df/" + id + "#df-" + id + "> ;\n");
        writer.write("    foaf:page <> ;\n");
        writer.write("    qb:structure <../ds/" + id + "#dsd> .\n");
        writer.write("\n");

        writer.write("<#transformation>\n");
        writer.write("    a prov:Activity ;\n");
        writer.write("    rdfs:label \"SDMX to RDF Data Transformation\" ;\n");
        writer.write("    prov:used <https://ec.europa.eu/eurostat/api/dissemination/sdmx/3.0/data/dataflow/ESTAT/" + id + "/1.0?format=tsv&compress=false> ;\n");
        writer.write("    prov:wasAssociatedWith <#estatwrap> ;\n");
        writer.write("    dcterms:date \"" + Main.ISO8601.format(new java.util.Date()) + "\" .\n");
        writer.write("\n");

        writer.write("<#estatwrap>\n");
        writer.write("    a prov:SoftwareAgent ;\n");
        writer.write("    rdfs:label \"Linked Eurostat (estatwrap)\" ;\n");
        writer.write("    foaf:homepage <http://estatwrap.ontologycentral.com/> ;\n");
        writer.write("    dcterms:description \"Service for converting Eurostat SDMX data to RDF\" .\n");
        writer.write("\n");

        Da d = new Da(in);
        d.convert(writer, id);

        writer.flush();
    }
}