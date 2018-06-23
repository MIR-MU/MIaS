/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.mias.indexing.doc;

import cz.muni.fi.mias.MIaSUtils;
import cz.muni.fi.mias.math.MathTokenizer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Implementation of MIaSDocument that creates one Lucene document for each math formula in the input file.
 * 
 * @author Martin Liska
 */
public class FormulaDocument extends AbstractMIaSDocument {    
    private static final Logger LOG = LogManager.getLogger(FormulaDocument.class);
    public FormulaDocument(DocumentSource source) {
        super(source);
    }

    @Override
    public List<Document> getDocuments() throws IOException {
        List<Document> result = new ArrayList<>();
        try {
            DocumentBuilder builder = MIaSUtils.prepareDocumentBuilder();
            org.w3c.dom.Document document = builder.parse(source.resetStream());
            NodeList list = document.getElementsByTagNameNS("*", "math");
            for (int i = 0; i < list.getLength(); i++) {
                Node item = list.item(i);
                String id;
                Node namedItem = item.getAttributes().getNamedItem("id");
                if (namedItem != null) {
                    id = namedItem.getNodeValue();
                } else {
                    id = String.valueOf(i);
                }
                Document doc = source.createDocument();
                id = doc.get("id") + "#" + id;
                doc.removeField("id");
                doc.add(new StringField("id", id, Field.Store.YES));
                doc.removeField("title");
                doc.add(new TextField("title", id, Field.Store.YES));
                
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Source xmlSource = new DOMSource(item);
                Result outputTarget = new StreamResult(outputStream);
                TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);

                byte[] bytes = outputStream.toByteArray();
                InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(bytes), "UTF-8");
                MathTokenizer mathTokenizer = new MathTokenizer(isr, true, MathTokenizer.MathMLType.PRESENTATION);
                mathTokenizer.setFormulaPosition(i+1);
                doc.add(new TextField("pmath", mathTokenizer));

                isr = new InputStreamReader(new ByteArrayInputStream(bytes), "UTF-8");
                MathTokenizer mathTokenizer1 = new MathTokenizer(isr, true, MathTokenizer.MathMLType.CONTENT);
                mathTokenizer1.setFormulaPosition(i+1);
                doc.add(new TextField("cmath", mathTokenizer1));
                result.add(doc);
            }
        } catch (TransformerException | SAXException | ParserConfigurationException ex) {
            LOG.fatal(ex);
        }
        return result;
    }
    
}
