package info.marcobrandizi.rdfutils.jena.elt;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDBLoader;
import org.apache.jena.tdb2.TDB2;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.tdb2.loader.Loader;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gc.iotools.stream.base.ExecutionModel;
import com.gc.iotools.stream.os.OutputStreamToInputStream;

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
		/*
		try ( 
			// This is an out wrapper that sends the received output to an input stream and then runs a thread
			// doing the TDB loading with such input stream (see http://io-tools.sourceforge.net/easystream/tutorial/tutorial.html)
			var rdfout = new OutputStreamToInputStream<Void>( true, ExecutionModel.STATIC_THREAD_POOL ) 
			{
		    @Override
		    protected Void doRead ( InputStream rdfin ) throws Exception 
		    {
		    		synchronized ( dataSet ) {
		    			
			    		DatasetGraphTDB gtdb = TDBInternal.getDatasetGraphTDB ( dataSet );
			    		TDBLoader.load ( gtdb, rdfin, Lang.NQUADS, false, false );
			    		TDBLoader.load ( gtdb, rdfin, false );
					}
		    		return null;
		    }
			};
		)	 
		{
			// The main thread pushes data into the output stream
			log.debug ( "Writing {} triple(s) to TDB", model.size () );
			model.write ( rdfout, Lang.NQUADS.getName () );
			log.debug ( "{} triple(s) written to TDB", model.size () );
		}
		catch ( IOException ex ) {
			throw new UncheckedIOException ( "Error while loading RDF data into support TDB: " + ex.getMessage (), ex );
		}
		*/
		
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
