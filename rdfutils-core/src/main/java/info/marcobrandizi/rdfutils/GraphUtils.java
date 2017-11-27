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
 * @param <R> the type of resource node (eg, Jena's Resource)
 * @param <P> the type of property node
 * @param <L> the type of literal node
 */
public abstract class GraphUtils<M, N, R extends N, P extends N, L extends N>
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
	public Optional<N> getObject ( M graphModel, String suri, String puri, boolean errorIfMultiple ) {
		return getObject ( graphModel, uri2Resource ( graphModel, suri ), uri2Property ( graphModel, puri ), errorIfMultiple );
	}
	
	
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

	public GraphUtils<M,N,R,P,L> assertLiteral ( M graphModel, String suri, String puri, String lexValue )
	{
		return this.assertLiteral ( graphModel, suri, puri, value2Literal ( graphModel, lexValue ).orElse ( null ) );
	}
	
	public GraphUtils<M,N,R,P,L> assertLiteral ( M graphModel, String suri, String puri, L literal )
	{
		return assertLiteral ( 
			graphModel, 
			uri2Resource ( graphModel, suri ),
			uri2Property ( graphModel, puri ), 
			literal 
		);
	}

	public abstract GraphUtils<M,N,R,P,L> assertLiteral ( M graphModel, R subj, P prop, L literal );
	
	public GraphUtils<M,N,R,P,L> assertResource ( M graphModel, String suri, String puri, String ouri )
	{
		this.checkNonNullTriple ( "assertResource", suri, puri, ouri );		
		return assertResource ( 
			graphModel, 
			uri2Resource ( graphModel, suri ), 
			uri2Property ( graphModel, puri ), 
			uri2Resource ( graphModel, ouri ) 
		);
	}
	
	public abstract GraphUtils<M,N,R,P,L> assertResource ( M graphModel, R subj, P prop, R obj );


	public abstract R uri2Resource ( M graphModel, String ruri );
	
	public abstract P uri2Property ( M graphModel, String puri );
	
	
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
	
	public abstract Optional<L> value2Literal ( M graphModel, String lexValue, String lang );
	public abstract Optional<L> value2TypedLiteral ( M graphModel, String lexValue, String typeUri );

	public Optional<L> value2Literal ( M graphModel, String lexValue ) {
		return this.value2Literal ( graphModel, lexValue, (String) null );
	}
	
	public <T> Optional<L> value2Literal ( M graphModel, T value, Function<T, String> toLexValueCvt, String lang ) {
		return value2Literal ( graphModel, toLexValueCvt.apply ( value ), lang );
	}

	public <T> Optional<L> value2Literal ( M graphModel, T value, Function<T, String> toLexValueCvt ) {
		return value2Literal ( graphModel, value, toLexValueCvt, null );
	}
	
	public <T> Optional<L> value2TypedLiteral ( M graphModel, T value, Function<T, String> toLexValueCvt, String typeUri ) {
		return value2TypedLiteral ( graphModel, toLexValueCvt.apply ( value ), typeUri );
	}

	public <T> Optional<L> value2TypedLiteral ( M graphModel, T value, Function<T, String> toLexValueCvt ) {
		return this.value2TypedLiteral ( graphModel, toLexValueCvt.apply ( value ), XsdMapper.dataTypeIri ( value ) );
	}

	public <T> Optional<L> value2TypedLiteral ( M graphModel, T value ) {
		return this.value2TypedLiteral ( graphModel, value, Object::toString );
	}
	
	public abstract Optional<String> literalDataType ( L literal );

	
	public void checkNonNullTriple ( String methodName, R subj, P prop, N obj )
	{
		this.checkNonNullTriple (
			methodName, 
			subj == null ? null : subj.toString (), 
		  prop == null ? null : prop.toString (), 
		  	obj == null ? null : prop.toString () 
		);
	}
	
	public void checkNonNullTriple ( String methodName, String suri, String puri, L literal ) {
		this.checkNonNullTriple ( methodName, suri, puri, literal2Value ( literal ).orElse ( null ), literalDataType ( literal ).orElse ( null ) );
	}

	
	/**
	 * Used above to check that we have non-null parameters.
	 * @parm methodName is only used for logging.
	 * @param dataTypeUri is not checked, only used to report error messages and it's the datatype expected for 
	 * the value.
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
	
	public void checkNonNullTriple ( String methodName, String subjectUri, String propertyUri, String objectValueOrUri ) {
		this.checkNonNullTriple ( methodName, subjectUri, propertyUri, objectValueOrUri, null );
	}

}
