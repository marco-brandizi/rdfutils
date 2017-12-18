package info.marcobrandizi.rdfutils.jena.elt;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Optional;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;

import uk.ac.ebi.utils.threading.BatchProcessor;

/**
 * A multi-thread RDF importer based on the structure of {@link RDFProcessor} and hence {@link BatchProcessor}.
 * 
 * This class is a skeleton that parses an input in the {@link #process(InputStream, String, Lang) process() methods}
 * below, sequentially splits it into multiple {@link Model} instances and passes them to parallel
 * {@link #getConsumer() consumers}. Hence, you need to define something to do with the input RDF via such 
 * {@link #setConsumer(java.util.function.Consumer) consumer}.
 * 
 * See unit tests for an example of use.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>1 Dec 2017</dd></dl>
 *
 */
public class RDFImporter extends RDFProcessor<InputStream>
{
	/**
	 * This bridges ourselves to the Jena {@link RDFDataMgr data manager} (ie, the RDF reader).
	 * As data are sent to this interface, we populate the {@link RDFImporter#getDestinationSupplier() current destination model}
	 * and possibly {@link RDFImporter#handleNewTask(Model, boolean) submit a new processing thread}.
	 */
	private class StreamReader implements StreamRDF
	{
		private Model model;
		
		public StreamReader () {
		}

		@Override
		public void start () {
			this.model = getDestinationSupplier ().get ();
		}

		@Override
		public void triple ( Triple triple ) 
		{
			this.model.getGraph ().add ( triple );
			this.model = handleNewTask ( this.model );
		}

		@Override
		public void quad ( Quad quad ) {}

		@Override
		public void base ( String base ) {}

		/**
		 * Populates the current model with this prefix def.
		 */
		@Override
		public void prefix ( String prefix, String iri )
		{
			this.model.setNsPrefix ( prefix, iri );
		}

		@Override
		public void finish () {
			handleNewTask ( this.model, true );
		}
	}
	
		
	@Override
	public void process ( InputStream rdfInput, Object... opts )
	{
		String base = null;
		Lang lang = null;

		if ( opts != null )
		{
			if ( opts.length > 0 && opts [ 0 ] != null )
			{
				if ( ! ( opts [ 0 ] instanceof String ) ) 
					throw new IllegalArgumentException ( String.format (
						"base param wrong type %s RDFImporter accepts String base and Lang lang as parameters, " +
						"check the documentation",
						opts [ 0 ].getClass ().getName ()
					));
				base = (String) opts [ 0 ];
			}
			if ( opts.length > 1 && opts [ 1 ] != null )
			{
				if ( !( opts [ 1 ] instanceof Lang ) ) 
					throw new IllegalArgumentException ( String.format (
						"base param wrong type %s RDFImporter accepts String base and Lang lang as parameters, " +
						"check the documentation",
						opts [ 0 ].getClass ().getName ()
					));
				lang = (Lang) opts [ 1 ];
			}
		}
		
		process ( rdfInput, base, lang );
	}
	
	/**
	 * Uses the {@link RDFDataMgr} to parse the input and send chunks of it to {@link #getConsumer() processors}.
	 * base and hintLang are the usual Jena parameters accepted by the 
	 * {@link RDFDataMgr#parse(StreamRDF, InputStream, String, Lang) RDF parsers}. 
	 * 
	 * @see {@link JenaIoUtils#getLangOrFormat(String)}, to convert a string to a {@link Lang} object.
	 * 
	 */
	public void process ( InputStream rdfInput, String base, Lang hintLang )
	{
		StreamReader streamReader = new StreamReader ();
		RDFDataMgr.parse ( streamReader, rdfInput, base, hintLang );
		this.waitExecutor ( "Waiting for all RDF import jobs to finish" );
		log.info ( "RDF import terminated" );
	}
	
	public void process ( File rdfFile, String base, Lang hintLang )
	{
		try ( InputStream in = new BufferedInputStream ( new FileInputStream ( rdfFile ) ); ) 
		{
			this.process ( new FileInputStream ( rdfFile ), base, hintLang );
		}
		catch ( IOException ex ) {
			throw new UncheckedIOException ( String.format ( 
				"Error while reading file '%s': %s", 
				Optional.ofNullable ( rdfFile ).map ( File::getAbsolutePath ).orElse ( "<null>" ),
				ex.getMessage () ), 
				ex 
			);
		}
	}
	
	public void process ( File rdfFile ) {
		this.process ( rdfFile, null, null );
	}

	
	public void process ( String rdfFilePath, String base, Lang hintLang ) {
		this.process ( new File ( rdfFilePath ), base, hintLang );
	}
	
	public void process ( String rdfFilePath ) {
		this.process ( rdfFilePath, null, null );
	}

}
