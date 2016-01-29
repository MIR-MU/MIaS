/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.mias.indexing.doc;

import cz.muni.fi.mias.MIaSUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Extracts information from HTML input.
 * 
 * @author Martin Liska
 */
public class HtmlDocumentExtractor {
    private static final Logger LOG = LogManager.getLogger(HtmlDocumentExtractor.class);
    private Element rawDoc;

    public HtmlDocumentExtractor(InputStream is) {
        parseDoc(is);
    }

    public HtmlDocumentExtractor(File file) {
        try {
            parseDoc(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            LOG.fatal(ex);
        }
    }
        
    private void parseDoc(InputStream is) {
        try {
            DocumentBuilder prepareDocumentBuilder = MIaSUtils.prepareDocumentBuilder();
            org.w3c.dom.Document root = prepareDocumentBuilder.parse(is);
            rawDoc = root.getDocumentElement();
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            LOG.fatal(ex);
        }
    }

    /**
     * 
     * @return Title of the document. It is either the content of <title></title> element or from the first <h1></h1> docuemnt.
     */
    public String getTitle() {
        if (rawDoc == null) {
            return null;
        }

        String title = null;

        NodeList nl = rawDoc.getElementsByTagName("title");
        if (nl.getLength() > 0) {
            title = getTextContent(nl.item(0));
        }
        if (title == null || title.equals("")) {
            title = getMetaAttribute("citation_title");
        }
        if (title == null || title.equals("")) {
            NodeList list = rawDoc.getElementsByTagName("h1");
            if (list.getLength() > 0) {
                title = getTextContent(list.item(0));
            }
        }
        return title;
    }
    
    private String getTextContent(Node n) {
        String result = null;
        Element titleElement = ((Element) n);
        Node firstChild = titleElement.getFirstChild();
        if (firstChild != null) {
            if (firstChild instanceof Text) {
                Text text = (Text) firstChild;                
                result = text.getData();
            } else {
                result = firstChild.getTextContent();
            }
        }
        return result;
    }

    /**
     * 
     * @return content of citation_arxiv_id attribute of meta element
     */
    public String getArxivId() {
        return getMetaAttribute("citation_arxiv_id");
    }

    /**
     * 
     * @return content of citation_authors attribute of meta element
     */
    public String getAuthors() {
        return getMetaAttribute("citation_authors");
    }

    private String getMetaAttribute(String metaName) {
        if (rawDoc == null) {
            return null;
        }

        String metaContent = null;

        NodeList meta = rawDoc.getElementsByTagName("meta");
        for (int i = 0; i < meta.getLength(); i++) {
            Node n = meta.item(i);
            NamedNodeMap nnm = n.getAttributes();
            Node nameNode = nnm.getNamedItem("name");
            if (nameNode != null && nameNode.getNodeValue().equals(metaName)) {
                metaContent = nnm.getNamedItem("content").getNodeValue();
                return metaContent;
            }
        }
        return metaContent;
    }

    /**
     * 
     * @return text content of the <body></body> element
     */
    public String getBody() {
        if (rawDoc == null) {
            return null;
        }

        String body = "";
        NodeList nl = rawDoc.getElementsByTagName("body");
        if (nl.getLength() > 0) {
            body = getBodyText(nl.item(0));
        }
        return body;
    }

    private String getBodyText(Node node) {
        NodeList nl = node.getChildNodes();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < nl.getLength(); i++) {
            Node child = nl.item(i);
            switch (child.getNodeType()) {
                case Node.ELEMENT_NODE:
                    if (!node.getNodeName().endsWith("math")) {
                        buffer.append(getBodyText(child));
                        buffer.append(" ");
                    }
                    break;
                case Node.TEXT_NODE:
                    buffer.append(((Text) child).getData());
                    break;
            }
        }
        return buffer.toString();
    }

}
