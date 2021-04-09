package info.marcobrandizi.rdfutils.jena;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * A {@link SparqlEndPointHelper} that is based on the plain {@link Model Jena Model}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>9 Apr 2021</dd></dl>
 *
 */
public class ModelEndPointHelper extends SparqlEndPointHelper
{
	private Model model;
	
	/**
	 * 
	 */
	public ModelEndPointHelper ( Model model )
	{
		this.model = model == null ? ModelFactory.createDefaultModel () : model;
	}
	
	public ModelEndPointHelper ()
	{
		this ( null );
	}


	@Override
	public QueryExecution getQueryExecutor ( Query query )
	{
		return QueryExecutionFactory.create ( query, model );
	}

	public Model getModel ()
	{
		return model;
	}
}
