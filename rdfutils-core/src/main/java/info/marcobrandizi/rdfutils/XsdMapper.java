package info.marcobrandizi.rdfutils;

import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;
import static uk.ac.ebi.utils.exceptions.ExceptionUtils.buildEx;
import static uk.ac.ebi.utils.exceptions.ExceptionUtils.throwEx;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import uk.ac.ebi.utils.exceptions.UnexpectedEventException;

/**
 * Utility to map Java types to XSD types.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Nov 2017</dd></dl>
 *
 */
public class XsdMapper
{
	/**
	 * Manages the conversions of a specific XSD/Java type pair. 
	 */
	protected static class LiteralType<T>
	{
		private String xsdIri;
		private Class<T> typeClass;
		private Function<T, String> lexicalFormConverter;
		private Function<String, T> typeConverter;
		
		protected LiteralType ( String xsdUri, Class<T> typeClass, Function<T, String> lexicalFormConverter,
				Function<String, T> typeConverter )
		{
			super ();
			this.xsdIri = xsdUri;
			this.typeClass = typeClass;
			this.lexicalFormConverter = lexicalFormConverter;
			this.typeConverter = typeConverter;
		}

		public String toLexicalForm ( T value ) {
			return this.lexicalFormConverter.apply ( value );
		} 

		public T javaValue ( String lexicalForm ) {
			return this.typeConverter.apply ( lexicalForm );
		}

		public Class<T> getTypeClass ()
		{
			return typeClass;
		}

		public String getXsdIri ()
		{
			return xsdIri;
		}
	}
	
	@SuppressWarnings ( "rawtypes" )
	private static Map<String, LiteralType> xsds2types = new HashMap<> ();
	@SuppressWarnings ( "rawtypes" )
	private static Map<Class, LiteralType> classes2xsds = new HashMap<> ();
	
	static
	{
		try
		{
			final var datatypeFactory = DatatypeFactory.newInstance ();
			
			// TODO: Spring and Open/Close principle
			
			@SuppressWarnings ( "rawtypes" )
			LiteralType types[] = new LiteralType[] { 
				new LiteralType<> ( 
					iri ( "xsd:string" ), String.class , Function.identity (), Function.identity () 
				),
				new LiteralType<> ( iri ( "xsd:integer" ), 
					BigInteger.class , BigInteger::toString , s -> BigInteger.valueOf ( Long.parseLong ( s ) )
				),
				new LiteralType<> ( 
					iri ( "xsd:int" ), Integer.class , n -> Integer.toString ( n ) , Integer::parseInt
				),
				new LiteralType<> (
					iri ( "xsd:long" ), Long.class, n -> Long.toString ( n ) , Long::parseLong
				),
				new LiteralType<> ( 
					iri ( "xsd:short" ), Short.class, n -> Short.toString ( n ) , Short::parseShort
				),
				new LiteralType<> ( 
					iri ( "xsd:decimal" ), 
					BigDecimal.class, BigDecimal::toString , s -> BigDecimal.valueOf ( Double.parseDouble ( s ) )
				),
				new LiteralType<> (
					iri ( "xsd:float" ), Float.class, x -> Float.toString ( x ), Float::parseFloat
				),
				new LiteralType<> ( 
					iri ( "xsd:double" ), Double.class, x -> Double.toString ( x ), Double::parseDouble
				),
				new LiteralType<> ( 
					iri ( "xsd:boolean" ), Boolean.class, b -> Boolean.toString ( b ), Boolean::parseBoolean
				),
				new LiteralType<> (
					iri ( "xsd:byte" ), Byte.class, b -> Byte.toString ( b ), Byte::parseByte
				),
				new LiteralType<> ( 
					iri ( "xsd:dateTime" ), XMLGregorianCalendar.class,
					XMLGregorianCalendar::toXMLFormat, datatypeFactory::newXMLGregorianCalendar
				),
				new LiteralType<> (
					iri ( "xsd:duration" ), Duration.class, Duration::toString, datatypeFactory::newDuration
				),
				new LiteralType<> (
					iri ( "xsd:NOTATION" ), QName.class, QName::toString, QName::valueOf
				),
				// Leave this after xsd:NOTATION (see below)
				new LiteralType<> (
					iri ( "xsd:QName" ), QName.class, QName::toString, QName::valueOf
				)				
			};
			
			for ( LiteralType<?> lt: types )
			{
				xsds2types.put ( lt.getXsdIri (), lt );
				// Should Qname ever be used, its class points back to xsd:QName, even if it came from xsd:NOTATION 
				classes2xsds.put ( lt.getTypeClass (), lt );
			}
		}
		catch ( DatatypeConfigurationException ex )
		{
			throwEx ( 
				UnexpectedEventException.class, ex, 
				"Internal error while initialising XSD/Java mapping: %s", ex.getMessage () 
			);
		}
	}
	
	public static String dataTypeIri ( Class<?> clazz ) {
		return Optional.ofNullable ( classes2xsds.get ( clazz ) ).map ( LiteralType::getXsdIri ).orElse ( null );
	}

  public static String dataTypeIri ( Object o ) {
  		return o == null ? null : dataTypeIri ( o.getClass () );
  }
  
  @SuppressWarnings ( { "unchecked" } )
	public static <T> String lexicalForm ( Class<? extends T> clazz, T o ) {
  	return o == null 
  		? null 
  		: Optional.ofNullable ( classes2xsds.get ( clazz ) )
  		  .map ( lt -> lt.toLexicalForm ( o ) )
  		  .orElse ( o.toString () );
  }
  
	public static <T> String lexicalForm ( T o ) { 
		return o == null ? null : lexicalForm ( o.getClass (), o );
	}

	/**
	 * @throws IllegalArgumentException if we don't support {@code xsdUri} (null included).	 
	 */
	@SuppressWarnings ( "unchecked" )
	public static <T> Class<T> javaClass ( String xsdIri )
	{
		return Optional.ofNullable ( (LiteralType<T>) xsds2types.get ( xsdIri ) )
			.map ( lt -> lt.getTypeClass () )
			.orElseThrow ( () -> buildEx ( 
				IllegalArgumentException.class,
				"Cannot convert XSD type: '%s' to Java, type unsupported, consider javaXXXWithDefault() instead",
				xsdIri 
			));
	}

	/**
	 * @return null if we don't support this XSD type.
	 * @param xsdIri
	 * @return
	 */
	@SuppressWarnings ( "unchecked" )
	public static <T> Class<T> javaClassWithDefault ( String xsdIri )
	{
		return Optional.ofNullable ( (LiteralType<T>) xsds2types.get ( xsdIri ) )
			.map ( lt -> lt.getTypeClass () )
			.orElse ( null );
	}

	
	/**
	 *
	 * @throws IllegalArgumentException if we don't support {@code xsdUri} (null included). If 
	 * lexicalForm is null, it's passed to the {@link LiteralType} that corresponds to {@code xsdUri}, so
	 * the latter might not generate exceptions if it is able to deal with nulls.
	 * 
	 */
	@SuppressWarnings ( "unchecked" )
	public static <T> T javaValue ( String xsdIri, String lexicalForm )
	{
		return Optional.ofNullable ( (LiteralType<T>) xsds2types.get ( xsdIri ) )
			.map ( lt -> lt.javaValue ( lexicalForm ) )
			.orElseThrow ( () -> buildEx ( 
				IllegalArgumentException.class,
				"Cannot convert XSD type: '%s' to Java, type unsupported, consider javaValueWithDefault() instead",
				xsdIri 
			));
	}

	/**
	 * Defaults to lexicalForm (hence returning a string) when {@code xsdUri} is not supported. The method
	 * returns null if lexicalForm is null.
	 * 
	 */
	public static Object javaValueWithDefault ( String xsdIri, String lexicalForm )
	{
		if ( lexicalForm == null ) return null;
		
		LiteralType<?> lt = Optional.ofNullable ( (LiteralType<?>) xsds2types.get ( xsdIri ) )
			.orElse ( null );
		if ( lt == null ) return lexicalForm;
		
		return lt.javaValue ( lexicalForm );
	}

}
