package cz.muni.fi.mias.indexing;

import cz.muni.fi.mias.PayloadSimilarity;
import cz.muni.fi.mias.Settings;
import cz.muni.fi.mias.indexing.doc.FileExtDocumentHandler;
import cz.muni.fi.mias.indexing.doc.FolderVisitor;
import cz.muni.fi.mias.indexing.doc.RecursiveFileVisitor;
import cz.muni.fi.mias.math.MathTokenizer;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * Indexing class responsible for adding, updating and deleting files from index,
 * creating, deleting whole index, printing statistics.
 *
 * @author Martin Liska
 * @since 14.5.2010
 */
public class Indexing {

    private static final Logger LOG = LogManager.getLogger(Indexing.class);
    
    private File indexDir;
    private Analyzer analyzer = new StandardAnalyzer();
    private long docLimit = Settings.getDocLimit();
    private long count = 0;
    private long progress = 0;
    private long fileProgress = 0;
    private String storage;
    private long startTime;

    /**
     * Constructor creates Indexing instance. Directory with the index is taken from the Settings.
     *
     */
    public Indexing() {
        this.indexDir = new File(Settings.getIndexDir());
    }

    /**
     * Indexes files located in given input path.
     * @param path Path to the documents directory. Can be a single file as well.
     * @param rootDir A path in the @path parameter which is a root directory for the document storage. It determines the relative path
     * the files will be index with.
     */
    public void indexFiles(String path, String rootDir) {
        storage = rootDir;
        if (!storage.endsWith(File.separator)) {
            storage += File.separator;
        }
        final File docDir = new File(path);
        if (!docDir.exists() || !docDir.canRead()) {
            LOG.fatal("Document directory '{}' does not exist or is not readable, please check the path.",docDir.getAbsoluteFile());            
            System.exit(1);
        }
        try {
            startTime = System.currentTimeMillis();
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_45, analyzer);
            PayloadSimilarity ps = new PayloadSimilarity();
            ps.setDiscountOverlaps(false);
            config.setSimilarity(ps);
            config.setIndexDeletionPolicy(new KeepOnlyLastCommitDeletionPolicy());
            try (IndexWriter writer = new IndexWriter(FSDirectory.open(indexDir), config))
            {
                LOG.info("Getting list of documents to index.");
                List<File> files = getDocs(docDir);
                countFiles(files);
                LOG.info("Number of documents to index is {}",count);
                indexDocsThreaded(files, writer);
            }
        } catch (IOException ex) {
            LOG.error(ex);
        }
    }

    private List<File> getDocs(File startPath) throws IOException {
        if(!startPath.canRead())
        {
            throw new IllegalArgumentException("Given path is not a folder. # "+startPath);
        }
        else
        {
            RecursiveFileVisitor fileVisitor = new FolderVisitor(docLimit);
            Files.walkFileTree(startPath.toPath(), fileVisitor);
            // TODO remove later
            List<File> result = new ArrayList<>(fileVisitor.getVisitedPaths().size());
            for(Path p : fileVisitor.getVisitedPaths())
            {
                result.add(p.toFile());
            }
            
            return result;
        }
    }

    private void indexDocsThreaded(List<File> files, IndexWriter writer) {
        try {
            Iterator<File> it = files.iterator();
            ExecutorService executor = Executors.newFixedThreadPool(Settings.getNumThreads());
            Future[] tasks = new Future[Settings.getNumThreads()];
            int running = 0;

            while (it.hasNext() || running > 0) {
                for (int i = 0; i < tasks.length; i++) {
                    if (tasks[i] == null && it.hasNext()) {
                        File f = it.next();
                        String path = resolvePath(f);
                        Callable callable = new FileExtDocumentHandler(f, path);
                        FutureTask ft = new FutureTask(callable);
                        tasks[i] = ft;
                        executor.execute(ft);
                        running++;
                    } else if (tasks[i] != null && tasks[i].isDone()) {
                        List<Document> docs = (List<Document>) tasks[i].get();
                        running--;
                        tasks[i] = null;
                        for (Document doc : docs) {
                            if (doc != null) {
                                if (progress % 10000 == 0) {
                                    printTimes();
                                    writer.commit();
                                }
                                try {
                                    LOG.info("adding to index {} docId={}",doc.get("path"),doc.get("id"));
                                    writer.updateDocument(new Term("id", doc.get("id")), doc);
                                    LOG.info("Documents indexed: {}", ++progress);
                                } catch (Exception ex) {
                                    LOG.fatal("Document '{}' indexing failed: {}",doc.get("path"),ex.getMessage());
                                    LOG.fatal(ex.getStackTrace());
                                }
                            }
                        }
                        LOG.info("File progress: {} of {} done...",++fileProgress, count);
                    }
                }
            }
            printTimes();
            executor.shutdown();
        } catch (IOException | InterruptedException | ExecutionException ex) {
            LOG.fatal(ex);
        }
    }

    /**
     * Optimizes the index.
     */
    public void optimize() {        
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_31, analyzer);
        config.setIndexDeletionPolicy(new KeepOnlyLastCommitDeletionPolicy());  
        // TODO what do we measure here ? time of optimization or optimiziation
        // and index opening aswell
        startTime = System.currentTimeMillis();
        try(IndexWriter writer = new IndexWriter(FSDirectory.open(indexDir), config)){
//            writer.optimize();    
            LOG.info("Optimizing time: {} ms",System.currentTimeMillis()-startTime);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    /**
     * Deletes whole current index directory
     */
    public void deleteIndexDir() {
        deleteDir(indexDir);
    }

    private void deleteDir(File f) {
        if (f.exists()) {
            File[] files = f.listFiles();
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    deleteDir(file);
                }
                else
                {
                    file.delete();
                }
            }
            f.delete();
        }
    }

    /**
     * Deletes files located in given path from the index
     *
     * @param path Path of the files to be deleted
     */
    public void deleteFiles(String path) {
        final File docDir = new File(path);
        if (!docDir.exists() || !docDir.canRead()) {
            LOG.error("Document directory '{}' does not exist or is not readable, please check the path.", docDir.getAbsolutePath());
            System.exit(1);
        }
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_31, analyzer);
        config.setIndexDeletionPolicy(new KeepOnlyLastCommitDeletionPolicy());
        try(IndexWriter writer = new IndexWriter(FSDirectory.open(indexDir), config)) { 
            deleteDocs(writer, docDir);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void deleteDocs(IndexWriter writer, File file) throws IOException {
        if (file.canRead()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File file1 : files)
                    {
                        deleteDocs(writer, file1);
                    }
                }
            } else {
                LOG.info("Deleting file {}.",file.getAbsoluteFile());
                writer.deleteDocuments(new Term("path",resolvePath(file)));
            }
        }
    }

    /**
     * Prints statistic about the current index
     */
    public void getStats() {
        String stats = "\nIndex statistics: \n\n";
        try(DirectoryReader dr = DirectoryReader.open(FSDirectory.open(indexDir))) {
            stats += "Index directory: "+indexDir.getAbsolutePath() + "\n";
            stats += "Number of indexed documents: " + dr.numDocs() + "\n";
            
            long fileSize = 0;
            for (int i = 0; i < dr.numDocs(); i++) {
                Document doc = dr.document(i);
                if (doc.getField("filesize")!=null) {
                    String size = doc.getField("filesize").stringValue();
                    fileSize += Long.valueOf(size);
                }
            }
            long indexSize = 0;
            File[] files = indexDir.listFiles();
            for (File f : files) {
                indexSize += f.length();
            }
            stats += "Index size: " + indexSize + " bytes \n";
            stats += "Approximated size of indexed files: " + fileSize + " bytes \n";

            LOG.info(stats);
        } catch (IOException | NumberFormatException e) {
            LOG.error(e.getMessage());
        } 
    }

    

    private String resolvePath(File file) throws IOException {
        String path = file.getCanonicalPath();
        return path.substring(storage.length());
    }

    private long getCpuTime() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        long result = 0;
        if (bean.isThreadCpuTimeSupported()) {
            final long[] ids = bean.getAllThreadIds();
            for (long id : ids) {
                result += bean.getThreadCpuTime(id) / 1000000;
            }
        }
        return result;
    }
    
    private long getUserTime() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        long result = 0;
        if (bean.isThreadCpuTimeSupported()) {
            final long[] ids = bean.getAllThreadIds();
            for (long id : ids) {
                result += bean.getThreadUserTime(id) / 1000000;
            }
        }
        return result;
    }

    private void printTimes() {
        LOG.info("---------------------------------");
        LOG.info(Settings.EMPTY_STRING);
        LOG.info("{} DONE in total time {} ms",progress,System.currentTimeMillis() - startTime);
        LOG.info("CPU time {} ms",getCpuTime());
        LOG.info("user time {} ms",getUserTime());
        MathTokenizer.printFormulaeCount(); // TODO
        LOG.info(Settings.EMPTY_STRING);
    }

    private void countFiles(List<File> files) {
        if (docLimit > 0) {
            count = Math.min(files.size(), docLimit);
        } else {
            count = files.size();
        }
    }
}
