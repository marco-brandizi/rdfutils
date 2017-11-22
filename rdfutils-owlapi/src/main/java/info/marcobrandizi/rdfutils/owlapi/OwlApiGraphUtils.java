package info.marcobrandizi.rdfutils.owlapi;

import java.util.Optional;
import java.util.function.Function;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLProperty;

import info.marcobrandizi.rdfutils.GraphUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Jan 2017</dd></dl>
 *
 */
public class OwlApiGraphUtils extends GraphUtils<OWLOntology, OWLObject, OWLObject, OWLProperty, OWLLiteral>
{
	public static final OwlApiGraphUtils OWLAPIUTILS = new OwlApiGraphUtils ();
	
	@Override
	public Optional<OWLObject> getObject ( OWLOntology m, String suri, String puri, boolean errorIfMultiple )
	{
		throw new UnsupportedOperationException ( "Not implemented yet" );
	}

	@Override
	public Optional<OWLObject> getObject ( OWLOntology m, OWLObject s, OWLProperty p, boolean errorIfMultiple )
	{
		throw new UnsupportedOperationException ( "Not implemented yet" );
	}

	@Override
	public GraphUtils<OWLOntology, OWLObject, OWLObject, OWLProperty, OWLLiteral> assertLiteral ( 
		OWLOntology m, String suri, String puri, String lexValue 
	)
	{
		this.checkNonNullTriple ( "assertLiteral", suri, puri, lexValue, "literal" );

		OWLOntologyManager owlMgr = m.getOWLOntologyManager ();
		OWLDataFactory owlFactory = owlMgr.getOWLDataFactory ();

		OWLLiteral literal = owlFactory.getOWLLiteral ( lexValue );

		return this.assertLiteral ( m, suri, puri, literal );
	}

	@Override
	public GraphUtils<OWLOntology, OWLObject, OWLObject, OWLProperty, OWLLiteral> assertLiteral ( 
		OWLOntology m, String suri, String puri, OWLLiteral literal 
	)
	{
		this.checkNonNullTriple ( "assertLiteral", suri, puri, literal );
		
		OWLOntologyManager owlMgr = m.getOWLOntologyManager ();
		OWLDataFactory owlFactory = owlMgr.getOWLDataFactory ();
		
		throw new UnsupportedOperationException ( "Not implemented yet" );
	}

	@Override
	public GraphUtils<OWLOntology, OWLObject, OWLObject, OWLProperty, OWLLiteral> assertResource ( 
		OWLOntology m, String suri, String puri, String ouri 
	)
	{
		this.checkNonNullTriple ( "assertResource", suri, puri, ouri, "" );
		
		OWLOntologyManager owlMgr = m.getOWLOntologyManager ();
		OWLDataFactory owlFactory = owlMgr.getOWLDataFactory ();

		owlMgr.addAxiom ( m, owlFactory.getOWLObjectPropertyAssertionAxiom ( 
			owlFactory.getOWLObjectProperty ( IRI.create( puri )), 
			owlFactory.getOWLNamedIndividual ( IRI.create ( suri )),  
			owlFactory.getOWLNamedIndividual ( IRI.create ( ouri ))  
		));		
		
		return this;
	}

	@Override
	public <T> Optional<T> literal2Value ( OWLObject literal, Function<String, T> converter )
	{
		if ( literal == null || ! ( literal instanceof OWLLiteral ) ) return Optional.empty ();
		return Optional.ofNullable ( ( (OWLLiteral) literal).getLiteral () ).map ( converter );
	}

	
	public void checkNonNullTriple ( String methodName, String subjectUri, String propertyUri, OWLObject obj )
	{
		String dataTypeStr = null; 
		if ( obj != null )
			dataTypeStr = obj instanceof OWLLiteral ? ( (OWLLiteral) obj ).getDatatype ().getIRI ().getIRIString () : obj.getClass ().getSimpleName ();
			
		this.checkNonNullTriple ( methodName, subjectUri, propertyUri, obj == null ? null : obj.toString (), dataTypeStr );
	}

}
