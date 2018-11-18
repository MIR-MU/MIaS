package cz.muni.fi.mias.indexing.doc;

import cz.muni.fi.mias.math.MathTokenizer;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.document.*;

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
            // Lucene API change: Index-time boosts are not supported anymore.
            // https://lucene.apache.org/core/7_0_0/MIGRATE.html
            // https://issues.apache.org/jira/browse/LUCENE-6819
            FloatDocValuesField titleBoostField = new FloatDocValuesField("title", 10.0f);
            document.add(titleField);
            document.add(titleBoostField);
        } else {
            // assign default boost value
            FloatDocValuesField titleBoostField = new FloatDocValuesField("title", 1.0f);
            document.add(titleBoostField);
        }

        // TODO 'authors' field is not used anywhere??
        String authors = htmldoc.getAuthors();
        if (authors != null) {
            Field authorsField = new StringField("authors", authors, Field.Store.YES);
            FloatDocValuesField authorsBoostField = new FloatDocValuesField("authors", 10.0f);
            document.add(authorsField);
            document.add(authorsBoostField);
        } else {
            // assign default boost value
            FloatDocValuesField authorsBoostField = new FloatDocValuesField("authors", 1.0f);
            document.add(authorsBoostField);
        }
        
        String content = htmldoc.getBody();
        if (content != null) {
            document.add(new TextField("content", content, Field.Store.NO));
        }

        // assign default boost value (in any case)
        FloatDocValuesField contentBoostField = new FloatDocValuesField("content", 1.0f);
        document.add(contentBoostField);

        InputStreamReader isr = new InputStreamReader(source.resetStream(), "UTF-8");
        document.add(new TextField("pmath", new MathTokenizer(isr, true, MathTokenizer.MathMLType.PRESENTATION)));
        isr = new InputStreamReader(source.resetStream(), "UTF-8");
        document.add(new TextField("cmath", new MathTokenizer(isr, true, MathTokenizer.MathMLType.CONTENT)));
        
        return Arrays.asList(document);
    }

}
