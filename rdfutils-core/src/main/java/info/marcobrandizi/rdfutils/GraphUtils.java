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
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Jan 2017</dd></dl>
 *
 * @param <M> the type of graph model that the implementor uses (eg, Jena Model).
 * @param <N> the type of generic RDF node that the implementor uses (eg, Jena RDFNode)
 * @param <R> the type of resource node (eg, Jena's Resource)
 * @param <P> the type of property node
 * @param <L> the type of literal node
 * 
 * TODO: possibly merge this with the Jena implementation. This multi-framework support has more problems 
 * than advantages and I can no longer afford to maintain it.
 *  
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
	 * @see #getObject(Object, Object, Object, boolean) 
	 */
	public Optional<N> getObject ( M graphModel, String suri, String puri, boolean errorIfMultiple ) {
		return getObject ( graphModel, uri2Resource ( graphModel, suri ), uri2Property ( graphModel, puri ), errorIfMultiple );
	}
	
	
	/**
	 * Gives the value of a subject/property in the graphModel. This might be explicit or inferred, depending
	 * on the model. 
	 * 
	 * @throws TooManyValuesException, if there are more than value for this subject/property and errorIfMultiple
	 * is true. Without this flag on, if multiple values for this subject/property are available, returns any of them, 
	 */
	public abstract Optional<N> getObject ( M graphModel, R s, P p, boolean errorIfMultiple );


	public Optional<R> getSubject ( M graphModel, String puri, String ouri, boolean errorIfMultiple ) {
		return getSubject ( graphModel, uri2Property ( graphModel, puri ), uri2Resource ( graphModel, ouri ), errorIfMultiple );
	}

	public Optional<R> getSubject ( M graphModel, String puri, L literal, boolean errorIfMultiple ) {
		return getSubject ( graphModel, uri2Property ( graphModel, puri ), literal, errorIfMultiple );
	}
	
	public abstract Optional<R> getSubject ( M graphModel, P p, N o, boolean errorIfMultiple );
	
	
	
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

	/**
	 * @see #assertLiteral(Object, Object, Object, Object)
	 */
	public GraphUtils<M,N,R,P,L> assertLiteral ( M graphModel, String suri, String puri, L literal )
	{
		return assertLiteral ( 
			graphModel, 
			uri2Resource ( graphModel, suri ),
			uri2Property ( graphModel, puri ), 
			literal 
		);
	}

	/**
	 * Add this triple to the graphModel (usually nothing happens if the triple is already there).
	 * @see value2Literl() methods to get literals from plain values. 
	 */
	public abstract GraphUtils<M,N,R,P,L> assertLiteral ( M graphModel, R subj, P prop, L literal );
	
	/**
	 * @see #assertResource(Object, Object, Object, Object)
	 */
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
	
	/**
	 * Similarly to {@link #assertLiteral(Object, Object, Object, Object)}, asserts a triple linking 
	 * two URIs.
	 */
	public abstract GraphUtils<M,N,R,P,L> assertResource ( M graphModel, R subj, P prop, R obj );

	/**
	 * Gets a resource node R from uri and using the graphModel. This is a way to abstract from the particular
	 * RDF framework that is implementing GraphUtils, a specific framework is supposed to have methods to 
	 * 
	 * get something here.
	 */
	public abstract R uri2Resource ( M graphModel, String ruri );

	/**
	 * @Similarly to {@link #uri2Resource(Object, String)}, gets a property node P from uri 
	 */
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
	 * Returns the string that the framework extracts from the type N (e.g., the lexical form) as-is, 
	 * without conversions
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
	
	public Optional<Object> literal2ObjectXsdMapperBased ( L literal )
	{
		String xsdIri = this.literalDataType ( literal ).orElse ( null );
		String lexicalForm = this.literal2Value ( literal ).orElse ( null );
		return Optional.ofNullable ( XsdMapper.javaValueWithDefault ( xsdIri, lexicalForm ) );
	}
	
	
	/**
	 * Converts the string to a generic literal having a language lang (if not null).
	 * Should return an empty if lexValue is null.
	 */
	public abstract Optional<L> value2Literal ( M graphModel, String lexValue, String lang );
	
	/**
	 * Converts a string into a literal of type typeUri (which is usually an XSD data type).
	 * Should return an empty if lexValue is null. 
	 */
	public abstract Optional<L> value2TypedLiteral ( M graphModel, String lexValue, String typeUri );

	/**
	 * {@link #value2Literal(Object, String, String)} with lang == null (ie, no language in the final literal). 
	 */
	public Optional<L> value2Literal ( M graphModel, String lexValue ) {
		return this.value2Literal ( graphModel, lexValue, (String) null );
	}
	
	/**
	 * Converts a value to a literal having the given language. Uses tolexValueCvt to get the string lexical
	 * value from the value.
	 * 
	 * Returns an empty if the value is null, so the conversion function can assume to 
	 * receive a non-null.
	 *  
	 */
	public <T> Optional<L> value2Literal ( M graphModel, T value, Function<T, String> toLexValueCvt, String lang )
	{
		if ( value == null ) return Optional.empty ();
		return value2Literal ( graphModel, toLexValueCvt.apply ( value ), lang );
	}

	/**
	 * Wraps {@link #value2Literal(Object, Object, Function, String)} with lang = null (ie, no-language literal).
	 */
	public <T> Optional<L> value2Literal ( M graphModel, T value, Function<T, String> toLexValueCvt ) {
		return value2Literal ( graphModel, value, toLexValueCvt, null );
	}
	
	/**
	 * Converts a value to a typed literal of type typeUri, getting the value's lexicalValue from the toLexValueCvt function.
	 * 
	 * Returns an empty if the value is null, so the conversion function can assume to 
	 * receive a non-null.
	 * 
	 */
	public <T> Optional<L> value2TypedLiteral ( M graphModel, T value, Function<T, String> toLexValueCvt, String typeUri )
	{
		if ( value == null ) return Optional.empty ();		
		return value2TypedLiteral ( graphModel, toLexValueCvt.apply ( value ), typeUri );
	}

	/**
	 * Invokes {@link #value2TypedLiteral(Object, Object, Function, String)} using {@link XsdMapper} to 
	 * get the literal type from most common Java types.
	 *  
	 */
	public <T> Optional<L> value2TypedLiteral ( M graphModel, T value, Function<T, String> toLexValueCvt ) 
	{
		if ( value == null ) return Optional.empty ();		
		return this.value2TypedLiteral ( graphModel, value, toLexValueCvt, XsdMapper.dataTypeIri ( value ) );
	}

	/**
	 * Wraps {@link #value2TypedLiteral(Object, Object)} using {@link XsdMapper#lexicalForm(Object)} 
	 * to get the lexical value of value. 
	 */
	public <T> Optional<L> value2TypedLiteral ( M graphModel, T value ) {
		return this.value2TypedLiteral ( graphModel, value, XsdMapper::lexicalForm  );
	}
	
	/**
	 * This should use the implementing framework to return the dataType URI of a literal, if any. 
	 */
	public abstract Optional<String> literalDataType ( L literal );

	/**
	 * Simple wrappers that uses {@link Object#toString()} for all of S/P/O. 
	 * You probably want to re-implement this in your own framework-specific extension.
	 */
	public void checkNonNullTriple ( String methodName, R subj, P prop, N obj )
	{
		this.checkNonNullTriple (
			methodName, 
			subj == null ? null : subj.toString (), 
		  prop == null ? null : prop.toString (), 
		  obj == null ? null : obj.toString () 
		);
	}
	
	/**
	 * Wrapper of {@link #checkNonNullTriple(String, String, String, String, String)} that uses 
	 * {@link #literal2Value(Object)} and {@link #literal2Date(Object)}. 
	 *   
	 */
	public void checkNonNullTriple ( String methodName, String suri, String puri, L literal ) {
		this.checkNonNullTriple ( methodName, suri, puri, literal2Value ( literal ).orElse ( null ), literalDataType ( literal ).orElse ( null ) );
	}

	
	/**
	 * Used above to check that we have non-null parameters.
	 * 
	 * <b>WARNING/b>: we changed this after 4.0.3, before objectValueOrUri was first {@link StringUtils#trimToNull(String) trimmed} and
	 * then checked against null/empty values. Now we don't do the trimming operations, since it's impossible to reliably know
	 * if an invoker actually wants to export values like "" (or "\n") or not. So, this is up to the invoker now, we only check
	 * that the triple object isn't null.
	 * 
	 * @parm methodName is only used for logging.
	 * 
	 * @param dataTypeUri is not checked, only used to report error messages and it's the datatype expected for 
	 * the value.
	 */	
	public void checkNonNullTriple ( String methodName, String subjectUri, String propertyUri, String objectValueOrUri, String dataTypeUri )
	{
		if ( StringUtils.trimToNull ( subjectUri ) == null 
				|| StringUtils.trimToNull ( propertyUri ) == null || objectValueOrUri == null 
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
	
	/**
	 * wrapper with null dataType URI
	 */
	public void checkNonNullTriple ( String methodName, String subjectUri, String propertyUri, String objectValueOrUri ) {
		this.checkNonNullTriple ( methodName, subjectUri, propertyUri, objectValueOrUri, null );
	}

}
