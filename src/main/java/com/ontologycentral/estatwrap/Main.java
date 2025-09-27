package com.ontologycentral.estatwrap;

import com.ontologycentral.estatwrap.convert.Cl;
import com.ontologycentral.estatwrap.convert.Cs;
import com.ontologycentral.estatwrap.convert.Da;
import com.ontologycentral.estatwrap.convert.Dc;
import com.ontologycentral.estatwrap.convert.Df;
import com.ontologycentral.estatwrap.convert.Ds;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Download and convert files without truncation.
 *
 * @author aharth
 */
public class Main {
    // Updated to use SDMX API
    public static String URI_PREFIX_21 = "https://ec.europa.eu/eurostat/api/dissemination/sdmx/2.1";
    public static String URI_PREFIX_3 = "https://ec.europa.eu/eurostat/api/dissemination/sdmx/3.0";

    // Thread-safe date formatters using ThreadLocal
    public static final ThreadLocal<SimpleDateFormat> ISO8601 = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        }
    };

    /** */
    public static void main(String[] args) throws IOException, XMLStreamException {
        Options options = new Options();

        Option outputO = new Option("o", "name of file to write, - for stdout");
        outputO.setArgs(1);
        options.addOption(outputO);




        Option csO = new Option("cs", "Eurostat dataset ID to get concept scheme as RDF (e.g., tag00038)");
        csO.setArgs(1);
        options.addOption(csO);

        Option clO = new Option("cl", "Eurostat code list ID to get code lists as RDF (e.g., FREQ for time frequencies, GEO for countries/regions)");
        clO.setArgs(1);
        options.addOption(clO);

        Option dsO = new Option("ds", "Eurostat dataset ID to get data structure definition as RDF (e.g., tag00038)");
        dsO.setArgs(1);
        options.addOption(dsO);

        Option dfO = new Option("df", "Eurostat dataset ID to get dataflow definition as RDF (e.g., tag00038)");
        dfO.setArgs(1);
        options.addOption(dfO);

        Option dcO = new Option("dc", "Eurostat dataset ID to get data constraint definition as RDF (e.g., tag00038)");
        dcO.setArgs(1);
        options.addOption(dcO);

        Option daO = new Option("da", "Eurostat dataset ID to get data observations using SDMX 3.0 API as RDF (e.g., tag00038)");
        daO.setArgs(1);
        options.addOption(daO);

        Option helpO = new Option("h", "help", false, "print help");
        options.addOption(helpO);

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);

            if (!(cmd.hasOption("i") || cmd.hasOption("cs") || cmd.hasOption("cl") || cmd.hasOption("ds") || cmd.hasOption("df") || cmd.hasOption("dc") || cmd.hasOption("da"))) {
                throw new ParseException("specify either -i, -cs, -cl, -ds, -df, -dc, or -da");
            }
        } catch (ParseException e) {
            System.err.println("***ERROR: " + e.getClass() + ": " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("parameters:", options);
            return;
        }

        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("parameters:", options);
            return;
        }

        PrintStream out = System.out;

        if (cmd.hasOption("o")) {
            if (cmd.getOptionValue("o").equals("-")) {
                out = System.out;
            } else {
                out = new PrintStream(new FileOutputStream(cmd.getOptionValue("o")));
            }
        }

        Da.MAX_COLS = Integer.MAX_VALUE;
        Da.MAX_ROWS = Integer.MAX_VALUE;

        String id = null;
        URL url = null;

        if (cmd.hasOption("i")) {
            id = cmd.getOptionValue("i");
            url = new URL(URI_PREFIX_21 + "/data/" + id + "/?format=TSV&compressed=true");
        } else if (cmd.hasOption("cs")) {
            id = cmd.getOptionValue("cs");
            url = new URL(Main.URI_PREFIX_3 + "/structure/conceptscheme/ESTAT/" + id);
        } else if (cmd.hasOption("cl")) {
            id = cmd.getOptionValue("cl");
            url = new URL(Main.URI_PREFIX_3 + "/structure/codelist/ESTAT/" + id.toUpperCase());
        } else if (cmd.hasOption("ds")) {
            id = cmd.getOptionValue("ds");
            url = new URL(Main.URI_PREFIX_3 + "/structure/datastructure/ESTAT/" + id);
        } else if (cmd.hasOption("df")) {
            id = cmd.getOptionValue("df");
            url = new URL(Main.URI_PREFIX_3 + "/structure/dataflow/ESTAT/" + id);
        } else if (cmd.hasOption("dc")) {
            id = cmd.getOptionValue("dc");
            url = new URL(Main.URI_PREFIX_3 + "/structure/dataconstraint/ESTAT/" + id);
        } else if (cmd.hasOption("da")) {
            id = cmd.getOptionValue("da");
            url = new URL(Main.URI_PREFIX_3 + "/data/dataflow/ESTAT/" + id + "/1.0?format=tsv&compress=false");
        }

        // Handle data and dictionary processing
        System.out.println(url);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        InputStream is = null;

        if (url.toString().contains("compressed=true")
                || url.toString().contains("compress=true")) {
            is = new GZIPInputStream(conn.getInputStream());
        } else {
            is = conn.getInputStream();
        }
        if (conn.getResponseCode() != 200) {
            throw new IOException("Response code: " + conn.getResponseCode());
        }

        String encoding = "UTF-8"; // Default to UTF-8 for SDMX API responses
        String contentType = conn.getContentType();
        if (contentType != null && contentType.contains("charset=")) {
            encoding = contentType.substring(contentType.indexOf("charset=") + 8);
            if (encoding.contains(";")) {
                encoding = encoding.substring(0, encoding.indexOf(";"));
            }
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(is, encoding));

        XMLOutputFactory factory = XMLOutputFactory.newInstance();

        XMLStreamWriter ch = factory.createXMLStreamWriter(out, "utf-8");

        if (cmd.hasOption("cs")) {
            try {
                Cs cs = new Cs();
                String xslPath = "src/main/webapp/WEB-INF/cs2rdf.xsl";
                cs.convert(in, out, xslPath);
            } catch (Exception e) {
                System.err.println("Error processing CS for " + id + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else if (cmd.hasOption("cl")) {
            try {
                Cl cl = new Cl();
                String xslPath = "src/main/webapp/WEB-INF/cl2rdf.xsl";
                cl.convert(in, out, xslPath);
            } catch (Exception e) {
                System.err.println("Error processing CL for " + id + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else if (cmd.hasOption("ds")) {
            try {
                Ds ds = new Ds();
                String xslPath = "src/main/webapp/WEB-INF/ds2rdf.xsl";
                ds.convert(in, out, xslPath);
            } catch (Exception e) {
                System.err.println("Error processing DS for " + id + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else if (cmd.hasOption("df")) {
            try {
                Df df = new Df();
                String xslPath = "src/main/webapp/WEB-INF/df2rdf.xsl";
                df.convert(in, out, xslPath);
            } catch (Exception e) {
                System.err.println("Error processing DF for " + id + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else if (cmd.hasOption("dc")) {
            try {
                Dc dc = new Dc();
                String xslPath = "src/main/webapp/WEB-INF/dc2rdf.xsl";
                dc.convert(in, out, xslPath);
            } catch (Exception e) {
                System.err.println("Error processing DC for " + id + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else if (cmd.hasOption("da")) {
            Da.convertWithSdmx3Identifiers(ch, new HashMap<String, String>(), id, in);
        }
        ch.close();

        out.close();
        in.close();
    }
}
