package uk.gov.nca.graph.utils.cli;

import static uk.gov.nca.graph.utils.cli.CommandLineUtils.createRequiredOption;
import static uk.gov.nca.graph.utils.cli.CommandLineUtils.parseCommandLine;

import java.io.File;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.nca.graph.utils.GraphSplitter;
import uk.gov.nca.graph.utils.GraphUtils;

public class SplitGraph {
  private static final Logger LOGGER = LoggerFactory.getLogger(SplitGraph.class);

  public static void main(String[] args){
    Options options = new Options();

    options.addOption(createRequiredOption("i", "input", true, "The input file to read the graph from"));
    options.addOption(createRequiredOption("f", "inputformat", true, "The format of the input file, either GraphML, GraphSON or Gryo"));
    options.addOption(createRequiredOption("o", "output", true, "The output directory to save the graph splits to"));
    options.addOption(createRequiredOption("g", "outputformat", true, "The format of the output file, either GraphML, GraphSON or Gryo"));
    options.addOption(new Option("t", "threshold", true, "The minimum number of vertices a component should have before outputting (if under this, components will be aggregated)"));

    CommandLine cmd = parseCommandLine(args, options, SplitGraph.class, "Split a graph into multiple components");
    if(cmd == null)
      return;

    LOGGER.info("Creating in memory graph");
    Graph graph = TinkerGraph.open();

    File inputFile = new File(cmd.getOptionValue('i'));
    String prefix = inputFile.getName().replaceFirst("[.][^.]+$", "");  //Replace everything after the last dot (i.e. remove extension)

    GraphUtils.readGraphFile(inputFile, cmd.getOptionValue('f'), graph);

    int threshold = 1000;
    if(cmd.hasOption('t')) {
      try {
        threshold = Integer.parseInt(cmd.getOptionValue('t'));
      } catch (NumberFormatException nfe) {
        LOGGER.warn("Unable to parse threshold value. Default value of {} will be used", threshold);
      }
    }

    GraphSplitter gs = new GraphSplitter(new File(cmd.getOptionValue('o')), prefix, cmd.getOptionValue('g'), threshold);
    List<File> files = gs.splitGraph(graph);

    if(!files.isEmpty()) {
      LOGGER.info("Graph split into {} components, and saved to the following files", files.size());
      files.forEach(f -> LOGGER.info(f.getAbsolutePath()));
    }

    GraphUtils.closeGraph(graph);
  }
}
