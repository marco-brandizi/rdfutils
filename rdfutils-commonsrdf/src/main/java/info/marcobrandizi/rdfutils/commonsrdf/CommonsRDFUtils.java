package info.marcobrandizi.rdfutils.commonsrdf;

import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Function;

import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.rdf.api.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.marcobrandizi.rdfutils.GraphUtils;
import info.marcobrandizi.rdfutils.XsdMapper;
import info.marcobrandizi.rdfutils.exceptions.RdfException;
import uk.ac.ebi.utils.exceptions.TooManyValuesException;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Jan 2017</dd></dl>
 *
 */
public class CommonsRDFUtils extends GraphUtils<Graph, RDFTerm, BlankNodeOrIRI, IRI, Literal>
{
	public static final CommonsRDFUtils COMMUTILS = new CommonsRDFUtils ();

	private static Logger log = LoggerFactory.getLogger ( CommonsRDFUtils.class );
			
	private static RDF defaultRdf;
	
	private RDF rdf;
	
	private synchronized static RDF getDefaultRdf () 
	{
		if ( defaultRdf != null ) return defaultRdf;
		
		ServiceLoader<RDF> loader = ServiceLoader.load ( RDF.class );
		Iterator<RDF> itr = loader.iterator();
		
		String noInstAvalMsg = "No implementation found for Commons RDF, please, review your dependencies/classpath";
		if ( !itr.hasNext () ) throw new RdfException ( noInstAvalMsg );

		defaultRdf = itr.next();
		
		String simpleFQN = "org.apache.commons.rdf.simple.SimpleRDF";
		
		// This is wrongly included in other implementations (https://issues.apache.org/jira/browse/COMMONSRDF-73)
		// and we're not interested in it, so let's skip it and try to move to the next.
		if ( simpleFQN.equals ( defaultRdf.getClass ().getName () ) )
		{
			if ( !itr.hasNext () ) throw new RdfException ( 
				noInstAvalMsg + "(SimpleRDF is ignored by default, you must set up it with setRDF())" 
			);
			defaultRdf = itr.next ();
		}
		
		if ( itr.hasNext () && !simpleFQN.equals ( itr.next ().getClass ().getName () ) ) log.warn ( 
			"More than one RDF instance available for Commons RDF, taking the first one ({})",  defaultRdf 
		);	
		else
			log.info ( "RDF Utils configured with {}", defaultRdf.getClass ().getName () );
		
		return defaultRdf;
	}
	
	/**
	 * Sets up the {@link RDF} implementation you want to work with. This in practice establishes the particular
	 * framework (eg., Jena, RDF4j) that you want to access via Commons.
	 * 
	 * Note that you must include that particular implementation in your classpath in order for this to work
	 * (see above).
	 * 
	 * <b>WARNING</b>: if you never call the {@link #setRDF(RDF) corresponding setter}, this is supposed to 
	 * get the right {@link RDF} instance via SPI, from {@code META-INF/services/org.apache.commons.rdf.api.RDF}.
	 * 
	 * However, with Jena there is a <a href = 'https://issues.apache.org/jira/browse/COMMONSRDF-73>dependency problem</a> 
	 * that prevents this mechanism to work correctly, so, for the time being, you should explicitly setup 
	 * the RDF implementation you want to work with (see the tests in this module).
	 *     
	 */
	public RDF getRDF () {
		return rdf == null ? rdf = getDefaultRdf () : rdf;
	}
	
	public synchronized void setRDF ( RDF rdf ) {
		this.rdf = rdf;
	}
	
	
	@Override
	public Optional<RDFTerm> getObject ( Graph m, BlankNodeOrIRI s, IRI p, boolean errorIfMultiple )
	{
		synchronized ( m )
		{
			@SuppressWarnings ( "unchecked" )
			Iterator<Triple> itr = (Iterator<Triple>) m
			  .stream ( s, p, null )
			  .unordered ()
			  .limit ( 2 )
			  .iterator ();
			
			if ( !itr.hasNext () ) return Optional.empty ();
			RDFTerm result = itr.next ().getObject ();
			if ( itr.hasNext () ) 
			{
				String msg = String.format ( "more than one value for <%s>, <%s>", s.toString (), p.toString () );
				if ( errorIfMultiple ) throw new TooManyValuesException ( msg );
				log.warn ( msg );
			}
			return Optional.of ( result );
		}
	}


	@Override
	public <T> Optional<T> literal2Value ( RDFTerm literal, Function<String, T> converter )
	{
		if ( literal == null || !( literal instanceof Literal ) ) return Optional.empty ();
		return Optional.ofNullable (  ( (Literal) literal ).getLexicalForm () ).map ( converter );
	}

	
	@Override
	public GraphUtils<Graph, RDFTerm, BlankNodeOrIRI, IRI, Literal> assertLiteral ( 
		Graph m, BlankNodeOrIRI subj, IRI prop, Literal literal 
	)
	{
		this.checkNonNullTriple ( "assertLiteral", subj, prop, literal );
		synchronized ( m ) {
			m.add ( subj, prop, literal ); 
			return this;
		}
	}

	@Override
	public GraphUtils<Graph, RDFTerm, BlankNodeOrIRI, IRI, Literal> assertResource (
		Graph m, BlankNodeOrIRI subj, IRI prop, BlankNodeOrIRI obj
	)
	{
		this.checkNonNullTriple ( "assertResource", subj, prop, obj );
		synchronized ( m ) {
			m.add ( subj, prop, obj);
			return this;
		}
	}

	@Override
	public BlankNodeOrIRI uri2Resource ( Graph m, String ruri ) 
	{
		RDF rdf = getRDF ();
		return ( ruri == null ) ? rdf.createBlankNode () : rdf.createIRI ( ruri );
	}

	@Override
	public IRI uri2Property ( Graph m, String puri ) {
		return getRDF ().createIRI ( puri );
	}

	@Override
	public <T> Optional<Literal> value2Literal ( Graph m, T value )
	{
		RDF rdf = getRDF ();
		
		return value == null 
			? Optional.empty () 
			: Optional.ofNullable ( 
				  rdf.createLiteral ( value.toString (), rdf.createIRI ( XsdMapper.dataTypeIri ( value ) ) ) 
			);
	}

	@Override
	public Optional<Literal> value2Literal ( Graph m, String value, String lang )
	{
		return value == null ? Optional.empty () : Optional.of ( getRDF ().createLiteral ( value, lang ) );
	}
	
	@Override
	public Optional<String> literalDataType ( Literal literal )
	{
		return literal == null 
			? Optional.empty () 
			: Optional.ofNullable ( literal.getDatatype ().getIRIString () );
	}	
	
	
	public void checkNonNullTriple ( String methodName, String suri, String puri, RDFTerm obj )
	{
		if ( obj == null )
			checkNonNullTriple ( methodName, suri, puri, null, null );
		
		String dataTypeStr = Optional
			.ofNullable ( ( (Literal) obj ).getDatatype () )
			.map ( IRI::getIRIString )
			.orElse ( null );
			
		this.checkNonNullTriple ( methodName, suri, puri, obj.toString (), dataTypeStr );
	}
	
}
