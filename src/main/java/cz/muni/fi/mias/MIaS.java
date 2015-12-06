package cz.muni.fi.mias;

import cz.muni.fi.mias.indexing.Indexing;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Main class witch main method.
 *
 * @author Martin Liska
 * @since 14.5.2010
 */
public class MIaS {

    public static void main(String[] args) {
        Options options = Settings.getMIaSOptions();
        try {
            if (args.length == 0) {
                printHelp(options);
                System.exit(1);
            }
            
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            
            Settings.init(cmd.getOptionValue(Settings.OPTION_CONF));
            
            if (cmd.hasOption(Settings.OPTION_ADD)) {
                Indexing i = new Indexing();
                i.indexFiles(cmd.getOptionValues(Settings.OPTION_ADD)[0], cmd.getOptionValues(Settings.OPTION_ADD)[1]);
            }
            if (cmd.hasOption(Settings.OPTION_OVERWRITE)) {
                Indexing i = new Indexing();
                i.deleteIndexDir();
                i.indexFiles(cmd.getOptionValues(Settings.OPTION_OVERWRITE)[0], cmd.getOptionValues(Settings.OPTION_OVERWRITE)[1]);
            }
            if (cmd.hasOption(Settings.OPTION_OPTIMIZE)) {
                Indexing i = new Indexing();
                i.optimize();
            }
            if (cmd.hasOption(Settings.OPTION_DELETEINDEX)) {
                Indexing i = new Indexing();
                i.deleteIndexDir();
            }
            if (cmd.hasOption(Settings.OPTION_DELETE)) {
                Indexing i = new Indexing();
                i.deleteFiles(cmd.getOptionValue(Settings.OPTION_DELETE));
            }
            if (cmd.hasOption(Settings.OPTION_STATS)) {
                Indexing i = new Indexing();
                i.getStats();
            }
            if (cmd.hasOption(Settings.OPTION_INDOCPROCESS)) {
                InDocProcessing idp = new InDocProcessing(cmd.getOptionValues(Settings.OPTION_INDOCPROCESS)[0], cmd.getOptionValues(Settings.OPTION_INDOCPROCESS)[1]);
                idp.process();
            }
        } catch (ParseException ex) {
            printHelp(options);
        }
    }

    static void printHelp(Options options) {        
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "MIaS", options );
    }
}
