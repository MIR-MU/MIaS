package cz.muni.fi.mias.indexing.doc;

import cz.muni.fi.mias.math.MathTokenizer;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

/**
 *
 * MIaSDocument implementation for creating Lucene document from xhtml and html files.
 *
 * @author Martin Liska
 */
public class HtmlDocument extends AbstractMIaSDocument {
        
    public HtmlDocument(DocumentSource source) {
        super(source);
    }

    @Override
    public List<Document> getDocuments() throws IOException {
        Document document = source.createDocument();
        
        HtmlDocumentExtractor htmldoc = new HtmlDocumentExtractor(source.resetStream());

        String arxivId = htmldoc.getArxivId();
        if (arxivId != null) {
            document.removeField("id");
            Field arxivIdField = new StringField("id", arxivId, Field.Store.YES);
            document.add(arxivIdField);
        }

        String title = htmldoc.getTitle();
        if (title != null) {
            document.removeField("title");
            Field titleField = new TextField("title", title, Field.Store.YES);
            titleField.setBoost(Float.parseFloat("10.0"));
            document.add(titleField);
        }

        String authors = htmldoc.getAuthors();
        if (authors != null) {
            Field authorsField = new StringField("authors", authors, Field.Store.YES);
            authorsField.setBoost(Float.parseFloat("10.0"));
            document.add(authorsField);
        }
        
        String content = htmldoc.getBody();
        if (content != null) {
            document.add(new TextField("content", content, Field.Store.NO));
        }

        InputStreamReader isr = new InputStreamReader(source.resetStream(), "UTF-8");
        document.add(new TextField("pmath", new MathTokenizer(isr, true, MathTokenizer.MathMLType.PRESENTATION)));
        isr = new InputStreamReader(source.resetStream(), "UTF-8");
        document.add(new TextField("cmath", new MathTokenizer(isr, true, MathTokenizer.MathMLType.CONTENT)));
        
        return Arrays.asList(document);
    }

}
