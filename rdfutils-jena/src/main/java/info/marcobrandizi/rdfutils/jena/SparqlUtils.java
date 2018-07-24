package info.marcobrandizi.rdfutils.jena;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Some utilities to ease the access to SPARQL in Jena.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>8 Dec 2017</dd></dl>
 *
 */
public class SparqlUtils
{
	/**
	 * A SPARQL query cache. This stores queries that have already been parsed from their string representation. 
	 * It is used in the data manager methods, to save some time about query parsing.  
	 * 
	 * TODO: move the query caching to {@link SparqlUtils}.
	 */
	private static LoadingCache<String, Query> queryCache; 
	
	static 
	{
		Cache<String, Query> cache = CacheBuilder
				.newBuilder ()
				.maximumSize ( 1000 )
				.build ( new CacheLoader<String, Query> () 
				{
					@Override
					public Query load ( String sparql ) throws Exception {
						return QueryFactory.create ( sparql, Syntax.syntaxARQ );
					}
				});
			queryCache = (LoadingCache<String, Query>) cache;		
	}
	
	/**
	 * Gets a {@link Query} object corresponding to the string. Queries are cached to save some syntax parsing time.
	 * All the calls below that accept SPARQL strings use this method. 
	 */
	public static Query getCachedQuery ( String sparql )
	{
		return queryCache.getUnchecked ( sparql );
	}
		
	
	/**
	 * Creates a query out of the string, using a {@link Syntax#syntaxARQ default syntax}. 
	 */
	public static QueryExecution query ( String sparql, Model model )
	{
		Query q = getCachedQuery ( sparql );
		return QueryExecutionFactory.create ( q, model );
	}
	
	/**
	 * Just runs a select query against the query and the model and returns the corresponding
	 * {@link ResultSet} to go through the results. 
	 */
	public static ResultSet select ( String sparqlSelect, Model model )
	{
		return query ( sparqlSelect, model ).execSelect ();
	}

	/**
	 * Runs an ASK query and tells the boolean result.
	 */
	public static boolean ask ( String sparqlAsk, Model model )
	{
		return query ( sparqlAsk, model ).execAsk ();
	}
	
	/**
	 * Adds the results of a CONSTRUCT query to the target model. Creates a new result model
	 * if target is null, i.e., returns a data set containing the query results only.
	 *  
	 * @return the target model, or a the newly created one.
	 */
	public static Model construct ( String sparqlConstruct, Model model, Model target )
	{
		QueryExecution qe = query ( sparqlConstruct, model );
		return target == null ? qe.execConstruct () : qe.execConstruct ( target );
	}

	/**
	 * Like {@link #construct(String, Model, Model)}, but with null as target model, i.e., returns the data set
	 * corresponding to the CONSTRUCT results.  
	 */
	public static Model construct ( String sparqlConstruct, Model model )
	{
		return construct ( sparqlConstruct, model, null );
	}
}
