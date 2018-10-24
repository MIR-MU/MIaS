/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.mias.search;

import java.util.List;

/**
 * Container class for results from a single search.
 *
 * @author Martin Liska
 */
public class SearchResult {

    private List<Result> results;
    // Lucene API change: TopDocs.totalHits is now a long
    // https://lucene.apache.org/core/7_0_0/MIGRATE.html
    private long totalResults;
    private long coreSearchTime;
    private long totalSearchTime;
    private String query;
    private String processedQuery;
    private String luceneQuery;

    public SearchResult() {
    }

    /**
     *
     * @return String form of the final Lucene query
     */
    public String getLuceneQuery() {
        return luceneQuery;
    }

    public void setLuceneQuery(String luceneQuery) {
        this.luceneQuery = luceneQuery;
    }

    /**
     *
     * @return List of results
     */
    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    /**
     *
     * @return Total number of matched documents
     */
    public long getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(long totalResults) {
        this.totalResults = totalResults;
    }

    /**
     *
     * @return search time of Lucene core, without extracting information about
     * the hits
     */
    public long getCoreSearchTime() {
        return coreSearchTime;
    }

    public void setCoreSearchTime(long coreSearchTime) {
        this.coreSearchTime = coreSearchTime;
    }

    /**
     *
     * @return search time including hits information extraction
     */
    public long getTotalSearchTime() {
        return totalSearchTime;
    }

    public void setTotalSearchTime(long totalSearchTime) {
        this.totalSearchTime = totalSearchTime;
    }

    /**
     * @return original query as posted to the system
     */
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * @return processed query as used by the system for search
     */
    public String getProcessedQuery() {
        return processedQuery;
    }

    public void setProcessedQuery(String processedQuery) {
        this.processedQuery = processedQuery;
    }

}
