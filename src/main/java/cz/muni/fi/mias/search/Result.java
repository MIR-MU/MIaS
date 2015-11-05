/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.mias.search;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Container class for information about a single hit document.
 * 
 * @author Martin Liska
 */
@XmlRootElement(name = "result")
public class Result {
    
    private String title;
    private String path;
    private String info;
    private String id;
    private String snippet;

    public Result(String title, String path, String info, String id, String snippet) {
        this.title = title;
        setPath(path);
        this.info = info;
        this.id = id;
        this.snippet = snippet;
    }

    public Result() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        if (path.endsWith(".zip")) {
            path = path.substring(0, path.length() -4);
        }
        this.path = path;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }
    
}
