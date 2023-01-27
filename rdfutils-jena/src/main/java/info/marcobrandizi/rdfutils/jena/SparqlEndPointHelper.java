package info.marcobrandizi.rdfutils.jena;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.machinezoo.noexception.Exceptions;
import com.machinezoo.noexception.throwing.ThrowingRunnable;
import com.machinezoo.noexception.throwing.ThrowingSupplier;

import uk.ac.ebi.utils.exceptions.ExceptionUtils;
import uk.ac.ebi.utils.runcontrol.ProgressLogger;

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
	/**
	 * A SPARQL query cache. This stores queries that have already been parsed from their string representation. 
	 * It is used in the data manager methods, to save some time about query parsing.  
	 * 
	 */
	private static LoadingCache<String, Query> queryCache;
	
	protected Logger log = LoggerFactory.getLogger ( this.getClass () );
	private static Logger slog = LoggerFactory.getLogger ( SparqlEndPointHelper.class );
	
	static 
	{
		// Initialise the query cache with the QueryFactory-based generator.
		//
		queryCache = CacheBuilder
		.newBuilder ()
		.maximumSize ( 1000 )
		.build ( new CacheLoader<String, Query> () 
		{
			@Override
			public Query load ( String sparql )
			{
				try {
					return QueryFactory.create ( sparql, Syntax.syntaxARQ );
				}
				catch ( QueryException ex ) {
					slog.error ( "SPARQL Error in {}, query is:\n{}", SparqlEndPointHelper.class.getSimpleName (), sparql );
					throw new IllegalArgumentException ( "SPARQL error in " + SparqlEndPointHelper.class + ": " + ex.getMessage (), ex );
				}
			}
		});
	}

	
	
	/**
	 * Just runs a SPARQL SELECT query against the current endpoint.
	 */
	public ResultSet select ( Query sparqlSelect, QuerySolutionMap params )
	{
		var qx = getQueryExecutor ( sparqlSelect, params );
		return qx.execSelect ();
	}
	
	public ResultSet select ( Query sparqlSelect )
	{
		return select ( sparqlSelect, null );
	}
	
	public ResultSet select ( String sparqlSelect, QuerySolutionMap params )
	{
		return select ( getQuery ( sparqlSelect ), params );
	}
	
	public ResultSet select ( String sparqlSelect )
	{
		return select ( sparqlSelect, null );
	}
	
	
	/**
	 * Just runs a SPARQL ASK query against the current endpoint.
	 */
	public boolean ask ( Query sparqlAsk, QuerySolutionMap params )
	{
		try ( var qx = getQueryExecutor ( sparqlAsk, params ) ) {
			return qx.execAsk ();
		}
	}
	
	public boolean ask ( Query sparqlAsk )
	{
		return ask ( sparqlAsk, null );
	}
	
	public boolean ask ( String sparqlAsk, QuerySolutionMap params )
	{
		return ask ( getQuery ( sparqlAsk ), params );
	}
	
	public boolean ask ( String sparqlAsk )
	{
		return ask ( sparqlAsk, null );
	}	
	
	
	public Model construct ( Query sparqlConstruct, Model initialModel, QuerySolutionMap params )
	{
		return processConstruct ( sparqlConstruct, v -> {}, initialModel, params );
	}
	
	public Model construct ( Query sparqlConstruct, Model initialModel )
	{
		return construct ( sparqlConstruct, initialModel, null );
	}
	
	public Model construct ( Query sparqlConstruct ) {
		return construct ( sparqlConstruct, null );
	}	
	
	
	public Model construct ( String sparqlConstruct, Model initialModel, QuerySolutionMap params )
	{
		return processConstruct ( sparqlConstruct, v -> {}, initialModel, params );
	}
	
	public Model construct ( String sparqlConstruct, Model initialModel )
	{
		return construct ( sparqlConstruct, initialModel, null );
	}
	
	public Model construct ( String sparqlConstruct ) {
		return construct ( sparqlConstruct, null );
	}		
	
	
	
	/**
	 * <p>Process a SPARQL query, by running it against some end point that this helper manages, then passes each 
	 * {@link QuerySolution} to the action parameter.</p>
	 * 
	 * <p>Works out operations like getting the proper handler from TDB query or caching the SPARQL queries.</p>
	 * 
	 * @param logPrefix used in the progress log message
	 */
	public long processSelect ( String logPrefix, Query sparqlSelect, Consumer<QuerySolution> action, QuerySolutionMap params ) 
	{	
		try ( QueryExecution qx = getQueryExecutor ( sparqlSelect, params ) )
		{
			if ( logPrefix == null ) logPrefix = "rdfutils";
			var progress = new ProgressLogger ( logPrefix + ": {} SPARQL tuples read from RDF", 100000 );
			progress.setIsThreadSafe ( true );
			
			qx.execSelect ().forEachRemaining ( row ->
			{
				// Doing a clone after having observed transaction timeouts with TDB
				var clonedRow = new QuerySolutionMap ();
				clonedRow.addAll ( row );
				action.accept ( clonedRow );
				
				progress.updateWithIncrement ();
			});
			
			return progress.getProgress ();
		}
	}
	
	public long processSelect ( String logPrefix, Query sparqlSelect, Consumer<QuerySolution> action )
	{
		return this.processSelect ( logPrefix, sparqlSelect, action, null );
	}
	
	public long processSelect ( String logPrefix, String sparqlSelect, Consumer<QuerySolution> action, QuerySolutionMap params ) 
	{
		return processSelect ( logPrefix, getQuery ( sparqlSelect ), action, params );
	}

	public long processSelect ( String logPrefix, String sparqlSelect, Consumer<QuerySolution> action ) 
	{
		return processSelect ( logPrefix, sparqlSelect, action, null );
	}
	
	
	public long processSelect ( Query sparqlSelect, Consumer<QuerySolution> action, QuerySolutionMap params ) 
	{	
		return processSelect ( null, sparqlSelect, action, params );
	}
	
	public long processSelect ( Query sparqlSelect, Consumer<QuerySolution> action )
	{
		return this.processSelect ( sparqlSelect, action, null );
	}
	
	
	public long processSelect ( String sparqlSelect, Consumer<QuerySolution> action, QuerySolutionMap params ) 
	{
		return processSelect ( null, sparqlSelect, action, params );
	}

	public long processSelect ( String sparqlSelect, Consumer<QuerySolution> action ) 
	{
		return processSelect ( sparqlSelect, action, null );
	}
	
	
	
	public Model processConstruct ( 
		Query sparqlConstruct, Consumer<Model> action, Model initialModel, QuerySolutionMap params 
	)
	{
		try ( QueryExecution qx = getQueryExecutor ( sparqlConstruct ) )
		{
			if ( params != null ) qx.setInitialBinding ( params );
		
			Model result = initialModel == null ? qx.execConstruct () : qx.execConstruct ( initialModel );
			action.accept ( result );
			return result;
		}
	}
	
	public Model processConstruct ( Query sparqlConstruct, Consumer<Model> action, Model initialModel )
	{
		return processConstruct ( sparqlConstruct, action, initialModel, null );
	}
	
	public Model processConstruct ( Query sparqlConstruct, Consumer<Model> action ) {
		return processConstruct ( sparqlConstruct, action, null );
	}
	
	
		
	public Model processConstruct ( 
		String sparqlConstruct, Consumer<Model> action, Model initialModel, QuerySolutionMap params 
	)
	{
		return processConstruct ( getQuery ( sparqlConstruct ), action, initialModel, params );
	}	
	
	public Model processConstruct ( String sparqlConstruct, Consumer<Model> action, Model initialModel )
	{
		return processConstruct ( sparqlConstruct, action, initialModel, null );
	}
	
	public Model processConstruct ( String sparqlConstruct, Consumer<Model> action ) {
		return processConstruct ( sparqlConstruct, action, null );
	}
	
	
	/**
	 * Gets the Jena {@link Query} object corresponding to this query string. By default this is a wrapper of
	 * {@link #getCachedQuery(String)}.
	 * 
	 */
	public Query getQuery ( String sparql ) {
		return getCachedQuery ( sparql );
	}
	
	/**
	 * Gets a {@link Query} object corresponding to the string. Queries are cached to save some syntax parsing time.
	 * All the {@link #getQuery(String) calls to this class} are based on this by default.
	 *  
	 */
	public static Query getCachedQuery ( String sparql )
	{
		return queryCache.getUnchecked ( sparql );
	}
	
	/**
	 * Gets a Jena {@link QueryExecution} handler, based on the type of data source that the specific implementation 
	 * of this helper manages (e.g., {@link Model}, TDB, remote HTTP endpoint).
	 * 
	 */
	public abstract QueryExecution getQueryExecutor ( Query query );
	
	
	public QueryExecution getQueryExecutor ( Query query, QuerySolutionMap params )
	{
		var qx = getQueryExecutor ( query );
		if ( params != null ) qx.setInitialBinding ( params );
		return qx;
	}

	public QueryExecution getQueryExecutor ( String query, QuerySolutionMap params )
	{
		return getQueryExecutor ( getQuery ( query ), params );
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
			throw ExceptionUtils.buildEx ( 
					UncheckedIOException.class, ex, "Error while working with SPARQL endpoint: $cause" 
			);
		}
		catch ( Exception ex ) {
			throw ExceptionUtils.buildEx ( 
				RuntimeException.class, ex, "Error while working with SPARQL endpoint: $cause" 
			);
		}
		catch ( Throwable ex ) {
			throw ExceptionUtils.buildEx ( 
				Error.class, ex, "Error while working with SPARQL endpoint: $cause" 
			);
		}	
	}	
}