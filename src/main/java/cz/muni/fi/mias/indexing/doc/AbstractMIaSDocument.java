package cz.muni.fi.mias.indexing.doc;

/**
 * Abstract implementation of the MIaSDocument which uses DocumentSource
 * 
 * @author Martin Liska
 */
public abstract class AbstractMIaSDocument implements MIaSDocument {
    
    protected DocumentSource source;

    public AbstractMIaSDocument(DocumentSource source) {
        this.source = source;
    }

    @Override
    public String getLogInfo() {
        return source.getDocumentSourcePath();
    }
}
