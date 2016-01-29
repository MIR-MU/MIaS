/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.mias.search.snippets;

import java.io.Serializable;

/**
 *
 * @author Dominik Szalai - emptulik at gmail.com
 */
public class Span implements Serializable, Comparable {
    private static final long serialVersionUID = -6709073152064919438L;
    
    private int doc;
    private String term;
    private int position;
    private float payload;

    public Span(int doc, String term, int position, float payload) {
        this.doc = doc;
        this.term = term;
        this.position = position;
        this.payload = payload;
    }

    public Span() {
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public int getDoc() {
        return doc;
    }

    public void setDoc(int doc) {
        this.doc = doc;
    }

    public float getPayload() {
        return payload;
    }

    public void setPayload(float payload) {
        this.payload = payload;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "Span{" + "doc=" + doc + "term=" + term + "position=" + position + "payload=" + payload + '}';
    }

    @Override
    public int compareTo(Object o) {
        float f1 = this.getPayload();
        float f2 = ((Span) o).getPayload();
        return f1 == f2 ? 0 : f1 - f2 > 0 ? -1 : 1;
    }
}