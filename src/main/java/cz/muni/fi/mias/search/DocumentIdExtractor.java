package cz.muni.fi.mias.search;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Extracts ID from the document for URL construction.
 *
 * @author Michal Růžička <mruzicka@mail.muni.cz>
 */
public interface DocumentIdExtractor {

    public String getId() throws FileNotFoundException, IOException;
}
