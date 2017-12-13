package info.marcobrandizi.rdfutils.jena.elt;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import uk.ac.ebi.utils.threading.SizeBasedBatchProcessor;

/**
 * A {@link SizeBasedBatchProcessor} dedicated to the processing of {@link Model Jena RDF graph models}
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>1 Dec 2017</dd></dl>
 *
 */
public abstract class RDFProcessor<S> extends SizeBasedBatchProcessor<S, Model>
{
	/**
	 * @return {@link Model#size()} Note that {@link #getDestinationMaxSize()} is set to a default of 10000. 
	 */
	@Override
	protected long getDestinationSize ( Model dest ) {
		return dest.size ();
	}

	/** 
	 * Initialises the {@link #getDestinationSupplier() destination supplier} with a new model generated via
	 * {@link ModelFactory} and populated with {@link NamespaceUtils#getNamespaces()}.
	 *  
	 */
	public RDFProcessor ()
	{
		super ();
		this.setDestinationSupplier ( () -> 
		{
			Model m = ModelFactory.createDefaultModel ();
			m.setNsPrefixes ( NamespaceUtils.getNamespaces () );
			return m;
		});
		
		this.setDestinationMaxSize ( 10000 );
	}
}
