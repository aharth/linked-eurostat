package com.ontologycentral.estatwrap.convert;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class ToC {
    Logger _log = Logger.getLogger(this.getClass().getName());

    InputStream _in;

    public ToC(InputStream is, String encoding) throws IOException {
        _in = is;
    }

    public Map<String, String> convert() throws IOException, XMLStreamException {
        Map<String, String> toc = new HashMap<String, String>();

        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(_in);

        String currentCode = null;
        String currentTitle = null;
        boolean inCode = false;
        boolean inTitle = false;
        boolean titleIsEnglish = false;

        try {
            while (reader.hasNext()) {
                int event = reader.next();

                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        String localName = reader.getLocalName();

                        if ("code".equals(localName)) {
                            inCode = true;
                        } else if ("title".equals(localName)) {
                            String language = reader.getAttributeValue(null, "language");
                            if ("en".equals(language)) {
                                inTitle = true;
                                titleIsEnglish = true;
                            } else {
                                titleIsEnglish = false;
                            }
                        }
                        break;

                    case XMLStreamConstants.CHARACTERS:
                        String text = reader.getText().trim();
                        if (!text.isEmpty()) {
                            if (inCode) {
                                currentCode = text;
                            } else if (inTitle && titleIsEnglish) {
                                currentTitle = text;
                            }
                        }
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        String endLocalName = reader.getLocalName();

                        if ("code".equals(endLocalName)) {
                            inCode = false;
                        } else if ("title".equals(endLocalName)) {
                            inTitle = false;
                        } else if ("leaf".equals(endLocalName)) {
                            if (currentCode != null && currentTitle != null) {
                                toc.put(currentCode, currentTitle);
                            }
                            currentCode = null;
                            currentTitle = null;
                        }
                        break;
                }
            }
        } finally {
            reader.close();
            _in.close();
        }

        return toc;
    }
}
