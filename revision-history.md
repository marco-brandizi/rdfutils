# Revision History

*This file was last reviewed on 2025-08-27*. **Please, keep this note updated**.

## 6.0.1-SNAPSHOT
* Current snapshot.


## 6.0
* README upgraded (to reflect the removal of CommonsRDF).
* Jena upgraded to 5.4.0


## 5.0.1
* Various dependencies upgraded.


## 5.0
* **WARNING**: Maven repository migrated to [my personal artifactory](https://artifactory.marcobrandizi.info/#/public).
* Migrated to Java 17. **WARNING: no backward compatibility guaranteed**.


## 4.1
* Fixing issue with auto-trimming values in checkNonNullTriple().


## 4.0.3
* Fixing issue with dependency management.


## 4.0.2
* Fixing issue with dependency management.


## 4.0.1
* Updating jutils dependency.


## 4.0
* Apache commons RDF removed.
* Various dependencies upgraded.
* Various dependencies factorised in the main POM, to be able to support other dependencies (java2rdf).


## 3.0 
* **FROM NOW ON, JDK < 11 IS NO LONGER SUPPORTED**. rdfutils will possibly work with 1.8 for 
  a while (until we start introducing incompatible changes), but that's not officially supported.


## 2.1.2-SNAPSHOT
* Just started


## 2.1.1
* Release to link jutils 9.1


## 2.1
* (rdf-utils-jena) More interfaces added to `info.marcobrandizi.rdfutils.jena.SparqlBasedTester`.
* Changes made to align recent improvements in `uk.ac.ebi.utils.threading.batchproc`
  
 
## 2.0
* Module for Apache Commons RDF added.
* (rdf-utils-jena) `SPARQLUtils` added.
* (rdf-utils-jena) `RDFProcessor`, `RDFImporter`, `TDBLoadingHandler` added.


## 1.0
* Created