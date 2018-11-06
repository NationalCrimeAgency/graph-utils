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

public class ExportGraph {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportGraph.class);

    public static void main(String[] args){
        Options options = new Options();

        options.addOption(createRequiredOption("c", "inputconfiguration", true, "Configuration file to connect to Gremlin graph"));
        options.addOption(createRequiredOption("o", "output", true, "The output file to save the graph to"));
        options.addOption(createRequiredOption("g", "outputformat", true, "The format of the output file, either GraphML, GraphSON or Gryo"));

        CommandLine cmd = parseCommandLine(args, options, ExportGraph.class, "Export from a Gremlin graph into a file format (e.g. GraphML)");
        if(cmd == null)
            return;

        LOGGER.info("Connecting to Gremlin graph");
        Graph graph = GraphFactory.open(cmd.getOptionValue('i'));

        GraphUtils.writeGraphFile(cmd.getOptionValue('o'), cmd.getOptionValue('f'), graph);

        GraphUtils.closeGraph(graph);
    }
}
