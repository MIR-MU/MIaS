package cz.muni.fi.mias;

import cz.muni.fi.mias.indexing.Indexing;
import cz.muni.fi.mias.search.Searching;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Main class witch main method.
 *
 * @author Martin Liska
 * @since 14.5.2010
 */
public class MIaS {

    /**
     * @param see printHelp()
     */
    public static void main(String[] args) {
        
        if (args.length == 0) {
            printHelp();
            System.exit(1);
        }

        if (args[0].equals("-h")||args[0].equals("-help")) {
            printHelp();
            System.exit(0);
        }

        if (args[0].equals("-add")) {
            if (args.length > 3) {
                printHelp();
                System.exit(1);
            }
            Indexing i = new Indexing();
            i.indexFiles(args[1], args[2]);
        }
        if (args[0].equals("-overwrite")) {
            if (args.length != 3) {
                printHelp();
                System.exit(1);
            }
            Indexing i = new Indexing();
            i.deleteIndexDir();
            i.indexFiles(args[1], args[2]);
        }
        if (args[0].equals("-search")) {
            Searching s = new Searching();
            String file = null;
            for (int i = 1; i < args.length; i++) {
                if (args[i].equals("-file")) {
                    file = args[i+1];
                    i++;
                }
            }
            if (file!=null) {
                try{
                InputStream is = new FileInputStream(file);
                s.search(is);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    System.exit(1);
                }
            } else {
                s.search(System.in);
            }
        }
        if (args[0].equals("-optimize")) {
            Indexing i = new Indexing();
            i.optimize();
        }
        if (args[0].equals("-deleteindex")) {
            Indexing i = new Indexing();
            i.deleteIndexDir();
        }
        if (args[0].equals("-delete")) {
            if (args.length != 2) {
                printHelp();
                System.exit(1);
            }
            Indexing i = new Indexing();
            i.deleteFiles(args[1]);
        }
        if (args[0].equals("-stats")) {
            Indexing i = new Indexing();
            i.getStats();
        }
        if (args[0].equals("-indocprocess")) {
            if (args.length != 3) {
                printHelp();
                System.exit(1);
            }
            InDocProcessing idp = new InDocProcessing(args[1], args[2]);
            idp.process();
        }
    }

    static void printHelp() {
        System.out.println("Wrong input. Usage:");
        System.out.println("MIaS.jar -add <input_path> <root_dir> where root_dir is an absolute path to a directory in the input_path to determine the relative path that the files will be indexed with");
        System.out.println("MIaS.jar -overwrite <input_path> <root_dir> Overwrites existing index " +
                "or creates a new one and adds file(s). See -add for parameters");
        System.out.println("MIaS.jar -search [-file <queries_file>] [-prefix p] Searches the index for a query in file.");
        System.out.println("MIaS.jar -optimize Optimizes the index for maximum searching performance.");
        System.out.println("MIaS.jar -deleteindex  Deletes the index.");
        System.out.println("MIaS.jar -delete <dir_or_file> Deletes file(s) from index.");
        System.out.println("MIaS.jar -stats Prints statistics about index.");
        System.out.println("MIaS.jar -indocprocess <input_path> <root_dir> where root_dir is an absolute path to a directory in the input_path. Processes math formulae and inserts M-terms into documents created under root_dir.");
    }
}
