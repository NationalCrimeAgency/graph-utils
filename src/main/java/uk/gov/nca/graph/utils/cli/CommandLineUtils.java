package uk.gov.nca.graph.utils.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CommandLineUtils {
    private CommandLineUtils(){}

    public static Option createRequiredOption(String opt, String longOpt, boolean hasArg, String description){
        Option o = new Option(opt, longOpt, hasArg, description);
        o.setRequired(true);

        return o;
    }

    public static CommandLine parseCommandLine(String[] args, Options options, Class<?> clazz, String description){
        CommandLineParser parser = new DefaultParser();

        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            printHelp(clazz.getName(), description, options);
            return null;
        }
    }

    public static void printHelp(String name, String description, Options options){
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp(name,
            "\n"+description+"\n\n",
            options,
            "",
            true);
    }
}
