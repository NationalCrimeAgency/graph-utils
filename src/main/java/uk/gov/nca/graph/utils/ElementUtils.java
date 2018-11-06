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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * Utility class for working with Elements (i.e. vertices and edges) from
 * Gremlin graphs
 */
public class ElementUtils {
    private ElementUtils(){}

    /**
     * Copy all properties from the source element to the target element
     */
    public static void copyProperties(Element eSource, Element eTarget){
        for (Iterator<? extends Property> it = eSource.properties(); it.hasNext(); ) {
            copyProperty(it.next(), eTarget);
        }
    }

    /**
     * Copy a property to the target element
     */
    public static void copyProperty(Property pSource, Element eTarget){
        //Check we're not overwriting
        if(eTarget.property(pSource.key()).isPresent()){
            Object o = eTarget.value(pSource.key());

            //If the two objects are equal, don't duplicate
            if(o.equals(pSource.value()))
                return;

            List<Object> properties = new ArrayList<>();
            if(o instanceof List){
                //If the existing property is a list, add all values
                properties.addAll((List<Object>)o);
            }else{
                //Otherwise just add the singular current value
                properties.add(o);
            }

            Object pValue = pSource.value();
            if(pValue instanceof List){
                //If the new value is a list, filter out existing values (don't duplicate)
                //and add values to list
                List<Object> pValueList = (List<Object>) pValue;
                pValueList.stream()
                    .filter(val -> !properties.contains(val))
                    .forEach(properties::add);
            }else {
                //If the new value is an object, check we don't duplicate
                if(!properties.contains(pValue))
                    properties.add(pValue);
            }

            eTarget.property(pSource.key(), properties);
        }else {
            eTarget.property(pSource.key(), pSource.value());
        }
    }

    /**
     * Get a property from an element if it is present, or return null otherwise
     */
    public static Object getProperty(Element e, String key){
        Property<?> prop = e.property(key);

        if(!prop.isPresent())
            return null;

        return prop.value();
    }

    /**
     * Return true if an element has all of the named properties
     */
    public static boolean hasProperty(Element e, String... keys){
        for(String key : keys){
            if(!e.property(key).isPresent())
                return false;
        }

        return true;
    }

    /**
     * Retrieve a property from a vertex linked by a specified (out) edge label to an original vertex.
     *
     * If more than one linked vertex is found, then a list of properties from all linked
     * vertices.
     *
     * If no linked vertices are found, then null is returned.
     */
    public static Object getLinkedProperty(Graph graph, Vertex v, String edgeLabel, String property){
        return getValuesFromProperties(graph.traversal().V(v.id()).out(edgeLabel).properties(property).toList());
    }

    /**
     * Retrieve a property from a vertex with a specified label linked by a specified edge label to an original vertex.
     *
     * If more than one linked vertex is found, then a list of properties from all linked
     * vertices.
     *
     * If no linked vertices are found, then null is returned.
     */
    public static Object getLinkedProperty(Graph graph, Vertex v, String edgeLabel, String vertexLabel, String property){
        return getValuesFromProperties(graph.traversal().V(v.id())
            .both(edgeLabel).hasLabel(vertexLabel).properties(property).toList());
    }

    private static Object getValuesFromProperties(List<? extends Property<Object>> properties){
        if(properties.size() > 1){
            List<Object> values = new ArrayList<>();
            for(Property<Object> prop : properties) {
                Object val = prop.value();
                if(val != null)
                    values.add(val);
            }

            if(values.isEmpty())
                return null;

            return values;
        }else if(properties.size() == 1){
            return properties.get(0).value();
        }else{
            return null;
        }
    }

    /**
     * Get all vertices that are one hop away from an initial vertex,
     * specifying the edge label and direction, and the target vertex label.
     */
    public static List<Vertex> getOneHopVertices(Graph g, Vertex v,
        String edgeLabel, Direction edgeDirection,
        String targetLabel){

        return g.traversal().V(v.id()).to(edgeDirection, edgeLabel)
            .hasLabel(targetLabel)
            .toList();
    }

    /**
     * Get all vertices that are two hops away from an initial vertex,
     * specifiying the edge labels and directions, and the intermediate vertex label.
     */
    public static List<Vertex> getTwoHopVertices(Graph g, Vertex v,
        String edgeLabel1, Direction edgeDirection1,
        String intermediateNodeLabel,
        String edgeLabel2, Direction edgeDirection2){

        return getTwoHopVertices(g, v, edgeLabel1, edgeDirection1, intermediateNodeLabel, edgeLabel2, edgeDirection2, null);
    }

    /**
     * Get all vertices that are two hops away from an initial vertex,
     * specifiying the edge labels and directions, the intermediate vertex label,
     * and the target vertex label.
     */
    public static List<Vertex> getTwoHopVertices(Graph g, Vertex v,
        String edgeLabel1, Direction edgeDirection1,
        String intermediateNodeLabel,
        String edgeLabel2, Direction edgeDirection2,
        String targetNodeLabel){

        if(targetNodeLabel != null){
            List<Vertex> vertices = g.traversal().V(v.id()).as("orig").to(edgeDirection1, edgeLabel1)
                .hasLabel(intermediateNodeLabel)
                .to(edgeDirection2, edgeLabel2)
                .hasLabel(targetNodeLabel)
                .toList();
            vertices.remove(v); //Remove the case where our hop has bought us back to the original vertex
            return vertices;
        }else {
            List<Vertex> vertices = g.traversal().V(v.id()).to(edgeDirection1, edgeLabel1)
                .hasLabel(intermediateNodeLabel)
                .to(edgeDirection2, edgeLabel2)
                .toList();
            vertices.remove(v); //Remove the case where our hop has bought us back to the original vertex
            return vertices;
        }
    }
}
