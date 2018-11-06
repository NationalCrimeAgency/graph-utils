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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Test;

public class ElementUtilsTest {

  @Test
  public void testCopyProperties() throws Exception{
    Graph g = TinkerGraph.open();

    Vertex v1 = g.addVertex(T.label, "Person",
        "name", "Michelle",
        "age", 37,
        "gender", "female",
        "britishNational", true,
        "favouriteAnimals", Arrays.asList("dog", "cat"));

    Vertex v2 = g.addVertex(T.label, "Person",
        "name", "Shell",
        "gender", "female",
        "favouriteAnimals", Arrays.asList("dog", "fish"));

    assertTrue(v2.property("name").isPresent());
    assertFalse(v2.property("age").isPresent());
    assertTrue(v2.property("gender").isPresent());
    assertFalse(v2.property("britishNational").isPresent());
    assertTrue(v2.property("favouriteAnimals").isPresent());

    ElementUtils.copyProperties(v1, v2);

    assertTrue(v2.property("name").isPresent());
    assertEquals(Arrays.asList("Shell", "Michelle"), v2.property("name").value());
    assertTrue(v2.property("age").isPresent());
    assertEquals(37, v2.property("age").value());
    assertTrue(v2.property("gender").isPresent());
    assertEquals("female", v2.property("gender").value());
    assertTrue(v2.property("britishNational").isPresent());
    assertEquals(true, v2.property("britishNational").value());
    assertTrue(v2.property("favouriteAnimals").isPresent());
    assertEquals(Arrays.asList("dog", "fish", "cat"), v2.property("favouriteAnimals").value());

    g.close();
  }

  @Test
  public void testCopyProperty() throws Exception{
    Graph g = TinkerGraph.open();

    Vertex v1 = g.addVertex(T.label, "Person", "name", "Michelle");
    Vertex v2 = g.addVertex(T.label, "Person", "name", "Shell");

    ElementUtils.copyProperty(v1.property("name"), v2);

    assertTrue(v2.property("name").isPresent());
    assertEquals(Arrays.asList("Shell", "Michelle"), v2.property("name").value());

    g.close();
  }

  @Test
  public void testGetProperties() throws Exception{
    Graph g = TinkerGraph.open();

    Vertex v1 = g.addVertex(T.label, "Person",
        "name", "Michelle",
        "age", 37,
        "gender", "female",
        "britishNational", true);


    assertEquals("Michelle", ElementUtils.getProperty(v1, "name"));
    assertEquals(37, ElementUtils.getProperty(v1, "age"));
    assertEquals("female", ElementUtils.getProperty(v1, "gender"));
    assertEquals(true, ElementUtils.getProperty(v1, "britishNational"));
    assertNull(ElementUtils.getProperty(v1, "location"));

    g.close();
  }

  @Test
  public void testHasProperties() throws Exception{
    Graph g = TinkerGraph.open();

    Vertex v1 = g.addVertex(T.label, "Person",
        "name", "Michelle",
        "age", 37,
        "gender", "female",
        "britishNational", true);


    assertTrue(ElementUtils.hasProperty(v1, "name"));
    assertTrue(ElementUtils.hasProperty(v1, "age"));
    assertTrue(ElementUtils.hasProperty(v1, "gender"));
    assertTrue(ElementUtils.hasProperty(v1, "britishNational"));
    assertFalse(ElementUtils.hasProperty(v1, "location"));

    g.close();
  }

  @Test
  public void testGetLinkedProperty() throws Exception{
    Graph g = TinkerGraph.open();

    Vertex v1 = g.addVertex(T.label, "Person");
    Vertex v2 = g.addVertex(T.label, "Email", "identifier", "michelle@example.com");
    v1.addEdge("email", v2);

    Vertex v3 = g.addVertex(T.label, "Person");
    Vertex v4 = g.addVertex(T.label, "Email", "identifier", "admin@example.com");
    v3.addEdge("email", v2);
    v3.addEdge("email", v4);

    assertEquals("michelle@example.com", ElementUtils.getLinkedProperty(g, v1, "email", "identifier"));

    assertEquals(Arrays.asList("michelle@example.com", "admin@example.com"),
        ElementUtils.getLinkedProperty(g, v3, "email", "identifier"));

    assertNull(ElementUtils.getLinkedProperty(g, v2, "email", "identifier"));
    assertNull(ElementUtils.getLinkedProperty(g, v2, "parent", "identifier"));

    g.close();
  }

  @Test
  public void testGetOneHopVertices() throws Exception{
    Graph g = TinkerGraph.open();

    Vertex v1 = g.addVertex(T.label, "Person");
    Vertex v2 = g.addVertex(T.label, "Person");
    Vertex v3 = g.addVertex(T.label, "Person");
    v1.addEdge("friendOf", v2);
    v1.addEdge("friendOf", v3);

    assertEquals(Arrays.asList(v2, v3), ElementUtils.getOneHopVertices(g, v1, "friendOf", Direction.OUT, "Person"));
    assertEquals(Arrays.asList(v2, v3), ElementUtils.getOneHopVertices(g, v1, "friendOf", Direction.BOTH, "Person"));

    assertTrue(ElementUtils.getOneHopVertices(g, v1, "friendOf", Direction.IN, "Person").isEmpty());
    assertTrue(ElementUtils.getOneHopVertices(g, v1, "friendOf", Direction.OUT, "Email").isEmpty());
    assertTrue(ElementUtils.getOneHopVertices(g, v1, "parentOf", Direction.OUT, "Person").isEmpty());

    g.close();
  }

  @Test
  public void testGetTwoHopVertices() throws Exception{
    Graph g = TinkerGraph.open();

    Vertex v1 = g.addVertex(T.label, "Person");
    Vertex v2 = g.addVertex(T.label, "Person");
    Vertex v3 = g.addVertex(T.label, "Person");
    Vertex v4 = g.addVertex(T.label, "Person");
    v1.addEdge("friendOf", v2);
    v2.addEdge("friendOf", v3);
    v2.addEdge("friendOf", v4);

    assertEquals(Arrays.asList(v3, v4),
        ElementUtils.getTwoHopVertices(g, v1, "friendOf", Direction.OUT, "Person", "friendOf", Direction.OUT));
    assertEquals(Arrays.asList(v3, v4),
        ElementUtils.getTwoHopVertices(g, v1, "friendOf", Direction.OUT, "Person", "friendOf", Direction.OUT, "Person"));
    assertEquals(Arrays.asList(v3, v4),
        ElementUtils.getTwoHopVertices(g, v1, "friendOf", Direction.BOTH, "Person", "friendOf", Direction.BOTH));
    assertEquals(Arrays.asList(v3, v4),
        ElementUtils.getTwoHopVertices(g, v1, "friendOf", Direction.BOTH, "Person", "friendOf", Direction.BOTH, "Person"));

    assertTrue(ElementUtils.getTwoHopVertices(g, v1, "friendOf", Direction.OUT, "Person", "friendOf", Direction.IN).isEmpty());
    assertTrue(ElementUtils.getTwoHopVertices(g, v1, "friendOf", Direction.IN, "Person", "friendOf", Direction.OUT).isEmpty());
    assertTrue(ElementUtils.getTwoHopVertices(g, v1, "friendOf", Direction.OUT, "Person", "parentOf", Direction.OUT).isEmpty());
    assertTrue(ElementUtils.getTwoHopVertices(g, v1, "friendOf", Direction.OUT, "Email", "friendOf", Direction.OUT).isEmpty());
    assertTrue(ElementUtils.getTwoHopVertices(g, v1, "parentOf", Direction.OUT, "Person", "friendOf", Direction.OUT).isEmpty());

    assertTrue(ElementUtils.getTwoHopVertices(g, v1, "friendOf", Direction.OUT, "Person", "friendOf", Direction.IN, "Person").isEmpty());
    assertTrue(ElementUtils.getTwoHopVertices(g, v1, "friendOf", Direction.IN, "Person", "friendOf", Direction.OUT, "Person").isEmpty());
    assertTrue(ElementUtils.getTwoHopVertices(g, v1, "friendOf", Direction.OUT, "Person", "friendOf", Direction.OUT, "Email").isEmpty());
    assertTrue(ElementUtils.getTwoHopVertices(g, v1, "friendOf", Direction.OUT, "Person", "parentOf", Direction.OUT, "Person").isEmpty());
    assertTrue(ElementUtils.getTwoHopVertices(g, v1, "friendOf", Direction.OUT, "Email", "friendOf", Direction.OUT, "Person").isEmpty());
    assertTrue(ElementUtils.getTwoHopVertices(g, v1, "parentOf", Direction.OUT, "Person", "friendOf", Direction.OUT, "Person").isEmpty());

    g.close();
  }
}
