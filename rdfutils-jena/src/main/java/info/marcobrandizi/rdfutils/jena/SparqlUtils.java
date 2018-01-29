package info.marcobrandizi.rdfutils.jena;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;

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
	 * Creates a query out of the string, using a {@link Syntax#syntaxARQ default syntax}. 
	 */
	public static QueryExecution query ( String sparql, Model model )
	{
		Query q = QueryFactory.create ( sparql, Syntax.syntaxARQ );
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
