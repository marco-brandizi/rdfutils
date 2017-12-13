package info.marcobrandizi.rdfutils.jena;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>8 Dec 2017</dd></dl>
 *
 */
public class SparqlUtils
{
	public static QueryExecution query ( String sparql, Model model )
	{
		Query q = QueryFactory.create ( sparql, Syntax.syntaxARQ );
		return QueryExecutionFactory.create ( q, model );
	}
	
	public static ResultSet select ( String sparqlSelect, Model model )
	{
		return query ( sparqlSelect, model ).execSelect ();
	}

	public static boolean ask ( String sparqlAsk, Model model )
	{
		return query ( sparqlAsk, model ).execAsk ();
	}
	
	public static Model construct ( String sparqlConstruct, Model model, Model target )
	{
		QueryExecution qe = query ( sparqlConstruct, model );
		return target == null ? qe.execConstruct () : qe.execConstruct ( target );
	}

	public static Model construct ( String sparqlConstruct, Model model )
	{
		return construct ( sparqlConstruct, model, null );
	}

}
