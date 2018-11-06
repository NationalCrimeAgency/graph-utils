package uk.gov.nca.graph.utils.cli;

import static uk.gov.nca.graph.utils.cli.CommandLineUtils.createRequiredOption;
import static uk.gov.nca.graph.utils.cli.CommandLineUtils.parseCommandLine;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.util.GraphFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.nca.graph.utils.GraphUtils;

public class ImportGraph {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportGraph.class);

    public static void main(String[] args){
        Options options = new Options();

        options.addOption(createRequiredOption("d", "outputconfiguration", true, "Configuration file to connect to Gremlin graph"));
        options.addOption(createRequiredOption("i", "input", true, "The input file to read the graph from"));
        options.addOption(createRequiredOption("f", "inputformat", true, "The format of the input file, either GraphML, GraphSON or Gryo"));

        CommandLine cmd = parseCommandLine(args, options, ImportGraph.class, "Import to a Gremlin graph from a file");
        if(cmd == null)
            return;

        LOGGER.info("Connecting to Gremlin graph");
        Graph graph = GraphFactory.open(cmd.getOptionValue('o'));

        GraphUtils.readGraphFile(cmd.getOptionValue('i'), cmd.getOptionValue('f'), graph);

        GraphUtils.closeGraph(graph);
    }
}
