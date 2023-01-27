package info.marcobrandizi.rdfutils.jena.elt;

import java.util.function.Consumer;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.system.Txn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Can be used with {@link RDFStreamLoader} to load data into a TDB triple store.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Dec 2017</dd></dl>
 *
 */
public class TDBLoadingHandler implements Consumer<Model>
{
	private Dataset dataSet = null;
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
		
	public TDBLoadingHandler ( Dataset dataSet )  
	{
		this.dataSet = dataSet;
	}

	public TDBLoadingHandler () {
	}
	
	@Override
	public void accept ( Model model )
	{
		Txn.executeWrite ( this.dataSet, () -> {
			log.debug ( "Writing {} triple(s) to TDB", model.size () );
			dataSet.getDefaultModel ().add ( model );
			log.debug ( "{} triple(s) written to TDB", model.size () );
		});
	}

	public Dataset getDataSet ()
	{
		return dataSet;
	}

	public void setDataSet ( Dataset dataSet )
	{
		this.dataSet = dataSet;
	}
}
