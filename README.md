MIaS – Math-aware full-text search engine
=========================================
[![ci](https://github.com/MIR-MU/MIaS/workflows/Build/badge.svg)][ci]

 [ci]: https://github.com/MIR-MU/MIaS/actions (GitHub Actions)

[MIaS][] (Math Indexer and Searcher) is a math-aware full-text search engine.
It is based on [Apache Lucene][lucene]; however, its maths processing
capabilities are standalone and can be easily integrated into any
[Lucene/Solr][solr] based system, as in [EuDML][] search service.

 [eudml]: https://eudml.org/
 [lucene]: https://lucene.apache.org/
 [mias]: https://mir.fi.muni.cz/mias/
 [solr]: https://lucene.apache.org/solr/

Usage
=====
[Install Docker][], place your dataset to a directory named `dataset/`, and
index your dataset using the [miratmu/mias][] Docker image:

```sh
docker run -v "$PWD"/dataset:/dataset:ro -v "$PWD"/index:/index:rw \
    --rm miratmu/mias
```

When running MIaS, you may control the configuration of the Java Virtual
Machine by changing the `JAVA_OPTS` environmental veriable:
`-e JAVA_OPTS='-Xms128g -Xmx192g'`.

 [install docker]: https://docs.docker.com/get-docker/
 [miratmu/mias]: https://hub.docker.com/r/miratmu/mias/tags

Usage without Docker
====================
Setting up `mias.properties`
----------------------------
Create a file named `mias.properties` in some location, e.g.
`/home/MIaS/conf/mias.properties`, and set up the following properties:

  - `INDEXDIR` – Path to the directory, where the index is / will be located.
  - `UPDATE` – If `TRUE`, the files that are already indexed and are about to
    be indexed again, will be updated. If `FALSE`, the indexer will skip them
    and only add new files.
  - `MAXRESULTS` – The maximum number of results that the system retrieves.
  - `DOCLIMIT` - The limit for the number of the documents that are indexed
    during one run. `-1` means no limit.
  - `THREADS` - The number of threads that will be used for processing.

The resulting file might have the following content:

```
INDEXDIR=/home/data/index
UPDATE=false
MAXRESULTS=10000
DOCLIMIT=-1
THREADS=8
```

In Windows, backslashes in paths need to be escaped, e.g. you would insert
`C:\\MIaS\\index` instead of `C:\MIaS\index`.

Running MIaS
------------
To run MIaS, locate the JAR file of MIaS and run the following command:

```
java -jar MIaS.jar [OPTIONS]
```

To see the available options, run the following command:

```
java -jar MIaS.jar -help
```

There must exist a directory named `lib` containing necessary dependencies
located within the same directory as the jar file.

Citing MIaS
===========
Text
----
SOJKA, Petr and Martin LÍŠKA. The Art of Mathematics Retrieval. In Matthew R.
B. Hardy, Frank Wm. Tompa. *Proceedings of the 2011 ACM Symposium on Document
Engineering.* Mountain View, CA, USA: ACM, 2011. p. 57–60. ISBN
978-1-4503-0863-2. doi:[10.1145/2034691.2034703][doi].

 [doi]: http://doi.org/10.1145/2034691.2034703

BibTeX
------
``` bib
@inproceedings{doi:10.1145:2034691.2034703,
     author = "Petr Sojka and Martin L\'{i}\v{s}ka",
      title = "{The Art of Mathematics Retrieval}",
  booktitle = "{Proceedings of the ACM Conference on Document Engineering,
                DocEng 2011}",
  publisher = "{Association of Computing Machinery}",
    address = "{Mountain View, CA}",
       year = 2011,
      month = Sep,
       isbn = "978-1-4503-0863-2",
      pages = "57--60",
        url = {http://doi.acm.org/10.1145/2034691.2034703},
        doi = {10.1145/2034691.2034703},
   abstract = {The design and architecture of MIaS (Math Indexer and Searcher), 
               a system for mathematics retrieval is presented, and design
               decisions are discussed. We argue for an approach based on
               Presentation MathML using a similarity of math subformulae. The
               system was implemented as a math-aware search engine based on
               the state-of-the-art system Apache Lucene. Scalability issues
               were checked against more than 400,000 arXiv documents with 158
               million mathematical formulae. Almost three billion MathML
               subformulae were indexed using a Solr-compatible Lucene.},
}
```
