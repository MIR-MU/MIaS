/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.mias.indexing.doc;

import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author Dominik Szalai - emptulik at gmail.com
 */
public interface RecursiveFileVisitor extends FileVisitor<Path>
{
    /**
     * Method is used to obtain paths visited by {@link FileVisitor}.
     * @return list of visited paths
     */
    List<Path> getVisitedPaths();
}
