package cz.muni.fi.mias.indexing.doc;

import java.io.IOException;
import java.io.InputStream;
import org.apache.lucene.document.Document;

/**
 * DocumentSource is a source an indexed document can be from. Its stream can be reset and also provides creation of the default Lucene document 
 * for this source.
 * 
 * @author mato
 */
public interface DocumentSource {
    
    public InputStream resetStream() throws IOException;
    
    public Document createDocument();

    public String getDocumentSourcePath();
        
}
