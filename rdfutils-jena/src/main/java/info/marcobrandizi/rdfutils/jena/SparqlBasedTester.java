package info.marcobrandizi.rdfutils.jena;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.JenaException;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple helper to verify what a triple store contains.
 * 
 * TODO: for the moment, it only works against a source document, endpoint-based testing in future.
 * 
 * <dl><dt>date</dt><dd>7 Oct 2014</dd></dl> 
 * (Migrated from <a href = "https://github.com/EBIBioSamples/java2rdf">java2rdf</a>)
 * 
 * @author Marco Brandizi
 *
 */
public class SparqlBasedTester
{
	private Model model;
	private String sparqlPrefixes = "";
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	/**
	 * @param model the data source to send queries to.
	 * @param sparqlPrefixes SPARQL prolog defining prefixes that are used in queries	  
	 */
	public SparqlBasedTester ( Model model, String sparqlPrefixes )
	{
		this.model = model == null ? ModelFactory.createDefaultModel () : model;
		if ( this.sparqlPrefixes != null ) this.sparqlPrefixes = sparqlPrefixes;
	}
	
	public SparqlBasedTester ( Model model )
	{
		this ( model, null );
	}

	/**
	 * @param url the path to the RDF source to be tested, which is loaded as RDF data set.
	 * @param sparqlPrefixes see {@link #SparqlBasedTester(Model, String)}	  
	 */
	public SparqlBasedTester ( String url, String sparqlPrefixes )
	{
		this ( (Model) null, sparqlPrefixes );
		model.read ( url );
	}
	
	/**
	 * No prefix defined.
	 */
	public SparqlBasedTester ( String url )
	{
		this ( url, null );
	}
	
	
	/**
	 * Performs a SPARQL ASK test and asserts via JUnit that the result is true.
	 *  
	 * @param errorMessage the error message to report in case of failure
	 * @param sparql the SPARQL/ASK query to run against the triple store passed to the class constructor.
	 */
	public void ask ( String errorMessage, String sparql )
	{
		sparql = sparqlPrefixes + sparql;
		try {
			Assert.assertTrue ( errorMessage, SparqlUtils.ask ( sparql, model ) );
		}
		catch ( JenaException ex ) 
		{
			log.error ( "Error while doing SPARQL, query is: {}, error is: {}", sparql, ex.getMessage () );
			throw ex;
		}
	}
}
