/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.mias.search.snippets;

/**
 *
 * @author Dominik Szalai - emptulik at gmail.com
 */
public class Snippet{
    private int start;
    private int end;
    private String text;

    public Snippet() {
    }

    public Snippet(int start, int end, String text) {
        this.start = start;
        this.end = end;
        this.text = text;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
