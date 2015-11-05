package cz.muni.fi.mias.search;

import cz.muni.fi.mias.MIaSUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts arXiv IDs from the documents from NTCIR-10 Math Task data collection
 * for URL construction.
 *
 * @author Michal Růžička <mruzicka@mail.muni.cz>
 */
public class NTCIR10CollectionDocumentIdExtractor implements DocumentIdExtractor {

    private InputStream document;

    NTCIR10CollectionDocumentIdExtractor(InputStream is) {
        this.document = is;
    }

    @Override
    public String getId() throws FileNotFoundException, IOException {

        if (document == null) {
            return null;
        }

        String content = MIaSUtils.getContent(document);

        String patternString = "property=\"dct:identifier\" content=\"([^\"]+)\"";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String arXivId = matcher.group(1);
            return "http://arxiv.org/abs/" + arXivId;
        } else {
            return null;
        }

    }
}
