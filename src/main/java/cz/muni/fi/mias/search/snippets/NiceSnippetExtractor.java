package cz.muni.fi.mias.search.snippets;

import cz.muni.fi.mias.MIaSUtils;
import cz.muni.fi.mias.Settings;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;

/**
 * Extracts fragments around the query match and highlights it. Two most
 * significant math hits and two textual matches are highlighted.
 *
 * @author Martin Liska
 */
public class NiceSnippetExtractor implements SnippetExtractor {

    private static final Logger LOG = LogManager.getLogger(NiceSnippetExtractor.class);
    private Query query;
    private int docNumber;
    private IndexReader indexReader;
    private InputStream inputStream;

    public NiceSnippetExtractor(InputStream in, Query q, int docNumber, IndexReader indexReader) {
        this.inputStream = in;
        this.query = q;
        this.docNumber = docNumber;
        this.indexReader = indexReader;
    }

    @Override
    public String getSnippet() throws InterruptedException {
        try {
            List<Query> stqs = new ArrayList<>();
            List<Query> nstqs = new ArrayList<>();
            getSpanTermQueries(query, stqs, nstqs);
            List<Span> formSpans = new ArrayList<>();
            for (Query q : stqs) {
                for (AtomicReaderContext context : indexReader.leaves()) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Snippet extraction thread interrupted during processing");
                    }
                    Spans spans = ((SpanTermQuery) q).getSpans(context, null, new HashMap());
                    spans.skipTo(docNumber - context.docBase - 1);
                    boolean cont = true;
                    boolean contextFound = false;
                    while (cont) {
                        int contextSpanDocNumber = context.docBase + spans.doc();
//                        System.out.println(contextSpanDocNumber);
                        if (docNumber == contextSpanDocNumber) {
                            contextFound = true;
                            cont = docNumber - contextSpanDocNumber >= 0;
                            Collection<byte[]> payloads = spans.getPayload();
                            formSpans.add(new Span(spans.doc(), q.toString(), spans.start(), cz.muni.fi.mias.math.PayloadHelper.decodeFloatFromShortBytes(payloads.iterator().next())));
                        }
                        if (!spans.next()) {
                            cont = false;
                        }
                    }
                    if (contextFound) {
                        break;
                    }
                }
            }
            return getSnippet(formSpans, nstqs);
        } catch (IOException ex) {
            LOG.fatal(ex);
        }
        return "";
    }

    private void getSpanTermQueries(Query query, List<Query> spanTermQueries, List<Query> nonSpamTermQueries) throws IOException, InterruptedException {
        Query q;
        synchronized (query) {
            q = query.rewrite(indexReader);
        }
        if (q instanceof SpanTermQuery) {
            spanTermQueries.add(q);
        } else {
            if (q instanceof BooleanQuery) {
                BooleanClause[] bcs = ((BooleanQuery) q).getClauses();
                for (BooleanClause bc : bcs) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Snippet extraction thread interrupted during boolean clauses processing");
                    }
                    getSpanTermQueries(bc.getQuery(), spanTermQueries, nonSpamTermQueries);
                }
            } else {
                nonSpamTermQueries.add(q);
            }
        }
    }

    private String getSnippet(List<Span> spans, List<Query> nstqs) throws FileNotFoundException, IOException, InterruptedException {
        String content = MIaSUtils.getContent(inputStream);

        List<Snippet> snippets = getDocSnippets(spans, nstqs, content);

        String result = "";

        Iterator<Snippet> it = snippets.iterator();
        while (it.hasNext()) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Snippet extraction thread interrupted during processing");
            }
            String text = it.next().getText();
            if (isDots(result, false) && isDots(text, true)) {
                result += " " + text.substring(3);
            } else {
                result += " " + text;
            }
        }
        return result;
    }

    private static boolean isDots(String text, boolean beggining) {
        int start;
        int end;
        if (beggining) {
            start = 0;
            end = 3;
        } else {
            start = Math.max(0, text.length() - 3);
            end = text.length();
        }
        String dots = text.substring(start, end);
        return dots.equals("...");
    }

    private List<Snippet> getDocSnippets(List<Span> spans, List<Query> nstqs, String content) throws InterruptedException {
        List<Snippet> result = new ArrayList<>();

        if (spans != null && !spans.isEmpty()) {
            Collections.sort(spans);
            //remove duplicite positions from generalization
            Iterator<Span> itSpan = spans.iterator();
            int prevPosition = -1;
            while (itSpan.hasNext()) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Snippet extraction thread interrupted during span processing");
                }
                Span span = itSpan.next();
                int currentPosition = span.getPosition();
                if (currentPosition == prevPosition) {
                    itSpan.remove();
                }
                prevPosition = currentPosition;

            }
            String mathStart = "<math";
            String mathEnd = "</math>";
            for (int j = 0; j <= 1 && j < spans.size(); j++) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Snippet extraction thread interrupted during span processing");
                }
                int pos = spans.get(j).getPosition();
                int start = 0;
                int end = 0;
                start = content.indexOf(mathStart, start + 1);
                if (start == -1) {
                    mathStart = "<m:math";
                    mathEnd = "</m:math>";
                }
                start = 0;
                for (int i = 0; i <= pos; i++) {
                    start = content.indexOf(mathStart, start + 1);
                }
                end = content.indexOf(mathEnd, start) + mathEnd.length();
                String snipp = "<span class=\"highlight\">"
                        + content.substring(start, end).replace("display=\"block\"", "display=\"inline\"")
                        + "</span>";
                Snippet snippet = new Snippet(start, end, snipp);
                addSurround(snippet, content);
                result.add(snippet);
            }
        }

        Set<Term> terms = new LinkedHashSet<>();
        for (Query q : nstqs) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Snippet extraction thread interrupted during nstqs processing");
            }
            q.extractTerms(terms);
        }
        final String tagHighlightStart = "<span class=\"highlight\">";
        final String tagHighlightEnd = "</span>";
        for (Term t : terms) {
            String text = t.text();
            String highlightIn;
            //first highlight in existing snippets
            boolean newSnippet = true;
            for (Snippet snip : result) {
                int start = -tagHighlightStart.length();// = highlightIn.indexOf(text);
                while (start != -1) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Snippet extraction thread interrupted during terms processing");
                    }
                    highlightIn = snip.getText();
                    start = highlightIn.indexOf(text, start + tagHighlightStart.length() + 1);
                    if (start != -1) {
                        if (!isInTag(highlightIn, start)) {
                            StringBuilder sb = new StringBuilder(highlightIn);
                            sb.insert(start, tagHighlightStart);
                            sb.insert(sb.indexOf(text, start) + text.length(), tagHighlightEnd);
                            snip.setText(sb.toString());
                            newSnippet = false;
                        }
                    }
                }
            }
            highlightIn = content.toLowerCase();
            //
            if (newSnippet && result.size() <= 4) {
                int start = 0;
                boolean added = false;
                while (!added) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Snippet extraction thread interrupted during terms processing");
                    }
                    start = highlightIn.indexOf(text, start);
                    if (start != -1) {
                        if (!isInTag(content, start)) {
                            int end = start + text.length();
                            String snipp = tagHighlightStart + content.substring(start, end) + tagHighlightEnd;
                            Snippet snippet = new Snippet(start, end, snipp);
                            addSurround(snippet, content);
                            if (isUniqueSnippet(snippet, result)) {
                                result.add(snippet);
                                added = true;
                            }
                            start = end;
                        } else {
                            start += 100;
                        }
                    } else {
                        added = true;
                    }
                }
            }
        }
        return result;
    }

    private boolean isUniqueSnippet(Snippet snippet, List<Snippet> result) throws InterruptedException {
        boolean unique = true;
        for (Snippet s : result) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Snippet extraction thread interrupted during uniqueness checking");
            }
            if (s.getText().replaceAll("\\s", "").contains(snippet.getText().replaceAll("\\s", ""))) {
                unique = false;
                break;
            }
        }
        return unique;
    }

    private void addSurround(Snippet snippet, String content) {
        int start = snippet.getStart();
        int end = snippet.getEnd();
        int preStart = Math.max(content.lastIndexOf("." + Settings.eol, start) + Settings.eol.length() + 1, Math.max(content.lastIndexOf(". ", start) + 2, content.lastIndexOf(">", start) + 1));
        preStart = preStart < 0 ? 0 : preStart;
        String pre = "";
        try {
            pre = content.substring(preStart, start);
        } catch (Exception ex) {
            LOG.fatal(ex);
        }
        if (pre.isEmpty() || !Character.isUpperCase(pre.charAt(0))) {
            pre = "... " + pre;
        }
        snippet.setStart(preStart);

        int postEndWithEol = content.indexOf("." + Settings.eol, start) + 1;
        int postEnd = Math.min(postEndWithEol == 0 ? Integer.MAX_VALUE : postEndWithEol, Math.min(content.indexOf(". ", end) + 1, content.indexOf("<", end)));
        postEnd = postEnd <= 0 ? end : postEnd;
        String post = "";
        try {
            post = content.substring(end, postEnd);
        } catch (Exception ex) {
            LOG.fatal(ex);
        }
        if (post.isEmpty() || (post.charAt(post.length() - 1)) != '.') {
            post += " ...";
        }
        snippet.setEnd(postEnd);
        snippet.setText(pre + snippet.getText() + post);
    }

    private static boolean isInTag(String content, int start) {
        int tagstart1 = content.lastIndexOf(">", start);
        int tagstart2 = content.lastIndexOf("<", start);
        return tagstart1 < tagstart2;
    }
}
