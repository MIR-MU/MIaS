/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.mias.indexing.doc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.lucene.document.*;

/**
 * File implementation of the DocumentSource.
 * 
 * @author Martin Liska
 */
public class FileDocument implements DocumentSource {
    
    private File file;
    private String path;

    /**
     * @param file File from which this DocumentSource is created.
     * @param path Relative path to the file.
     */
    public FileDocument(File file, String path) {
        this.file = file;
        this.path = path;
    }
    
    @Override
    public InputStream resetStream() throws IOException {
       return new FileInputStream(file); 
    }
    
    /**
     * Creates Lucene document with the fields:
     * <ul>
     *  <li>path: relative path from the constructor</li>
     *  <li>id: the same as path</li>
     *  <li>modified: last modified date of the file</li>
     *  <li>filesize: size of the file</li>
     *  <li>title: name of the file</li>
     * </ul>
     * @return New Lucene document.
     */
    @Override
    public Document createDocument() {
        Document doc = new Document();

        doc.add(new StringField("path", path, Field.Store.YES));
        
        doc.add(new StringField("id", path, Field.Store.YES));

        doc.add(new StringField("modified",
                DateTools.timeToString(file.lastModified(), DateTools.Resolution.MINUTE),
                Field.Store.YES));

        // Multiple values for the same field in one document is allowed.
        String filesize = "filesize";
        // for exact / range queries
        doc.add(new LongPoint(filesize, file.length()));
        // for storing the value
        doc.add(new StoredField(filesize, file.length()));
        // for sorting / scoring / faceting
        doc.add(new NumericDocValuesField(filesize, file.length()));

        doc.add(new TextField("title", file.getName(), Field.Store.YES));
        return doc;
    }

    @Override
    public String getDocumentSourcePath() {
        return path;
    }

}
