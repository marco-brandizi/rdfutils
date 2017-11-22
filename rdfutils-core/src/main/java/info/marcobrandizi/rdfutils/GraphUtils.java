package info.marcobrandizi.rdfutils;

import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Doubles;

import info.marcobrandizi.rdfutils.exceptions.RdfException;
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
public abstract class GraphUtils<M,N,R,P,L>
{
	protected Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	/**
	 * Defaults to errorIfMultiple = false
	 */
	public Optional<N> getObject ( M graphModel, String suri, String puri ) {
		return getObject ( graphModel, suri, puri, false );
	}

	/**
	 * @throws TooManyValuesException, if there are more than value for this subject/property.  
	 */
	public abstract Optional<N> getObject ( M graphModel, String suri, String puri, boolean errorIfMultiple );
	
	
	/**
	 * @throws TooManyValuesException, if there are more than value for this subject/property.  
	 */
	public abstract Optional<N> getObject ( M graphModel, R s, P p, boolean errorIfMultiple );


	/**
	 * Defaults to errorIfMultiple = false
	 */
	public Optional<N> getObject ( M graphModel, R s, P p ) {
		return getObject ( graphModel, s, p, false );
	}
	
	

	public abstract GraphUtils<M,N,R,P,L> assertLiteral ( M graphModel, String suri, String puri, String lexValue );
	public abstract GraphUtils<M,N,R,P,L> assertLiteral ( M graphModel, String suri, String puri, L literal );

	public abstract GraphUtils<M,N,R,P,L> assertResource ( M graphModel, String suri, String puri, String ouri );

	
	/**
	 * Facility to convert a literal value into a custom value, using the converter parameter.
	 * Does this safely, it returns an empty optional if the node parameter is null, or is not a literal node.
	 * 
	 * @param converter can assume the received string is non-null, but must not assume it is syntactically valid, nor 
	 * that extra boundary spaces are trimmed. 
	 * 
	 */
	public abstract <T> Optional<T> literal2Value ( N literal, Function<String, T> converter );

	/**
	 * Returns the literal that is achieved from type N (e.g., the lexical form) as-is, without conversions
	 */
	public Optional<String> literal2Value ( N literal ) {
		return literal2Value ( literal, Function.identity () );
	}
	
	public Optional<Double> literal2Double ( N literal ) {
		return literal2Value ( literal, Doubles::tryParse );
	}

	
	public Optional<Date> literal2Date ( N literal ) 
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
	
	public Optional<Boolean> literal2Boolean ( N literal ) {
		return literal2Value ( literal, Boolean::parseBoolean );
	}
	
	
	public void checkNonNullTriple ( String methodName, R subj, P prop, N obj )
	{
		this.checkNonNullTriple ( 
			methodName, 
			subj == null ? (String) null : subj.toString (), 
			prop == null ? (String) null : prop.toString (), 
		  obj
		);
	}

	public abstract void checkNonNullTriple ( String methodName, String subjectUri, String propertyUri, N obj );

	
	/**
	 * Used above to check that we have non-null parameters. dataTypeUri is not checked, only used to report error messages.
	 * methodName is only used for logging.
	 */	
	public void checkNonNullTriple ( String methodName, String subjectUri, String propertyUri, String objectValueOrUri, String dataTypeUri )
	{
		if ( StringUtils.trimToNull ( subjectUri ) == null 
				|| StringUtils.trimToNull ( propertyUri ) == null || StringUtils.trimToNull ( objectValueOrUri ) == null 
		)
		{
			if ( dataTypeUri == null )
				throw new RdfException ( String.format (
					"Cannot assert an RDF triple with null elements: %s ( '%s', '%s', '%s' )", 
					methodName, subjectUri, propertyUri, StringUtils.abbreviate ( objectValueOrUri, 255 ) ));
			else
				throw new RdfException ( String.format (
					"Cannot assert an RDF triple with null elements: %s ( '%s', '%s', '%s', '%s' )", 
					methodName, subjectUri, propertyUri, StringUtils.abbreviate ( objectValueOrUri, 255 ), dataTypeUri ));
		}
		
		log.trace ( "doing {} ( '{}', '{}', '{}' )", 
			methodName, subjectUri, propertyUri, StringUtils.abbreviate ( objectValueOrUri, 50 ) 
		);
	}

}
