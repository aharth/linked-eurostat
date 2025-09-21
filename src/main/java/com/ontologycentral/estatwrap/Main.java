package com.ontologycentral.estatwrap;

import com.ontologycentral.estatwrap.convert.Data;
import com.ontologycentral.estatwrap.convert.DataPage;
import com.ontologycentral.estatwrap.convert.Dictionary;
import com.ontologycentral.estatwrap.convert.Dsd;
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

    public static SimpleDateFormat RFC822 =
            new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);
    public static SimpleDateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    /** */
    public static void main(String[] args) throws IOException, XMLStreamException {
        Options options = new Options();

        Option outputO = new Option("o", "name of file to write, - for stdout");
        outputO.setArgs(1);
        options.addOption(outputO);

        Option idO = new Option("i", "name of Eurostat id (e.g., tag00038)");
        idO.setArgs(1);
        options.addOption(idO);

        Option dicO = new Option("d", "name of Eurostat dic (e.g., freq)");
        dicO.setArgs(1);
        options.addOption(dicO);

        Option dsdO = new Option("s", "name of Eurostat dataset for DSD (e.g., tag00038)");
        dsdO.setArgs(1);
        options.addOption(dsdO);

        Option helpO = new Option("h", "help", false, "print help");
        options.addOption(helpO);

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);

            if (!(cmd.hasOption("i") || cmd.hasOption("d") || cmd.hasOption("s"))) {
                throw new ParseException("specify either -i, -d, or -s");
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

        Data.MAX_COLS = Integer.MAX_VALUE;
        Data.MAX_ROWS = Integer.MAX_VALUE;

        String id = null;
        URL url = null;

        if (cmd.hasOption("i")) {
            id = cmd.getOptionValue("i");
            url = new URL(URI_PREFIX_21 + "/data/" + id + "/?format=TSV&compressed=true");
        } else if (cmd.hasOption("d")) {
            id = cmd.getOptionValue("d");
            url =
                    new URL(
                            URI_PREFIX_3
                                    + "/structure/codelist/ESTAT/"
                                    + id.toUpperCase()
                                    + "/?compress=true&format=TSV&formatVersion=2.0");
        } else if (cmd.hasOption("s")) {
            id = cmd.getOptionValue("s");
            // DSD will be handled separately, no URL needed here
            url = null;
        }

        // Handle DSD separately since it uses different processing
        if (cmd.hasOption("s")) {
            try {
                Dsd dsd = new Dsd(id);
                // For Main.java, we need to provide the XSLT path.
                // Assuming dsd2rdf.xsl is available in the classpath or current directory
                String xslPath = "src/main/webapp/WEB-INF/dsd2rdf.xsl";
                dsd.convert(out, xslPath);
            } catch (Exception e) {
                System.err.println("Error processing DSD for " + id + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else {
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

            String encoding = conn.getContentEncoding();
            if (encoding == null) {
                encoding = "ISO-8859-1";
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(is, encoding));

            XMLOutputFactory factory = XMLOutputFactory.newInstance();

            XMLStreamWriter ch = factory.createXMLStreamWriter(out, "utf-8");

            if (cmd.hasOption("i")) {
                DataPage.convert(ch, new HashMap<String, String>(), id, in);
            } else if (cmd.hasOption("d")) {
                if ("geo".equals(id)) {
                    System.err.println("not supported right now");
                } else {
                    Dictionary dict = new Dictionary(in, id);
                    dict.convert(ch, id);
                }
            }

            ch.close();
        }

        out.close();
    }
}
