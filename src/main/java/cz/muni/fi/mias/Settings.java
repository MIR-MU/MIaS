package cz.muni.fi.mias;

import java.io.FileInputStream;
import java.util.Properties;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Settings class responsible for loading settings from mias.properties Property file.
 * mias.properties file is located in the path specified by the cz.muni.fi.mias.mias.to file or in the working directory.
 *
 * @author Martin Liska
 * @since 14.12.2012
 */
public class Settings {

    private static final Logger LOG = LogManager.getLogger(Settings.class);
    public static final String EMPTY_STRING  = "";
    private static Properties config;
    public static char dirSep = System.getProperty("file.separator").charAt(0);
    public static String eol = System.getProperty("line.separator");
    public static final String MATHDOCHEADER = "<?xml version='1.0' encoding='UTF-8'?><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1 plus MathML 2.0//EN\" \"http://www.w3.org/TR/MathML2/dtd/xhtml-math11-f.dtd\">";
    
    public static final String OPTION_CONF = "conf";
    public static final String OPTION_ADD = "add";
    public static final String OPTION_OVERWRITE = "overwrite";
    public static final String OPTION_DELETE = "delete";
    public static final String OPTION_OPTIMIZE = "optimize";
    public static final String OPTION_DELETEINDEX = "deleteindex";
    public static final String OPTION_STATS = "stats";
    public static final String OPTION_INDOCPROCESS = "indocprocess";
    
    public static Options getMIaSOptions() {
        Options options = new Options();
        options.addOption(Option.builder(OPTION_CONF)
            .hasArg()
            .desc("Path to indexing configuration file.")
            .build());
        options.addOption(Option.builder(OPTION_ADD)
            .hasArgs()
            .numberOfArgs(2)
            .argName("input_path> <root_dir")
            .desc("where root_dir is an absolute path to a directory in the input_path to determine the relative path that the files will be indexed with")
            .build());
        options.addOption(Option.builder(OPTION_OVERWRITE)
            .hasArgs()
            .numberOfArgs(2)
            .argName("input_path> <root_dir")
            .desc("Overwrites existing index")
            .build());
        options.addOption(Option.builder(OPTION_DELETE)
            .hasArg()
            .argName("dir_or_file")
            .desc("Deletes file(s) from index.")
            .build());
        options.addOption(Option.builder(OPTION_OPTIMIZE)
            .desc("Optimizes the index for maximum searching performance.")
            .build());
        options.addOption(Option.builder(OPTION_DELETEINDEX)
            .desc("Deletes the index.")
            .build());
        options.addOption(Option.builder(OPTION_STATS)
            .desc("Prints statistics about index.")
            .build());
        options.addOption(Option.builder(OPTION_INDOCPROCESS)
            .hasArgs()
            .numberOfArgs(2)
            .argName("input_path> <root_dir")
            .desc("where root_dir is an absolute path to a directory in the input_path. Processes math formulae and inserts M-terms into documents created under root_dir.")
            .build());
        return options;
    }
    
    public static void init(String propertiesFilePath) {
        config = new Properties();
        if (propertiesFilePath != null) {
            try {
                config.load(new FileInputStream(propertiesFilePath));
            } catch (Exception e) {
               LOG.fatal(e);
                System.exit(2);
            }
        }
    }
    
    public static void init() {
        config = new Properties();
    }
    
    /**
     * 
     * @return Direcotry where the index is located or will be stored.
     */
    public static String getIndexDir() {
        String indexDir = config.getProperty("INDEXDIR");
        String result = "/index";
        if (indexDir != null) {
            result = indexDir;
        }
        return result;
    }

    /**
     * 
     * @return Preference for updating of the already indexed files. If true, the already indexed files will be updated. If false
     * only new files will be added.
     */
    public static boolean getUpdateFiles() {
        String duplicates = config.getProperty("UPDATE");
        boolean result = false;
        if (duplicates != null) {
            result = Boolean.parseBoolean(duplicates);
        }
        return result;
    }
    
    /**
     * 
     * @return Number of threads for processing.
     */
    public static int getNumThreads() {
        String threads = config.getProperty("THREADS");
        int result = -1;
        try {
            result = Integer.parseInt(threads);
        } catch (Exception e) {
        }
        if (result < 1) {
            result = Runtime.getRuntime().availableProcessors();
        }
        return result;
    }

    /**
     * 
     * @return Maximum number of results that the system retrieves.
     */
    public static int getMaxResults() {
        String n = config.getProperty("MAXRESULTS");
        int result = 1000;
        try {
            result = Integer.parseInt(n);
        } catch (Exception e) {
        }
        return result;
    }
    
    public static void setMaxResults(String maxResults) {
        config.setProperty("MAXRESULTS", maxResults);
    }

    /**
     * 
     * @return A limit for the number of indexed files for one run. -1 means no limit.
     */
    public static long getDocLimit() {
        String n = config.getProperty("DOCLIMIT");
        long result = -1;
        try {
            result = Integer.parseInt(n);
        } catch (Exception e) {
        }
        return result;
    }

    public static boolean getIndexFormulaeDocuments() {
        String prop = config.getProperty("FORMULA_DOCUMENTS");
        boolean result = false;
        if (prop != null) {
            result = Boolean.parseBoolean(prop);
        }
        return result;
    }
}
