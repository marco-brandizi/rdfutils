package info.marcobrandizi.rdfutils.jena;

import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.marcobrandizi.rdfutils.GraphUtils;
import uk.ac.ebi.utils.exceptions.TooManyValuesException;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Jan 2017</dd></dl>
 *
 */
public class JenaGraphUtils implements GraphUtils<Model, RDFNode, Resource, Property, Literal>
{
	public static final JenaGraphUtils JENAUTILS = new JenaGraphUtils ();
	
	private static Logger log = LoggerFactory.getLogger ( JenaGraphUtils.class );

	@Override
	public Optional<RDFNode> getObject ( Model m, String suri, String puri, boolean errorIfMultiple )
	{
		return getObject ( m, m.getResource ( suri ), m.getProperty ( puri ), errorIfMultiple );
	}

	@Override
	public Optional<RDFNode> getObject ( Model m, Resource s, Property p, boolean errorIfMultiple )
	{
		NodeIterator itr = m.listObjectsOfProperty ( s, p );
		if ( !itr.hasNext () ) return Optional.empty ();
		RDFNode result = itr.next ();
		if ( itr.hasNext () ) 
		{
			String msg = String.format ( "more than one value for <%s>, <%s>", s.toString (), p.toString () );
			if ( errorIfMultiple ) throw new TooManyValuesException ( msg );
			log.warn ( msg );
		}
		return Optional.of ( result );
	}

	@Override
	public GraphUtils<Model, RDFNode, Resource, Property, Literal> assertLiteral ( 
		Model m, String suri, String puri, String lexValue 
	)
	{
		m.add ( m.createResource ( suri ), m.createProperty ( puri ), m.createLiteral ( lexValue ) );
		return this;
	}

	@Override
	public GraphUtils<Model, RDFNode, Resource, Property, Literal> assertLiteral ( 
		Model m, String suri, String puri, Literal literal 
	)
	{
		m.add ( m.createResource ( suri ), m.createProperty ( puri ), literal );
		return this;
	}

	@Override
	public GraphUtils<Model, RDFNode, Resource, Property, Literal> assertResource ( 
		Model m, String suri, String puri, String ouri 
	)
	{
		m.add ( m.createResource ( suri ), m.createProperty ( puri ), m.createResource ( ouri ) );
		return this;
	}

	@Override
	public <T> Optional<T> literal2Value ( RDFNode literal, Function<String, T> converter )
	{
		if ( literal == null || !literal.canAs ( Literal.class ) ) return Optional.empty ();
		return Optional.ofNullable ( literal.asLiteral ().getLexicalForm () ).map ( converter );
	}

	public <T> Stream<T> toStream ( ExtendedIterator<T> extendedIterator, boolean isParallel, int splitIteratorType  )
	{
		return StreamSupport.stream ( 
			Spliterators.spliteratorUnknownSize ( extendedIterator, splitIteratorType ),
			isParallel
		);		
	}

	/**
	 * Defaults to {@link Spliterator#IMMUTABLE}.
	 */
	public <T> Stream<T> toStream ( ExtendedIterator<T> extendedIterator, boolean isParallel )
	{
		return toStream ( extendedIterator, isParallel, Spliterator.IMMUTABLE );
	}
	
	/**
	 * Defaults to false
	 */
	public <T> Stream<T> toStream ( ExtendedIterator<T> extendedIterator )
	{
		return toStream ( extendedIterator, false );
	}

}
