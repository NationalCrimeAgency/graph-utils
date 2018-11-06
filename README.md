# Graph Utils

Provides a number of Java utilities for interacting with graphs through Tinkerpop,
thereby reducing the amount of boilerplate code required for common tasks such as
getting and copying properties from graphs, splitting graphs into sub-graphs, and
reading and writing graphs to file.

## Command Line Utilities

A number of the functions provided by Graph Utils have been wrapped up to provide
a simple CLI.

The available tools are as follows:

| Tool Name | Description |
| --- | --- |
| CleanGraph | Take a file and remove any control characters from it |
| ConvertGraph | Convert a graph between two graph formats |
| ExportGraph | Export a graph to file |
| ImportGraph | Import a graph into an existing graph without merging |
| SplitGraph | Split a graph into multiple subgraphs |

Running the above commands without any options will print out the available flags
and input parameters. For example:

    $ java -cp utils-1.1-SNAPSHOT-shaded.jar uk.gov.nca.graph.utils.ConvertGraph
    
    usage: uk.gov.nca.graph.utils.cli.ConvertGraph -f <arg> -g <arg> -i <arg>
           -o <arg>
    
    Convert a graph file between formats
    
     -f,--inputformat <arg>    The format of the input file, either GraphML,
                               GraphSON or Gryo
     -g,--outputformat <arg>   The format of the output file, either GraphML,
                               GraphSON or Gryo
     -i,--input <arg>          The input file to read the graph from
     -o,--output <arg>         The output file to save the graph to
