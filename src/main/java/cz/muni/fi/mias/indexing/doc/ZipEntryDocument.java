/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.mias.indexing.doc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

/**
 * Zip file implementation of the DocumentSource.
 * 
 * @author Martin Liska
 */
public class ZipEntryDocument implements DocumentSource {
    
    private String path;
    private ZipEntry zipEntry;
    private ZipFile zipFile;

    /**
     * 
     * @param zipFile Zip compressed file
     * @param path Relative path to the file
     * @param zipEntry Zip file entry from which the Lucene document will be created.
     */
    public ZipEntryDocument(ZipFile zipFile, String path, ZipEntry zipEntry) {
        this.path = path;
        this.zipFile = zipFile;
        this.zipEntry = zipEntry;
    }

    @Override
    public InputStream resetStream() throws IOException {
        return zipFile.getInputStream(zipEntry);
    }

    /**
     * Creates Lucene document for the zip file entry with the fields:
     * <ul>
     *  <li>path: relative path from the constructor</li>
     *  <li>id: relative path + path within the zip file</li>
     *  <li>modified: last modified date of the entry</li>
     *  <li>filesize: size of the entry</li>
     *  <li>title: file name of the entry</li>
     *  <li>archivepath: file name of the entry</li>
     * </ul>
     * @return New Lucene document.
     */
    @Override
    public Document createDocument() {
        Document doc = new Document();
        doc.add(new StringField("path", path, Field.Store.YES));
        doc.add(new StringField("id", path + File.separator+zipEntry.getName(), Field.Store.YES));
        doc.add(new StringField("modified",
                DateTools.timeToString(zipEntry.getTime(), DateTools.Resolution.MINUTE),
                Field.Store.YES));
        doc.add(new LongField("filesize", zipEntry.getSize(), Field.Store.YES));     
        doc.add(new TextField("title", zipEntry.getName(), Field.Store.YES));   
        doc.add(new StringField("archivepath", zipEntry.getName(), Field.Store.YES));
        return doc;
    }

    @Override
    public String getDocumentSourcePath() {
        return path + "#" +zipEntry.getName();
    }
}
