package uk.gov.nca.graph.utils.cli;

import static uk.gov.nca.graph.utils.cli.CommandLineUtils.createRequiredOption;
import static uk.gov.nca.graph.utils.cli.CommandLineUtils.parseCommandLine;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.nca.graph.utils.GraphUtils;

public class ConvertGraph {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertGraph.class);

    public static void main(String[] args){
        Options options = new Options();

        options.addOption(createRequiredOption("i", "input", true, "The input file to read the graph from"));
        options.addOption(createRequiredOption("f", "inputformat", true, "The format of the input file, either GraphML, GraphSON or Gryo"));
        options.addOption(createRequiredOption("o", "output", true, "The output file to save the graph to"));
        options.addOption(createRequiredOption("g", "outputformat", true, "The format of the output file, either GraphML, GraphSON or Gryo"));

        CommandLine cmd = parseCommandLine(args, options, ConvertGraph.class, "Convert a graph file between formats");
        if(cmd == null)
            return;

        LOGGER.info("Creating in memory graph");
        Graph graph = TinkerGraph.open();

        GraphUtils.readGraphFile(cmd.getOptionValue('i'), cmd.getOptionValue('f'), graph);
        GraphUtils.writeGraphFile(cmd.getOptionValue('o'), cmd.getOptionValue('f'), graph);

        GraphUtils.closeGraph(graph);
    }
}
