package info.marcobrandizi.rdfutils.jena;

import java.util.function.Consumer;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDB2Factory;

/**
 * A {@link SparqlEndPointHelper} for Jena TDB triple stores.. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Jul 2018</dd></dl>
 *
 */
public class TDBEndPointHelper extends SparqlEndPointHelper implements AutoCloseable
{
	private String tdbPath = null;
	protected Dataset dataSet = null;

	public TDBEndPointHelper () {
		super ();
	}

	/**
	 * Calls {@link #open(String)}.
	 */
	public TDBEndPointHelper ( String tdbPath ) 
	{
		this ();
		open ( tdbPath );
	}	

	
	/**
	 * <p>Opens a TDB on the file system and initialises the {@link #getDataSet() data set} that is managed by this
	 * class.</p>
	 * 
	 * <p>Many TDB-related operations require that this method is first invoked. {@link #close()} is its counterpart.</p> 
	 */
	public void open ( String tdbPath )
	{
		wrapTask ( () -> 
		{
			log.debug ( "Setting TDB to '{}'", tdbPath );
			
			this.tdbPath = tdbPath;
			this.dataSet = TDB2Factory.connectDataset ( tdbPath );
		});
	}

	/**
	 * Ensures that {@link #open(String)} was called and raises an exception if not. Used in several methods below that 
	 * require this condition.
	 */
	protected void ensureOpen ()
	{
		if ( this.dataSet == null ) throw new IllegalStateException ( 
			"The data manager must be open() before working with it" 
		);
	}

	/**
	 * Wraps {@link SparqlEndPointHelper#processSelect(String, String, Consumer)} into a TDB transaction.
	 */
	@Override
	public long processSelect ( String logPrefix, String sparql, Consumer<QuerySolution> action )
	{	
		Dataset ds = this.getDataSet ();

		long result[] = { 0 }; 
 		Txn.executeRead ( ds, () -> result [ 0 ] = super.processSelect ( logPrefix, sparql, action ) );
		return result [ 0 ];
	}

	
	
	/**
	 * Similarly to {@link #processSelect(String, String, Consumer)}, wraps the operation into a transaction. 
	 */
	@Override
	public Model processConstruct ( String logPrefix, String sparqlConstruct, Consumer<Model> action, Model initialModel )
	{
		Dataset ds = this.getDataSet ();

		Model result[] = { null }; 
 		Txn.executeRead (
 			ds,
 			() -> result [ 0 ] = super.processConstruct ( logPrefix, sparqlConstruct, action, initialModel )
 		);
		return result [ 0 ];
	}

	/**
	 * You need to {@link #open(String) open the TDB} before this.
	 */
	@Override
	public QueryExecution getQueryExecutor ( Query query )
	{
		return QueryExecutionFactory.create ( query, this.getDataSet() );
	}
	
	/**
	 * This returns the Jena {@link Dataset} corresponding to the TDB triple store at {@link #getTdbPath()} that was 
	 * opened by {@link #open(String)}. 
	 * 
	 */
	public Dataset getDataSet ()
	{
		ensureOpen ();
		return dataSet;
	}
	
	/**
	 * This is null until {@link #open(String)}.
	 * @return
	 */
	public String getTdbPath ()
	{
		return tdbPath;
	}

	@Override
	public void close ()
	{
		if ( this.dataSet == null ) return;
		this.dataSet.close ();
		this.dataSet = null;
	}

}