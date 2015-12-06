/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.mias;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utilities class.
 *
 * @author Martin Liska
 */
public class MIaSUtils {

    private static final String MATHML_DTD = "/cz/muni/fi/mias/math/xhtml-math11-f.dtd";

    /**
     * Extracts string content from a reader.
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String getContent(InputStream is) throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder builder = new StringBuilder(1000);
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            builder.append(readData);
        }
        return builder.toString();
    }

    public static DocumentBuilder prepareDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver(new EntityResolver() {

            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, java.io.IOException {
                if (systemId.endsWith("dtd")) {
                    return new InputSource(MIaSUtils.class.getResourceAsStream(MATHML_DTD));
                } else {
                    return null;
                }
            }
        });
        return builder;
    }
}
