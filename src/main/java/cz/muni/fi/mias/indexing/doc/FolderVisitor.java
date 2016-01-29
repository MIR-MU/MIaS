/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.mias.indexing.doc;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Dominik Szalai - emptulik at gmail.com
 */
public class FolderVisitor implements RecursiveFileVisitor
{
    private static final Logger LOG = LogManager.getLogger(FolderVisitor.class);

    private final List<Path> visitedPaths = new ArrayList<>();
    private final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*{html,xhtml,zip}");
    private long docLimit = -1;

    public FolderVisitor(long docLimit)
    {
        this.docLimit = docLimit;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
    {
        if (canContinue())
        {
            LOG.trace("Entering directory {}", dir);
            return FileVisitResult.CONTINUE;
        }
        else
        {
            LOG.debug("Document number reached.");
            return FileVisitResult.TERMINATE;
        }
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
    {
        if (canContinue())
        {
            if (matcher.matches(file.getFileName()))
            {
                LOG.trace("Adding file {} to output list.", file);
                visitedPaths.add(file);
                return FileVisitResult.CONTINUE;
            }
            else
            {
                LOG.trace("Path {} was rejected by matcher.",file);
                return FileVisitResult.CONTINUE;
            }
        }
        else
        {
            LOG.debug("Document number reached.");
            return FileVisitResult.TERMINATE;
        }
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
    {
        LOG.error(exc);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
    {
        LOG.trace("Exiting directory {}", dir);
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public List<Path> getVisitedPaths()
    {
        return visitedPaths;
    }
    
    private boolean canContinue()
    {
        if(docLimit == -1)
        {
            return true;
        }
        
        return visitedPaths.size() <= docLimit;
    }
}
