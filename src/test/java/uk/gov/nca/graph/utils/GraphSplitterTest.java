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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Test;

public class GraphSplitterTest {

  @Test
  public void testSingular() throws Exception{
    Graph g = TinkerGraph.open();

    createGraph(g);

    File outputDir = Files.createTempDirectory("graphsplitter").toFile();

    GraphSplitter gs = new GraphSplitter(outputDir, "graph", "graphml", 1);

    List<File> results = gs.splitGraph(g);
    assertEquals(3, results.size());  //3 Singular

    //TODO: Validate files contain expected structure (has been manually verified for initial code)

    results.forEach(File::delete);
    outputDir.delete();

    g.close();
  }

  @Test
  public void testMixed() throws Exception{
    Graph g = TinkerGraph.open();

    createGraph(g);

    File outputDir = Files.createTempDirectory("graphsplitter").toFile();

    GraphSplitter gs = new GraphSplitter(outputDir, "graph", "graphml", 3);

    List<File> results = gs.splitGraph(g);
    assertEquals(2, results.size());  //1 Aggregated, 1 Singular

    //TODO: Validate files contain expected structure (has been manually verified for initial code)

    results.forEach(File::delete);
    outputDir.delete();

    g.close();
  }

  @Test
  public void testAggregated() throws Exception{
    Graph g = TinkerGraph.open();

    createGraph(g);

    File outputDir = Files.createTempDirectory("graphsplitter").toFile();

    GraphSplitter gs = new GraphSplitter(outputDir, "graph", "graphml");

    List<File> results = gs.splitGraph(g);
    assertEquals(1, results.size());  //1 Aggregated

    //TODO: Validate files contain expected structure (has been manually verified for initial code)

    results.forEach(File::delete);
    outputDir.delete();

    g.close();
  }

  private void createGraph(Graph g){
    // G -> A -> B -> C
    // D -> E
    // F

    g.traversal().addV().property(T.id, "A").property("greeting", "Hello World!").as("a").
        addV().property(T.id, "B").property("greeting", "Bonjour le Monde!").as("b").
        addV().property(T.id, "C").property("greeting", "Hallo Welt!").as("c").
        addV().property(T.id, "D").property("greeting", "Hola Mundo!").as("d").
        addV().property(T.id, "E").property("greeting", "Hallo Wereld!").as("e").
        addV().property(T.id, "F").property("greeting", "Olá Mundo!").
        addV().property(T.id, "G").as("g").
        addE("link").from("a").to("b").
        addE("link").from("b").to("c").
        addE("link").from("d").to("e").
        addE("link").from("g").to("a").iterate();
  }

}
