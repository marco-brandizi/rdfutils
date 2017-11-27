package info.marcobrandizi.rdfutils.jena;

import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.Lock;
import org.apache.jena.util.iterator.ExtendedIterator;

import info.marcobrandizi.rdfutils.GraphUtils;
import uk.ac.ebi.utils.exceptions.TooManyValuesException;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Jan 2017</dd></dl>
 *
 */
public class JenaGraphUtils extends GraphUtils<Model, RDFNode, Resource, Property, Literal>
{
	public static final JenaGraphUtils JENAUTILS = new JenaGraphUtils ();
	
	@Override
	public Optional<RDFNode> getObject ( Model m, Resource s, Property p, boolean errorIfMultiple )
	{
		return doRead ( m, () -> 
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
		});
	}


	@Override
	public <T> Optional<T> literal2Value ( RDFNode literal, Function<String, T> converter )
	{
		if ( literal == null || !literal.canAs ( Literal.class ) ) return Optional.empty ();
		return Optional.ofNullable ( literal.asLiteral ().getLexicalForm () ).map ( converter );
	}

	
	@Override
	public GraphUtils<Model, RDFNode, Resource, Property, Literal> assertLiteral ( 
		Model m, Resource subj, Property prop, Literal literal 
	)
	{
		this.checkNonNullTriple ( "assertLiteral", subj, prop, literal );
		doWriteVoid ( m, () -> m.add ( subj, prop, literal ) );
		return this;
	}

	@Override
	public GraphUtils<Model, RDFNode, Resource, Property, Literal> assertResource (
		Model m, Resource subj, Property prop, Resource obj
	)
	{
		this.checkNonNullTriple ( "assertResource", subj, prop, obj );
		doWriteVoid ( m, () -> m.add ( subj, prop, obj) );
		return this;
	}

	@Override
	public Resource uri2Resource ( Model m, String ruri ) {
		return doWrite ( m, () -> m.createResource ( ruri ) );
	}

	@Override
	public Property uri2Property ( Model m, String puri ) {
		return doWrite ( m, () -> m.createProperty ( puri ) );
	}
	
	@Override
	public Optional<Literal> value2TypedLiteral ( Model m, String lexValue, String typeUri )
	{
		if ( lexValue == null ) return Optional.empty ();
		return Optional.of ( m.createTypedLiteral ( lexValue, typeUri ) );
	}

	
	@Override
	public <T> Optional<Literal> value2TypedLiteral ( Model m, T value )
	{
		return value == null 
			? Optional.empty () : 
				Optional.ofNullable ( doWrite ( m, () -> m.createTypedLiteral ( value ) ) );
	}
	
	@Override
	public Optional<Literal> value2Literal ( Model m, String lexValue, String lang )
	{
		return lexValue == null 
			? Optional.empty () 
			: Optional.of ( 
					doWrite (
						m, 
						() -> lang == null ? m.createLiteral ( lexValue ) : m.createLiteral ( lexValue, lang ) ) 
			);
	}


	@Override
	public Optional<String> literalDataType ( Literal literal )
	{
		return literal == null ? Optional.empty () : Optional.ofNullable ( literal.getDatatypeURI () );
	}	
	
	
	public void checkNonNullTriple ( String methodName, String subjectUri, String propertyUri, RDFNode obj )
	{
		if ( obj == null )
			checkNonNullTriple ( methodName, subjectUri, propertyUri, null, null );
		
		String dataTypeStr = obj instanceof Literal 
			? obj.asLiteral ().getDatatypeURI () 
			: null;
			
		this.checkNonNullTriple ( methodName, subjectUri, propertyUri, obj.toString (), dataTypeStr );
	}
	
	/**
	 * Facility to turn a Jena {@link ExtendedIterator} into a stream, based on 
	 * {@link Spliterators#spliteratorUnknownSize(java.util.Iterator, int)}.
	 * 
	 * @param splitIteratorType, use {@link Spliterator} constants, like {@link Spliterator#IMMUTABLE}
	 * or {@link Spliterator#CONCURRENT}.
	 * 
	 */
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
	
	
	
	private <T> T doRead ( Model m, Supplier<T> op )
	{
		return this.doSync ( m, op, Lock.READ );
	}

	private void doReadVoid ( Model m, Runnable op ) {
		this.doSyncVoid ( m, op, Lock.READ );
	}

	private <T> T doWrite ( Model m, Supplier<T> op )
	{
		return this.doSync ( m, op, Lock.WRITE );
	}

	private void doWriteVoid ( Model m, Runnable op ) {
		this.doSyncVoid ( m, op, Lock.WRITE );
	}

	private <T> T doSync ( Model m, Supplier<T> op, boolean isReadOnly )
	{
		m.enterCriticalSection ( isReadOnly );
		try {
			return op.get ();
		}
		finally {
			m.leaveCriticalSection ();
		}
	}

	private void doSyncVoid ( Model m, Runnable op, boolean isReadOnly ) {
		this.doSync ( m, () -> { op.run (); return null; }, isReadOnly );
	}

}
