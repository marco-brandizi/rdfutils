package info.marcobrandizi.rdfutils;

import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

import org.joda.time.format.ISODateTimeFormat;

import com.google.common.primitives.Doubles;

import uk.ac.ebi.utils.exceptions.TooManyValuesException;

/**
 * Misc utils to access graph functions of an RDF graph implementer (e.g., Jena, Sesame).
 * 
 * TODO: more comments.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Jan 2017</dd></dl>
 *
 * @param <M> the type of graph model that the implementor uses (eg, Jena Model).
 * @param <N> the type of generic RDF node that the implementor uses (eg, Jena RDFNode)
 * @param <R> the type of resource node (Jena's Resource)
 * @param <P> the type of property node
 * @param <L> the type of literal node
 */
public interface GraphUtils<M,N,R,P,L>
{

	/**
	 * Defaults to errorIfMultiple = false
	 */
	public default Optional<N> getObject ( M graphModel, String suri, String puri ) {
		return getObject ( graphModel, suri, puri, false );
	}

	/**
	 * @throws TooManyValuesException, if there are more than value for this subject/property.  
	 */
	public Optional<N> getObject ( M graphModel, String suri, String puri, boolean errorIfMultiple );
	
	
	/**
	 * @throws TooManyValuesException, if there are more than value for this subject/property.  
	 */
	public Optional<N> getObject ( M graphModel, R s, P p, boolean errorIfMultiple );


	/**
	 * Defaults to errorIfMultiple = false
	 */
	public default Optional<N> getObject ( M graphModel, R s, P p ) {
		return getObject ( graphModel, s, p, false );
	}
	
	

	public GraphUtils<M,N,R,P,L> assertLiteral ( M graphModel, String suri, String puri, String lexValue );
	public GraphUtils<M,N,R,P,L> assertLiteral ( M graphModel, String suri, String puri, L literal );

	public GraphUtils<M,N,R,P,L> assertResource ( M graphModel, String suri, String puri, String ouri );

	
	/**
	 * Facility to convert a literal value into a custom value, using the converter parameter.
	 * Does this safely, it returns an empty optional if the node parameter is null, or is not a literal node.
	 * 
	 * @param converter can assume the received string is non-null, but must not assume it is syntactic valid, nor that 
	 * extra boundary spaces are trimmed. 
	 * 
	 */
	public <T> Optional<T> literal2Value ( N literal, Function<String, T> converter );

	/**
	 * Returns the literal that is achieved from type N (e.g., the lexical form) as-is, without conversions
	 */
	public default Optional<String> literal2Value ( N literal ) {
		return literal2Value ( literal, Function.identity () );
	}
	
	public default Optional<Double> literal2Double ( N literal ) {
		return literal2Value ( literal, Doubles::tryParse );
	}

	
	public default Optional<Date> literal2Date ( N literal ) 
	{
		return literal2Value ( literal, ds -> {
			try {
				return ISODateTimeFormat.dateTime ().parseDateTime ( ds ).toDate ();
			}
			catch ( IllegalArgumentException ex ) {
				return null;
			}
		});
	}
}
