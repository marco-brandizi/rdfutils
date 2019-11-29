package info.marcobrandizi.rdfutils.jena.elt;

import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import uk.ac.ebi.utils.threading.batchproc.AbstractSizedBatchCollector;
import uk.ac.ebi.utils.threading.batchproc.BatchProcessor;

/**
 * A {@link AbstractSizedBatchCollector sized-based batch collector} for RDF {@link BatchProcessor batch processors},
 * such as {@link RDFStreamLoader}. A model batch collector aims at collecting batches of RDF triples, into 
 * {@link Model RDF models}. 
 * 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Nov 2019</dd></dl>
 *
 */
public class ModelBatchCollector extends AbstractSizedBatchCollector<Model>
{
	public ModelBatchCollector ( long maxBatchSize ) {
		super ( maxBatchSize );
	}

	/**
	 * Defaults to 10000.
	 */
	public ModelBatchCollector () {
		this ( 10000 );
	}

	@Override
	public Supplier<Model> batchFactory ()
	{
		return () -> 
		{
			Model m = ModelFactory.createDefaultModel ();
			m.setNsPrefixes ( NamespaceUtils.getNamespaces () );
			return m;
		};
	}

	@Override
	public Function<Model, Long> batchSizer ()
	{
		return model -> model.size ();
	}
}
