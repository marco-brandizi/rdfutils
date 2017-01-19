package info.marcobrandizi.rdfutils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>16 Jan 2017</dd></dl>
 *
 */
public class NamespacesTest
{
	@Test
	public void testBasics ()
	{
		assertEquals ( 
			"No rdf: namespace found!", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", 
			NamespaceUtils.ns ( "rdf" ) 
		);
		assertEquals ( "uri ( rdfs:label ) not working!", 
			"http://www.w3.org/2000/01/rdf-schema#label", 
			NamespaceUtils.iri ( "rdfs:label" ) 
		);
	}
	
	@Test
	public void testRegisterNs ()
	{
		String fooNs = "http://somewhere.in.net/foons#";
		NamespaceUtils.registerNs ( "foo", fooNs );
		assertEquals ( "registerNs not working!", fooNs, NamespaceUtils.ns ( "foo" ) );
	}
	
}
