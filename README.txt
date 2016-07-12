MIaS
Indexing util for indexing mathematical documents. Uses Lucene.

Setting up MIaS main properties file

1. Create a file mias.properties in some location. For example /home/MIaS/conf/mias.properties

2. Properties in the mias.properties file need to be set (<key>=<value>):
  INDEXDIR - Path to the directory, where the index is/will be located.
  UPDATE - If TRUE, the files that are already indexed and are about to be indexed again, will be updated. If FALSE, the indexer will skip them and only add new files.
  MAXRESULTS - The maximum number of the results that the system retrieves
  DOCLIMIT - The limit for the number of the documents that are indexed during one run. -1 means no limit.
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

Example:

/home/MIaS$ java -jar MIaS.jar -conf /home/MIaS/conf/mias.properties -add /home/MIaS/doc/ /home/MIaS/

The files will be indexed with the relative path doc/xxx.xhtml. This needs to be known for the correct configuration of WebMIaS.
