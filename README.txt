MIaS
Indexing util for indexing mathematical documents. Uses Lucene.

Setting up MIaS

1. Absolute path to the main configuration file, mias.properties, needs to be set in the mias.to configuration file located in the .jar archive in the cz/muni/fi/mias directory.
   
Example:
   MIASPATH=/home/MIaS/conf/mias.properties

2. Properties in the mias.properties file need to be set (<key>=<value>):
  INDEXDIR - Path to the directory, where the index is/will be located.
  UPDATE - If TRUE, the files that are already indexed and are about to be indexed again, will be updated. If FALSE, the indexer will skip them and only add new files.
  MAXRESULTS - The maximum number of the results that the system retrieves
  DOCLIMIT - The limit for the number of the documents that are indexed during one run. -1 means no limit.
  MMLDTD - Path to the MathML DTD file used for parsing the files.
  THREADS - Number of threads that will be used for processing.
  
Example:
  INDEXDIR=/home/data/index
  UPDATE=false
  MAXRESULTS=10000
  DOCLIMIT=-1
  THREADS=8

In windows, backslash in paths need to be escaped with a backslash (for example C:\\MIaS\\index)

To run the program, locate the jar file and run the command:
java -jar MIaS.jar <options>

Options are shown when the above command is run without any options or with -h or -help options.
There must be the lib directory containing necessary dependencies located within the same directory as the jar file.

A working example is located in the example dir. The example is configured with relative paths and therefore needs to be run from the example root directory. Documents to index are in the doc directory.

/home/MIaS$ java -jar MIaS-1.5-SNAPSHOT.jar -add doc/ /home/MIaS/

The files will be indexed with the relative path doc/xxx.xhtml. This needs to be known for the correct configuration of WebMIaS.
