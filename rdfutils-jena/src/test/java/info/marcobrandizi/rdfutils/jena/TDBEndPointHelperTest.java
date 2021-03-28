package info.marcobrandizi.rdfutils.jena;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.jena.query.QuerySolution;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.utils.io.IOUtils;
import uk.ac.ebi.utils.threading.batchproc.processors.SetBasedBatchProcessor;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>26 Mar 2021</dd></dl>
 *
 */
public class TDBEndPointHelperTest
{
	private static class MyBatchProcessor extends SetBasedBatchProcessor<QuerySolution, Consumer<Set<QuerySolution>>>
		implements AutoCloseable
	{
		private Logger log = LoggerFactory.getLogger ( this.getClass () );

		private AtomicInteger count = new AtomicInteger ( 0 );
		
		TDBEndPointHelper helper = new TDBEndPointHelper ( "/tmp/rdf2pg-tdb" );
		
		public MyBatchProcessor ()
		{
			super ( 2500 );
			this.setBatchJob ( sols -> log.info ( "Got {} results", count.addAndGet ( sols.size () ) ) );
		}

		public void process () throws IOException
		{
			var sparql = IOUtils.readFile ( "/Users/brandizi/Documents/Work/RRes/ondex_git/ondex-knet-builder/ondex-knet-builder/modules/neo4j-export/src/main/assembly/resources/ondex_config/concept_rel_types.sparql" );

			Consumer<Consumer<QuerySolution>> allSolProc = 
			  solProc -> helper.processSelect ( "", sparql, solProc );
			  
			super.process ( allSolProc );
		}

		@Override
		public void close () {
			helper.close ();
		}
		
		
		
	}
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );

	@Test @Ignore ( "Not a real test, used to verify the performance in rdf2pg" )
	public void testProcessSelect() throws IOException
	{
		var sparql = IOUtils.readFile ( "/Users/brandizi/Documents/Work/RRes/ondex_git/ondex-knet-builder/ondex-knet-builder/modules/neo4j-export/src/main/assembly/resources/ondex_config/concept_rel_types.sparql" );
		try ( var helper = new TDBEndPointHelper ( "/tmp/rdf2pg-tdb" ) )
		{
			helper.processSelect ( "", sparql, sol -> log.info ( "SOL: {}", sol ) );
		}
	}
	
	@Test //@Ignore ( "Not a real test, used to verify the performance in rdf2pg" )
	public void testProcessSelectWithBatchProc () throws IOException
	{
		try ( var proc = new MyBatchProcessor () )
		{
			for ( int i = 0; i < 1; i++ )
				proc.process ();
		}
	}
}
