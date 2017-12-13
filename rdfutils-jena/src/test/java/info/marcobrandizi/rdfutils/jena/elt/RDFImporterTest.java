package info.marcobrandizi.rdfutils.jena.elt;

import static info.marcobrandizi.rdfutils.jena.elt.JenaIoUtils.getLangOrFormat;
import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.ns;
import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.registerNs;
import static java.lang.String.format;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.Lock;
import org.junit.BeforeClass;
import org.junit.Test;

import info.marcobrandizi.rdfutils.jena.SparqlBasedTester;
import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import junit.framework.Assert;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Dec 2017</dd></dl>
 *
 */
public class RDFImporterTest
{
	@BeforeClass
	public static void initNss() 
	{
		registerNs ( "ex", "http://example.org/" );
		registerNs ( "foaf", "http://xmlns.com/foaf/0.1/" );
		registerNs ( "foaf", "http://xmlns.com/foaf/0.1/" );
		registerNs ( "dbo", "http://dbpedia.org/ontology/" );
		registerNs ( "dbr", "http://dbpedia.org/resource/" );
	}
	
	
	@Test
	public void testBasics ()
	{
		AtomicBoolean flag = new AtomicBoolean ( false ) ;

		RDFImporter importer = new RDFImporter ();
		importer.setConsumer ( model -> 
		{
			flag.set ( true );
			
			SparqlBasedTester tester = new SparqlBasedTester ( model, NamespaceUtils.asSPARQLProlog () );
			tester.ask ( "No :alice!", "ASK {ex:alice a foaf:Person}" );
			tester.ask ( "No :bob!", "ASK {ex:bob a foaf:Person; foaf:name 'Bob'}" );
			tester.ask ( "No :alice knows!", "ASK {ex:alice foaf:knows ex:bob, ex:charlie, ex:snoopy}" );
		});
		
		importer.process ( "target/test-classes/foaf_example.ttl", null, getLangOrFormat ( "TURTLE" ).getRight () );
		assertTrue ( "Processor not invoked!", flag.get () );
	}
	
	
	@Test
	public void testMultiThread ()
	{
		int chunkSize = 10;
		AtomicInteger chunksCount = new AtomicInteger ( 0 );
		Model umodel = ModelFactory.createDefaultModel ();
		
		RDFImporter importer = new RDFImporter ();		
		importer.setDestinationMaxSize ( chunkSize );
		importer.setConsumer ( model -> 
		{
			umodel.enterCriticalSection ( Lock.WRITE );
			umodel.add ( model );
			umodel.leaveCriticalSection ();
			
			chunksCount.getAndIncrement ();
		});
		importer.process ( "target/test-classes/dbpedia_berlin.rdf", null, getLangOrFormat (  "RDFXML"  ).getRight ());
		assertTrue ( "Chunks count < 2", chunksCount.get () > 2 );
		SparqlBasedTester tester = new SparqlBasedTester ( umodel, NamespaceUtils.asSPARQLProlog () );
		
		tester.ask ( "Berlin's label not found", "ASK {dbr:Berlin rdfs:label 'Berlin'@en }" );
		tester.ask ( "Berlin's area not found!", "ASK {dbr:Berlin dbo:areaTotal ?area. FILTER ( xsd:double ( ?area ) = 8.917e+08 ) }" );
		tester.ask ( "Berlin's Cuisine redirection not found!", "ASK {dbr:Cuisine_of_Berlin dbo:wikiPageRedirects dbr:Berlin}" );
	}
}
