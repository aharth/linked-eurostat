package com.ontologycentral.estatwrap.convert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class DataSdmx3 {
    Logger _log = Logger.getLogger(this.getClass().getName());

    public static String PREFIX = "http://ontologycentral.com/2009/01/eurostat/ns#";

    BufferedReader _in;

    // here, use a threshold to limit the amount of data converted (GAE limitations)
    // added * 8 to remove restriction
    public static int MAX_COLS = 8 * 10;
    public static int MAX_ROWS = 1024 * 8 * 10;

    /** */
    public DataSdmx3(Reader sr) throws IOException {
        _in = new BufferedReader(sr);
    }

    /** */
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
                    if (hd1.get(j).equals("time")) {
                        String time = convertTime(ld1.get(j));
                        out.writeStartElement("dcterms:date");
                        out.writeCharacters(time);
                        out.writeEndElement();
                    } else {
                        out.writeStartElement(hd1.get(j));
                        out.writeAttribute(
                                "rdf:resource", getSdmx3IdentifierUri(hd1.get(j), ld1.get(j)));
                        out.writeEndElement();
                    }
                }

                if (h.getDim2().equals("time")) {
                    String time = convertTime(hcol.get(i));
                    out.writeStartElement("dcterms:date");
                    out.writeCharacters(time);
                    out.writeEndElement();
                } else {
                    out.writeStartElement(h.getDim2());
                    out.writeAttribute(
                            "rdf:resource", getSdmx3IdentifierUri(h.getDim2(), hcol.get(i)));
                    out.writeEndElement();
                }

                // http://purl.org/linked-data/sdmx/2009/measure#obsValue
                // do not print empty values
                String val = (String) lcol.get(i).trim();
                if (!val.equals("")) {
                    out.writeStartElement("sdmx-measure:obsValue");
                    if (isNumeric(val)) {
                        out.writeAttribute("rdf:datatype", "http://www.w3.org/2001/XMLSchema#decimal");
                    }
                    out.writeCharacters(val);
                    out.writeEndElement();
                }

                // Handle status flags - extract flag from value if present
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

    /**
     * Generate SDMX 3.0 compatible identifier URIs using /cl, /cs, /ds, /df, /dc patterns
     */
    private String getSdmx3IdentifierUri(String dimension, String value) {
        // Map dimension types to appropriate URI patterns
        String prefix;
        switch (dimension.toLowerCase()) {
            case "freq":
            case "geo":
            case "dairyprod":
            case "milkitem":
            case "unit":
            case "age":
            case "sex":
            case "isced11":
            case "citizen":
            case "c_birth":
            case "duration":
                // These are typically code lists
                prefix = "../cl/";
                break;
            case "time_period":
                // Time periods are special - keep as concept scheme
                prefix = "../cs/";
                break;
            default:
                // Default to code list for unknown dimensions
                prefix = "../cl/";
                break;
        }

        return prefix + dimension + "#" + value;
    }

    /**
     * Check if a string represents a numeric value
     */
    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Extract status flag from the original line (flags like 'p', 'e', 'b', etc.)
     */
    private String extractStatusFlag(String line, int columnIndex) {
        // TSV format often has flags after the value, separated by space
        String[] parts = line.split("\t");
        if (parts.length > columnIndex + 1) {
            String cellValue = parts[columnIndex + 1]; // Adjust for header offset
            if (cellValue.contains(" ")) {
                String[] valueParts = cellValue.split(" ");
                if (valueParts.length > 1) {
                    return valueParts[1].trim();
                }
            }
        }
        return null;
    }

    /**
     * Convert status flags to descriptive text
     */
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

    /**
     * Convert time format (copied from original Data class)
     */
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
}