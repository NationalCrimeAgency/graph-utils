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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for performing IO operations on graphs
 */
public class GraphUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphUtils.class);

    private GraphUtils(){}

    /**
     * Read a graph from a file, with a format of either graphml, graphson or gryo.
     * The batch size (i.e. how many to read at a time) is set to a default value of 10000.
     */
    public static boolean readGraphFile(String file, String format, Graph graph){
        return readGraphFile(new File(file), format, graph);
    }

    /**
     * Read a graph from a File, with a format of either graphml, graphson or gryo.
     * The batch size (i.e. how many to read at a time) is set to a default value of 10000.
     */
    public static boolean readGraphFile(File file, String format, Graph graph){
        LOGGER.info("Reading graph file {}", file);
        try (final InputStream stream = new FileInputStream(file)) {
            return readGraph(stream, format, graph);
        } catch (IOException ioe) {
            LOGGER.error("Unable to read graph from disk", ioe);
            return false;
        }
    }

    /**
     * Read a graph from an InputStream, with a format of either graphml, graphson or gryo.
     * The batch size (i.e. how many to read at a time) is set to a default value of 10000.
     */
    public static boolean readGraph(InputStream inputStream, String format, Graph graph){
        return readGraph(inputStream, format, graph, 10000);
    }

    /**
     * Read a graph from an InputStream, with a format of either graphml, graphson or gryo.
     * The batch size (i.e. how many to read at a time) can be explicitly set.
     */
    public static boolean readGraph(InputStream inputStream, String format, Graph graph, long batchSize){
        try {
            if ("graphml".equalsIgnoreCase(format)) {
                LOGGER.info("Reading from GraphML stream");
                graph.io(IoCore.graphml()).reader().batchSize(batchSize).create().readGraph(inputStream, graph);
            } else if ("graphson".equalsIgnoreCase(format)) {
                LOGGER.info("Reading from GraphSON stream");
                graph.io(IoCore.graphson()).reader().batchSize(batchSize).create().readGraph(inputStream, graph);
            } else if ("gryo".equalsIgnoreCase(format)) {
                LOGGER.info("Reading from Gryo stream");
                graph.io(IoCore.gryo()).reader().batchSize(batchSize).create().readGraph(inputStream, graph);
            } else {
                return false;
            }
        }catch (IOException ioe) {
            LOGGER.error("Unable to read graph", ioe);
            return false;
        }

        return true;
    }

    /**
     * Write a graph to file, with a format of either graphml, graphson or gryo.
     */
    public static boolean writeGraphFile(String file, String format, Graph graph){
        return writeGraphFile(new File(file), format, graph);
    }

    /**
     * Write a graph to file, with a format of either graphml, graphson or gryo.
     */
    public static boolean writeGraphFile(File file, String format, Graph graph){
        LOGGER.info("Writing graph file {}", file);
        try {
            if ("graphml".equalsIgnoreCase(format)) {
                LOGGER.info("Outputting graph as GraphML to {}",file);
                graph.io(IoCore.graphml()).writer().create().writeGraph(new FileOutputStream(file), graph);
            } else if ("graphson".equalsIgnoreCase(format)) {
                LOGGER.info("Outputting graph as GraphSON to {}",file);
                graph.io(IoCore.graphson()).writer().create().writeGraph(new FileOutputStream(file), graph);
            } else if("gryo".equalsIgnoreCase(format)) {
                LOGGER.info("Outputting graph as Gryo to {}",file);
                graph.io(IoCore.gryo()).writer().create().writeGraph(new FileOutputStream(file), graph);
            }else{
                return false;
            }

            return true;
        } catch(IOException ioe){
            LOGGER.error("Unable to save graph to disk", ioe);
            return false;
        }
    }

    /**
     * Close a graph, silently capturing any exceptions
     */
    public static void closeGraph(Graph graph){
        try {
            graph.close();
        }catch(Exception e){
            LOGGER.debug("Error occurred closing graph", e);
        }
    }

    /**
     * Commits and closes the current transaction open on graph
     */
    public static void commitGraph(Graph graph){
        try {
            graph.tx().commit();
            graph.tx().close();
        }catch (UnsupportedOperationException uoe){
            LOGGER.debug("Graph does not support transactions, commit call failed (but data likely persisted anyway)", uoe);
        }
    }

    /**
     * Clear a graph by dropping each node
     */
    public static void clearGraph(Graph graph){
        graph.traversal().V().drop().iterate();
        commitGraph(graph);
    }
}
