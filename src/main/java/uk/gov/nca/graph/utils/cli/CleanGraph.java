package uk.gov.nca.graph.utils.cli;

import static uk.gov.nca.graph.utils.cli.CommandLineUtils.createRequiredOption;
import static uk.gov.nca.graph.utils.cli.CommandLineUtils.parseCommandLine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanGraph {
    private static final Logger LOGGER = LoggerFactory.getLogger(CleanGraph.class);

    public static void main(String[] args){
        Options options = new Options();

        options.addOption(createRequiredOption("i", "input", true, "File to read dirty input from"));
        options.addOption(createRequiredOption("o", "output", true, "File to write cleaned output to"));

        CommandLine cmd = parseCommandLine(args, options, CleanGraph.class, "Clean a graph file of unsupported characters (e.g. control characters)");
        if(cmd == null)
            return;

        try(
                BufferedReader reader = new BufferedReader(new FileReader(cmd.getOptionValue('i')));
                BufferedWriter writer = new BufferedWriter(new FileWriter(cmd.getOptionValue('o')))
        ){
            int r;
            while ((r = reader.read()) != -1) {
                if(clean(r))
                    writer.write(r);
            }
        }catch (IOException ioe){
            LOGGER.error("Failed to clean file", ioe);
        }

        LOGGER.info("File {} cleaned and written to {}", cmd.getOptionValue('i'), cmd.getOptionValue('o'));

    }

    /**
     * Returns true if the character is not a control character
     */
    protected static boolean clean(int ch){
        return !((ch >= 0 && ch <= 8) || (ch >= 11 && ch <= 31) || ch == 127);
    }

}
