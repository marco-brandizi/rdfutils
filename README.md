# rdfutils

Utilities to manage RDF and RDF frameworks.

This library is arranges as a core package, where abstract interfaces are defined to access function from existing RDF frameworks (e.g., Jena, RDF4J) and a set of framework-specific package. In particular, the [rdfutils-commonsrdf](rdfutils-commonsrdf) module adds up an indirection layer, the [Commons-RDF library](https://commons.apache.org/proper/commons-rdf), to make it possible to use our utilities in an RDF framework-independent way, or, to put it in another way, to plug our utilities on the framework you prefer.
  
