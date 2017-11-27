package info.marcobrandizi.rdfutils.commonsrdf;

import static info.marcobrandizi.rdfutils.commonsrdf.CommonsRDFUtils.COMMUTILS;
import static info.marcobrandizi.rdfutils.namespaces.NamespaceUtils.iri;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.junit.Test;

import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>23 Nov 2017</dd></dl>
 *
 */
public class UtilsTest
{
	static {
		NamespaceUtils.registerNs ( "foo", "http://www.findme.net/test/" );
	}
	
	
	@Test
	public void testAssertLiteral ()
	{
		Graph g = COMMUTILS.getRDF ().createGraph ();
				
		COMMUTILS.assertLiteral ( g, iri ( "foo:alice" ), iri ( "foo:hasAge" ), COMMUTILS.value2TypedLiteral ( g, 30 ).get () );
		
		BlankNodeOrIRI alice = COMMUTILS.uri2Resource ( g, iri ( "foo:alice" ) );
		IRI hasAge = COMMUTILS.uri2Property ( g, iri ( "foo:hasAge" ) );
		
		Optional<RDFTerm> age = COMMUTILS.getObject ( g, alice, hasAge, true );
		
		assertTrue ( "age not found!", age.isPresent () );
		assertEquals ( "age mismatch!", 30d, (double) COMMUTILS.literal2Double ( age.get () ).get (), 0d );
	}


	@Test
	public void testAssertResource ()
	{
		Graph g = COMMUTILS.getRDF ().createGraph ();
				
		COMMUTILS.assertResource ( g, iri ( "foo:alice" ), iri ( "foo:knows" ), iri ( "foo:bob" ) );
		
		IRI alice = (IRI) COMMUTILS.uri2Resource ( g, iri ( "foo:alice" ) );
		IRI knows = COMMUTILS.uri2Property ( g, iri ( "foo:knows" ) );
		IRI bob = (IRI) COMMUTILS.uri2Resource ( g, iri ( "foo:bob" ) );
				
		Optional<RDFTerm> obj = COMMUTILS.getObject ( g, alice, knows, true );
		
		assertTrue ( "bob not found!", obj.isPresent () );
		assertEquals ( "age mismatch!", bob.getIRIString (), ( (IRI) obj.get () ).getIRIString () );
	}

}
