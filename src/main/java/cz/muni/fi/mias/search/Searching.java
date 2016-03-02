package cz.muni.fi.mias.search;

import cz.muni.fi.mias.*;
import cz.muni.fi.mias.math.MathSeparator;
import cz.muni.fi.mias.math.MathTokenizer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.payloads.AveragePayloadFunction;
import org.apache.lucene.search.payloads.PayloadTermQuery;
import org.apache.lucene.store.FSDirectory;

/**
 * Searching class responsible for searching over current index.
 *
 *
 * @author Martin Liska
 * @since 14.5.2010
 */
public class Searching {

    private IndexSearcher indexSearcher;
    private String storagePath;
    private PayloadSimilarity ps = new PayloadSimilarity();
//    private TitlesSuggester sug;
    private int snippetsEnabledLimit = 1000;

    /**
     * Constructs new Searching on the index from the Settings file.
     *
     */
    public Searching() {
        try {
            this.indexSearcher = new IndexSearcher(IndexReader.open(FSDirectory.open(new File(Settings.getIndexDir()))));
            this.indexSearcher.setSimilarity(ps);
            this.storagePath = "";
//            sug = new TitlesSuggester(indexSearcher.getIndexReader());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructs new Searching using the given IndexSearcher
     *
     * @param searcher IndexSearcher
     * @param storagePath Root path where the document searched by the searcher
     * are located
     */
    public Searching(IndexSearcher searcher, String storagePath) {
        this.indexSearcher = searcher;
        this.indexSearcher.setSimilarity(new PayloadSimilarity());
        this.storagePath = storagePath;
//        sug = new TitlesSuggester(indexSearcher.getIndexReader());
    }

    /**
     * Searches the index for input in given InputStream. Used for command line
     * or file input of the query. Prints results to standard output.
     *
     * @param is InputStream with query input.
     */
    public void search(InputStream is) {
        BufferedReader br = null;
        try {
            if (!(is instanceof FileInputStream)) {
                System.out.println("\nEnter query: ");
            }
            br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String queryString = "";
            String line;
            while ((line = br.readLine()) != null) {
                queryString += line + " ";
            }
            search(queryString, true, 0, 100, false);
        } catch (IOException ex) {
            Logger.getLogger(Searching.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(Searching.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Searches the index for query specified by string.
     *
     * @param query String with the query
     * @param print if true, results will be printed to standard output
     * @param offset index of the first retrieved result
     * @param limit number of results to retrieve
     * @param debug if true, results will contain debugging information
     *
     * @return Search result
     */
    public SearchResult search(String query, boolean print, int offset, int limit, boolean debug) {
        return search(query, print, offset, limit, debug, MathTokenizer.MathMLType.BOTH);
    }

    public SearchResult search(String query, boolean print, int offset, int limit, boolean debug, MathTokenizer.MathMLType variant) {
        SearchResult result = new SearchResult();
        result.setQuery(query);
        try {
            Date start = new Date();
            Query bq = parseInput(query, variant);
            TopDocs docs = indexSearcher.search(bq, Settings.getMaxResults());
//            TopFieldDocs docs = indexSearcher.search(bq, null, Settings.getMaxResults(), Sort.RELEVANCE, true, false);
            Date end = new Date();
            long time = end.getTime() - start.getTime();
            result.setCoreSearchTime(time);
            result.setResults(getResults(offset, limit, new ArrayList<ScoreDoc>(Arrays.asList(docs.scoreDocs)), bq, debug));
            result.setTotalResults(docs.totalHits);
            if (debug) {
                result.setLuceneQuery(bq.toString());
            }
            end = new Date();
            result.setTotalSearchTime(end.getTime() - start.getTime());
            if (print) {
                printResults(result, bq, indexSearcher);
            }
        } catch (IOException ex) {
            Logger.getLogger(Searching.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * Parses given query string with possible MathML formulae Supports query
     * grammar specified by org.apache.lucene.queryParser.QueryParser for text
     * queries
     *
     * @param queryString String holding the query.
     * @return Query instance representing input query. This query is in form of
     * (formula_1 or ... or formula_n) and (text queries)
     */
    private Query parseInput(String queryString, MathTokenizer.MathMLType variant) {
        BooleanQuery result = new BooleanQuery();
        String[] sep = MathSeparator.separate(queryString, "");
        if (sep[1].length() > 0) {
            BooleanQuery bq = new BooleanQuery();
            String mathQuery = "<?xml version='1.0' encoding='UTF-8'?><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1 plus MathML 2.0//EN\" \"http://www.w3.org/TR/MathML2/dtd/xhtml-math11-f.dtd\"><html>" + sep[1] + "</html>";
            if (variant == MathTokenizer.MathMLType.PRESENTATION || variant == MathTokenizer.MathMLType.BOTH) {
                addMathQueries(mathQuery, bq, MathTokenizer.MathMLType.PRESENTATION);
            }
            if (variant == MathTokenizer.MathMLType.CONTENT || variant == MathTokenizer.MathMLType.BOTH) {
                addMathQueries(mathQuery, bq, MathTokenizer.MathMLType.CONTENT);
            }
            result.add(bq, BooleanClause.Occur.MUST);
        }
        if (sep[0].length() > 0) {
            QueryParser parser = new MultiFieldQueryParser(new String[]{"content", "title"}, new StandardAnalyzer());
            try {
                Query query = parser.parse(sep[0]);
                result.add(query, BooleanClause.Occur.MUST);
            } catch (ParseException pe) {
                System.out.println(pe.getMessage());
            }
        }
        return result;
    }

    private void addMathQueries(String mathQuery, BooleanQuery bq, MathTokenizer.MathMLType variant) {
        MathTokenizer mt = new MathTokenizer(new StringReader(mathQuery), false, variant);
        try {
            mt.reset();
        } catch (IOException ex) {
            Logger.getLogger(Searching.class.getName()).log(Level.SEVERE, null, ex);
        }
        Map<String, Float> queryForms = mt.getQueryFormulae();
        List<Query> cQueries = getMathQueries(queryForms, variant);
        for (Query q : cQueries) {
            bq.add(q, BooleanClause.Occur.SHOULD);
        }
    }

    private List<Query> getMathQueries(Map<String, Float> queryForms, MathTokenizer.MathMLType type) {
        String field = (type == MathTokenizer.MathMLType.PRESENTATION ? "p" : "c") + "math";
        List<Query> result = new ArrayList<Query>();
        Iterator<Map.Entry<String, Float>> it = queryForms.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Float> entry = it.next();
            Float boost = entry.getValue();
            PayloadTermQuery ptq = new PayloadTermQuery(new Term(field, entry.getKey()), new AveragePayloadFunction());

            ptq.setBoost(boost);
            result.add(ptq);
        }
        return result;
    }

    /**
     * Constructs the list with the results.
     *
     * @param offset
     * @param limit
     * @param docs
     * @param query
     * @param debug
     * @return
     * @throws IOException
     */
    private List<Result> getResults(int offset, int limit, List<ScoreDoc> docs, Query query, boolean debug) throws IOException {
        List<Result> results = new ArrayList<Result>();
        List<ScoreDoc> temp = docs.subList(offset, Math.min(offset + limit, docs.size()));

        for (ScoreDoc sd : temp) {
            Document document = indexSearcher.doc(sd.doc);
            String fullLocalPath = document.get("path");
            String dataPath = storagePath + fullLocalPath;

            String title = document.get("title");
            String info = "score = " + sd.score;
            if (debug) {
                info += "\nExplanation: \n" + indexSearcher.explain(query, sd.doc);
            }
            //SPECIAL FOR ARXMLIV
            String id = document.get("arxivId");
            if (id != null && !Character.isDigit(id.charAt(0))) {
                id = "http://arxiv.org/abs/" + id.replace(".", "/");
            } else {
                id = document.get("id");
            }

            String snippet = "Snippets disabled";
            if (limit <= snippetsEnabledLimit) {
                InputStream snippetIs = getInputStreamFromDataPath(document);
                if (snippetIs != null) {
                    SnippetExtractor extractor = new NiceSnippetExtractor(snippetIs, query, sd.doc, indexSearcher.getIndexReader());
                    snippet = extractor.getSnippet();
                } else {
                    System.out.println("Stream is null for snippet extraction " + dataPath);
                }
                if (snippetIs != null) {
                    snippetIs.close();
                }
            } else {
                snippet = "Snippets disabled for limit > " + snippetsEnabledLimit;
            }
            results.add(new Result(title, fullLocalPath, info, id, snippet));
        }
        return results;
    }

    /**
     * Prints results to standard output.
     *
     * @param docs TopDocs with top hits for query.
     * @param query Query that was searched for.
     * @param searcher Searcher that performed the search.
     * @throws IOException
     * @throws CorruptIndexException
     */
    private void printResults(SearchResult searchResult, Query query, IndexSearcher searcher)
            throws IOException, CorruptIndexException {
        System.out.println("Searching for: " + query.toString());
        System.out.println("Time: " + searchResult.getCoreSearchTime() + "ms");
        int totalResults = searchResult.getTotalResults();
        System.out.println("\nTotal hits: " + totalResults);
        if (totalResults == 0) {
            System.out.println("-------------");
            System.out.println("Nothing found");
            System.out.println("-------------");
        } else {
            BufferedReader is = new BufferedReader(new InputStreamReader(System.in));
            int hitsPP = 30;
            int start = 0;
            int end = 0;
            boolean quit = false;

            while (!quit) {
                end = Math.min(end + hitsPP, searchResult.getResults().size());
                for (int i = start; i < end; i++) {
                    Result result = searchResult.getResults().get(i);
                    String title = result.getTitle();
                    if (title != null) {
                        if (title.length() > 60) {
                            System.out.println(title.substring(0, 60) + " ...");
                        } else {
                            System.out.println(title);
                        }
                    }
                    System.out.println("id: " + result.getId());
                    System.out.println(result.getPath());
                    System.out.println(result.getSnippet());
                    System.out.println("----------------------------------------------------");
                }
                System.out.println("Showing results " + (start + 1) + "-" + end);
                if (end == searchResult.getResults().size()) {
                    break;
                }
                System.out.println("Show next page?(y/n)");
                String s = is.readLine();
                if (s == null || s.length() == 0 || s.charAt(0) == 'n') {
                    quit = true;
                }
                start = start + hitsPP;
            }
        }
    }

    private InputStream getInputStreamFromDataPath(Document document) {

        InputStream is = null;

        try {
            String fullLocalPath = document.get("path");
            String dataPath = storagePath + fullLocalPath;
            File f = new File(dataPath);

            if (f.exists() && !dataPath.endsWith("zip")) {
                is = new FileInputStream(f);
            }
            if (dataPath.endsWith("zip")) {
                if (f.exists()) {
                    String archivePath = document.get("archivepath");
                    ZipFile zipFile = new ZipFile(dataPath);
                    Enumeration e = zipFile.entries();
                    while (e.hasMoreElements() && is == null) {
                        ZipEntry entry = (ZipEntry) e.nextElement();
                        if (entry.getName().equals(archivePath)) {
                            is = zipFile.getInputStream(entry);
                        }
                    }
                } else {
                    String unzippedPath = dataPath.substring(0, dataPath.lastIndexOf(File.separator)) + File.separator + document.get("archivepath");
                    f = new File(unzippedPath);
                    if (f.exists()) {
                        is = new FileInputStream(f);
                    }
                }
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Searching.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return is;
        }

    }
}
