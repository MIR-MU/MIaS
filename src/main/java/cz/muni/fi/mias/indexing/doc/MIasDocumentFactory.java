/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.mias.indexing.doc;

import cz.muni.fi.mias.Settings;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Dominik Szalai - emptulik at gmail.com
 */
public class MIasDocumentFactory
{
    private Set<String> availableExtensions = new HashSet<>();
    
    public MIasDocumentFactory()
    {
        availableExtensions.add("html");
        availableExtensions.add("xhtml");
    }
    
    public MIaSDocument buildDocument(String fileExtension, DocumentSource documentSource)
    {
        if (Settings.getIndexFormulaeDocuments())
        {
            return new FormulaDocument(documentSource);
        }
        else if (availableExtensions.contains(fileExtension))
        {
            return new HtmlDocument(documentSource);
        }
        else
        {
            return null;
        }
    }

    public void setAvailableExtensions(Set<String> availableExtensions)
    {
        this.availableExtensions = availableExtensions;
    }
}
