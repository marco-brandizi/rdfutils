package info.marcobrandizi.rdfutils.jena;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.machinezoo.noexception.throwing.ThrowingRunnable;
import com.machinezoo.noexception.throwing.ThrowingSupplier;

/**
 * A SPARQL end point helper for Jena, aiming at making SPARQL access independent of the endpoint type 
 * (eg, transparent wrt TDB or HTTP endpoint).
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Jul 2018</dd></dl>
 *
 */
public abstract class SparqlEndPointHelper
{
	protected Logger log = LoggerFactory.getLogger ( this.getClass () );

	/**
	 * Gets a Jena {@link QueryExecution} handler, based on the type of data source that the specific implementation 
	 * of this helper manages (e.g., {@link Model}, TDB, remote HTTP endpoint).
	 * 
	 */
	public abstract QueryExecution getQueryExecutor ( Query query );
	
	/**
	 * <p>Process a SPARQL query, by running it against some end point that this helper manages, then passes each 
	 * {@link QuerySolution} to the action parameter.</p>
	 * 
	 * <p>Works out operations like getting the proper handler from TDB query or caching the SPARQL queries.</p>
	 *  
	 * @param logPrefix operation name, used for logging.
	 */
	public long processSelect ( String logPrefix, String sparqlSelect, Consumer<QuerySolution> action ) 
	{	
		Query query = getQuery ( sparqlSelect );
		QueryExecution qx = getQueryExecutor ( query );

		long[] ctr = { 0L };
		
		qx.execSelect ().forEachRemaining ( row -> {
			// TODO: remove? Now it seems to be working again
			/* Doing a clone after having observed transaction timeouts with TDB
			var clonedRow = new QuerySolutionMap ();
			clonedRow.addAll ( row );
			
			action.accept ( clonedRow );*/
			action.accept ( row );
			if ( ++ctr [ 0 ] % 100000 == 0 ) log.info ( "{}: {} SPARQL tuples read from RDF", logPrefix, ctr [ 0 ] ); 
		});
		
		return ctr [ 0 ];		
	}
	
	public void processConstruct ( String logPrefix, String sparqlConstruct, Consumer<Model> action ) {
		processConstruct ( logPrefix, sparqlConstruct, action, null );
	}
	
	public Model processConstruct ( String logPrefix, String sparqlConstruct, Consumer<Model> action, Model initialModel )
	{
		Query query = getQuery ( sparqlConstruct );
		QueryExecution qx = getQueryExecutor ( query );
		
		Model result = initialModel == null ? qx.execConstruct () : qx.execConstruct ( initialModel );
		action.accept ( result );
		return result;
	}
	
	
	/**
	 * Gets the Jena {@link Query} object corresponding to this query string. By default this is a wrapper of
	 * {@link SparqlUtils#getCachedQuery(String)}.
	 * 
	 */
	public Query getQuery ( String sparql ) {
		return SparqlUtils.getCachedQuery ( sparql );
	}
	
	
	/** 
	 * A wrapper of {@link #wrapFun(ThrowingSupplier)} for procedures that don't need to return anything.  
	 */
	protected static void wrapTask ( ThrowingRunnable task )
	{
		wrapFun ( () -> { task.run (); return null; } ); 
	}
	
	/**
	 * A facility that wraps some code throwing a checked exception with a try/catch and an possibly re-throws an 
	 * unchecked exception.  
	 */
	protected static <V> V wrapFun ( ThrowingSupplier<V> fun )
	{
		try {
			return fun.get ();
		}
		catch ( IOException ex ) {
			throw new UncheckedIOException ( "I/O error while working with SPARQL endpoint: " + ex.getMessage (), ex );
		}
		catch ( Exception ex ) {
			throw new RuntimeException ( "Error while working with SPARQL endpoint: " + ex.getMessage (), ex );
		}
	}	
}