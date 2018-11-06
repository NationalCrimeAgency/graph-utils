/*
National Crime Agency (c) Crown Copyright 2018

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package uk.gov.nca.graph.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for splitting graphs into subgraphs based on their connected components.
 * This is currently incredibly slow for graphs with large components and needs improving.
 */
public class GraphSplitter {
  private static final Logger LOGGER = LoggerFactory.getLogger(GraphSplitter.class);

  private int fileCounter = 0;

  private final File outputDirectory;
  private final String prefix;
  private final String format;
  private final int threshold;

  /**
   * Create a new GraphSplitter with the default threshold of 1000.
   *
   * Format can be one of graphml, graphson or gryo;
   * and will default to graphml if an unrecognised format is provided.
   */
  public GraphSplitter(File outputDirectory, String prefix, String format){
    this(outputDirectory, prefix, format, 1000);
  }

  /**
   * Create a new GraphSplitter with a specified threshold.
   *
   * Format can be one of graphml, graphson or gryo;
   * and will default to graphml if an unrecognised format is provided.
   */
  public GraphSplitter(File outputDirectory, String prefix, String format, int threshold){
    this.outputDirectory = outputDirectory;
    this.prefix = prefix;

    if("graphml".equals(format) || "graphson".equals(format) || "gryo".equals(format)) {
      this.format = format.toLowerCase();
    }else {
      LOGGER.warn("Unrecognised format {}, defaulting to GraphML", format);
      this.format = "graphml";
    }
    this.threshold = threshold;
  }

  /**
   * Split a graph into smaller sub-graphs, by finding connected components and writing
   * each component to an individual file.
   *
   * If a component has fewer nodes than the threshold value, it is added to an aggregated
   * graph which will contain multiple components (until the aggregated graph itself
   * exceeds the threshold).
   */
  public List<File> splitGraph(Graph graph){
    List<File> splitFiles = new ArrayList<>();

    LOGGER.info("Getting graph IDs");
    List<Object> ids = graph.traversal().V().id().toList();

    int counter = 1;
    List<Vertex> aggregatedComponent = new ArrayList<>();

    LOGGER.info("Finding components");
    while(!ids.isEmpty()) {
      LOGGER.debug("Identifying component {}", counter);

      Object initialId = ids.get(0);
      List<Vertex> component = graph.traversal().V(initialId).repeat(__.both().dedup()).emit().dedup().toList();
      if(component.isEmpty())
        component.add(graph.vertices(initialId).next());

      component.forEach(v -> ids.remove(v.id()));

      LOGGER.debug("Component {} fully identified - contains {} vertices", counter,
          component.size());

      if(component.size() < threshold){
        LOGGER.debug("Component {} is under threshold size and will be aggregated before writing to disk", counter);
        aggregatedComponent.addAll(component);

        if(aggregatedComponent.size() >= threshold){
          LOGGER.debug("Aggregated component has exceeded threshold, and will be written to disk");
          splitFiles.add(outputGraphComponent(aggregatedComponent));
          aggregatedComponent = new ArrayList<>();
        }
      }else{
        LOGGER.debug("Writing component {} to disk", counter);
        splitFiles.add(outputGraphComponent(component));
      }

      LOGGER.debug("Finished processing component {}", counter);

      //Provide regular progress updates
      LOGGER.info("{} vertices left to process", ids.size());

      counter++;
    }

    if(!aggregatedComponent.isEmpty()){
      LOGGER.debug("Writing final aggregated component to disk");
      splitFiles.add(outputGraphComponent(aggregatedComponent));
    }

    LOGGER.info("Finished splitting graph - {} sub-graphs produced", splitFiles.size());
    return splitFiles;
  }

  private File outputGraphComponent(List<Vertex> vertices){
    LOGGER.debug("Creating graph for component");

    Graph splitGraph = TinkerGraph.open();
    Map<Vertex, Vertex> mapVertices = new HashMap<>();

    LOGGER.debug("Adding vertices to graph for component");
    for (Vertex v : vertices) {
      Vertex newV = splitGraph.addVertex(v.label());
      ElementUtils.copyProperties(v, newV);
      mapVertices.put(v, newV);
    }

    LOGGER.debug("Adding edges to graph for component");
    for (Entry<Vertex, Vertex> entry : mapVertices.entrySet()) {
      entry.getKey().edges(Direction.OUT).forEachRemaining(e -> {
        Vertex src = entry.getValue();
        Vertex tgt = mapVertices.get(e.inVertex());

        Edge newE = src.addEdge(e.label(), tgt);
        ElementUtils.copyProperties(e, newE);
      });
    }

    LOGGER.debug("Beginning write process for component");
    //Write partition to outputDirectory and save in specified format
    fileCounter++;
    File f = new File(outputDirectory, prefix + "." + fileCounter + "." + format);

    //Check we're not overwriting an existing file
    while (f.exists()) {
      fileCounter++;
      f = new File(outputDirectory, prefix + "." + fileCounter + "." + format);
    }

    GraphUtils.writeGraphFile(f, format, splitGraph);

    return f;
  }
}
