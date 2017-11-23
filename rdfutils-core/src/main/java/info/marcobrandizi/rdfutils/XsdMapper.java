package info.marcobrandizi.rdfutils;

import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;

import java.util.HashMap;
import java.util.Map;

import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Nov 2017</dd></dl>
 *
 */
public class XsdMapper
{
	@SuppressWarnings ( "rawtypes" )
	private static Map<Class, String> java2iris = new HashMap<> ();
	
	static {
		java2iris.put ( java.lang.String.class, iri ( "xsd:string" ) );
		java2iris.put ( java.math.BigInteger.class, iri ( "xsd:integer" ) );
		java2iris.put ( Integer.class, iri ( "xsd:int" ) );
		java2iris.put ( Long.class, iri ( "xsd.long" ) );
		java2iris.put ( Short.class, iri ( "xsd:short" ) );
		java2iris.put ( java.math.BigDecimal.class, iri ( "xsd:decimal" ) );
		java2iris.put ( Float.class, iri ( "xsd:float" ) );
		java2iris.put ( Double.class, iri ( "xsd:double" ) );
		java2iris.put ( Boolean.class, iri ( "xsd:boolean" ) );
		java2iris.put ( Byte.class, iri ( "xsd:byte" ) );
		java2iris.put ( javax.xml.namespace.QName.class, iri ( "xsd:QName" ) );
		java2iris.put ( javax.xml.datatype.XMLGregorianCalendar.class, iri ( "xsd:dateTime" ) );
		java2iris.put ( javax.xml.datatype.Duration.class, iri ( "xsd:duration" ) );
		java2iris.put ( javax.xml.namespace.QName.class, iri ( "xsd:NOTATION" ) );
	}
	
	@SuppressWarnings ( "rawtypes" )
	public static String dataTypeIri ( Class clazz )
	{
		return java2iris.get ( clazz );
	}

  public static String dataTypeIri ( Object o )
  {
  		return o == null ? null : dataTypeIri ( o.getClass () );
  }
}
