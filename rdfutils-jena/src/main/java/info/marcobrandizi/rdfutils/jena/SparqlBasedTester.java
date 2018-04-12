package info.marcobrandizi.rdfutils.jena;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.JenaException;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.utils.exceptions.UncheckedFileNotFoundException;

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
	
	/**
	 * Like {@link #ask(String, String)}, but gets the SPARQL query from a file. 
	 */
	public void askFromFile ( String errorMessage, String sparqlPath )
	{
		try {
			ask ( errorMessage, IOUtils.toString ( new FileReader ( sparqlPath ) ) );
		}
		catch ( FileNotFoundException ex )
		{
			throw new UncheckedFileNotFoundException ( 
				"File '" + sparqlPath + "' not found while running SparqlBasedTester: " + ex.getMessage (), 
				ex 
			);
		}
		catch ( IOException ex ) {
			throw new UncheckedIOException ( "I/O error while running SparqlBasedTester: " + ex.getMessage (), ex );
		}
	}
	
	/**
	 * Invokes {@link #askFromFile(String, String)} for all {@code *.sparql} files that are found in dirPath.
	 * 
	 * @return a count of all the test files that were found.
	 */
	public long askFromDirectory ( String dirPath, boolean isRecursive )
	{
		long testsCount = FileUtils.listFiles ( 
			new File ( dirPath ), 
			new String[] { "sparql" },
			isRecursive
		)
		.stream ()
		.sorted ()
		.peek ( f -> {
			log.info ( "Running '{}'", f.getName () );
			this.askFromFile ( "Error with SPARQL test '" + f.getName () + "'", f.toString () );
		})
		.count ();
		
		return testsCount;
	}
	
	/**
	 * Wrapper with isRecursive = true.
	 */
	public long askFromDirectory ( String dirPath  ) {
		return askFromDirectory ( dirPath, true );
	}

}
