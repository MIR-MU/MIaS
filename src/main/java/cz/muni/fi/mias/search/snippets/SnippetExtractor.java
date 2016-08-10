package cz.muni.fi.mias.search.snippets;

/**
 * Extracts the snippet. Arguments are left to implementations and their
 * constructors.
 *
 * @author Martin Liska
 */
public interface SnippetExtractor {

    public String getSnippet() throws InterruptedException;

}
