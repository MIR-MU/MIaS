package cz.muni.fi.mias.indexing.doc;

import cz.muni.fi.mias.Settings;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.lucene.document.Document;

/**
 * Class providing document handling for files based on their file extension.
 * html,xhtml and txt files supported so far.
 *
 * @author Martin Liska
 */
public class FileExtDocumentHandler implements Callable {

    private static final Logger LOG = Logger.getLogger(FileExtDocumentHandler.class.getName());
    
    private File file;
    private String path;

    public FileExtDocumentHandler(File file, String path) {
        this.file = file;
        this.path = path;
    }

    /**
     * Calls coresponding document for input files based on it's extension. If needed, extracts an archive for file entries.
     * HtmlDocument is called in case of xhtml, html and xml files.
     * @param file Input file to be handled.
     * @return List<Lucene> of documents for the input files
     */
    public static List<Document> getDocuments(File file, String path) {
        String ext = path.substring(path.lastIndexOf(".") + 1);
        List<Document> result = new ArrayList<Document>();
        List<MIaSDocument> miasDocuments = new ArrayList<MIaSDocument>();
        try {
            ZipFile zipFile;
            if (ext.equals("zip")) {
                zipFile = new ZipFile(file);
                Enumeration e = zipFile.entries();
                while (e.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) e.nextElement();
                    if (!entry.isDirectory()) {
                        String name = entry.getName();
                        int extEnd = name.lastIndexOf("#");
                        if (extEnd < name.lastIndexOf(".")) {
                            extEnd = name.length();
                        }
                        ext = name.substring(name.lastIndexOf(".") + 1, extEnd);
                        MIaSDocument miasDocument = handleExtension(ext, new ZipEntryDocument(zipFile, path, entry));
                        if (miasDocument != null) {
                            miasDocuments.add(miasDocument);
                        }
                    }
                }
            } else {
                DocumentSource source = new FileDocument(file, path);
                MIaSDocument miasDocument = handleExtension(ext, source);
                if (miasDocument!=null) {
                    miasDocuments.add(miasDocument);
                }
            }            
            for (MIaSDocument doc : miasDocuments) {
                result.addAll(doc.getDocuments());
            }
        } catch (Exception e) {
            System.out.println("Cannot handle file " + file.getAbsolutePath());
            e.printStackTrace();
        }
        return result;
    }
    
    private static MIaSDocument handleExtension(String ext, DocumentSource source) {
        MIaSDocument miasDocument = null;
        if (Settings.getIndexFormulaeDocuments()) {
            miasDocument = new FormulaDocument(source);
        } else if (ext.equals("html") || ext.equals("xhtml")) {// || ext.equals("xml")) {
            miasDocument = new HtmlDocument(source);
        }
        return miasDocument;
    }

    public List<Document> call() {
        return getDocuments(file, path);
    }
    
}
