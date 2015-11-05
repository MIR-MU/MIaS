package cz.muni.fi.mias;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Settings class responsible for loading settings from mias.properties Property file.
 * mias.properties file is located in the path specified by the cz.muni.fi.mias.mias.to file or in the working directory.
 *
 * @author Martin Liska
 * @since 14.12.2012
 */
public class Settings {

    private static Properties config;
    public static char dirSep = System.getProperty("file.separator").charAt(0);
    public static String eol = System.getProperty("line.separator");
    public static final String MATHDOCHEADER = "<?xml version='1.0' encoding='UTF-8'?><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1 plus MathML 2.0//EN\" \"http://www.w3.org/TR/MathML2/dtd/xhtml-math11-f.dtd\">";

    static {
        config = new Properties();
        String path;
        Properties configFilePointer = new Properties();
        try {
            configFilePointer.load(Settings.class.getResourceAsStream("mias.to"));
            path = configFilePointer.getProperty("MIASPATH");
        } catch (IOException ex) {
            path = "./mias.properties";
            Logger.getLogger(Settings.class.getName()).log(Level.WARNING, "Cannot load properties file path from 'mias.to' file. Using {0} instead", path);
        }
        try {
            config.load(new FileInputStream(path));
        } catch (Exception e) {
            Logger.getLogger(Settings.class.getName()).log(Level.WARNING, "Cannot load properties file in " + path);
            path = "./mias.properties";
            Logger.getLogger(Settings.class.getName()).log(Level.INFO, "Using "+path+" instead.");
            try {
                config.load(new FileInputStream(path));
            } catch (IOException ex) {
                Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, "Cannot load properties file in "+path+", please check path of mias.properties.");
                System.exit(2);
            }
        }
    }
    
    /**
     * 
     * @return Direcotry where the index is located or will be stored.
     */
    public static String getIndexDir() {
        String result = config.getProperty("INDEXDIR");
        if (result == null || result.equals("")) {
            System.out.println("Broken properties file mias.properties. Please check INDEXDIR entry.");
            System.exit(2);
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
        int result = Integer.parseInt(threads);
        if (result<0) {
            result = 1;
        }
        return result;
    }

    /**
     * 
     * @return Maximum number of results that the system retrieves.
     */
    public static int getMaxResults() {
        String n = config.getProperty("MAXRESULTS");
        int result = 10000;
        try {
            result = Integer.parseInt(n);
        } catch (Exception e) {
        }
        return result;
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
        if (prop == null || prop.isEmpty()) {
            return true;
        }
        boolean result = Boolean.parseBoolean(prop);
        return result;
    }
}
