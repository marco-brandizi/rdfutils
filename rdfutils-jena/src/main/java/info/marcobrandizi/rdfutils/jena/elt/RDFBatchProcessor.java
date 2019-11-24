//package info.marcobrandizi.rdfutils.jena.elt;
//
//import org.apache.jena.rdf.model.Model;
//import org.apache.jena.rdf.model.ModelFactory;
//
//import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
//import uk.ac.ebi.utils.threading.batchproc.SizedBatchProcessor;
//
///**
// * A {@link SizedBatchProcessor} dedicated to the processing of RDF data coming from a source S through batches 
// * consisting of {@link Model Jena RDF graph models}.
// *
// * @author brandizi
// * <dl><dt>Date:</dt><dd>1 Dec 2017</dd></dl>
// *
// */
//public abstract class RDFBatchProcessor
//	extends SizedBatchProcessor<S, Model>
//{		
//	/**
//	 * @return {@link Model#size()} Note that {@link #getBatchMaxSize()} is set to a default of 10000. 
//	 */
//	@Override
//	protected long getCurrentBatchSize ( Model batch ) {
//		return batch.size ();
//	}
//
//	/** 
//	 * Initialises the {@link #getBatchFactory() destination supplier} with code that spawns a new model generated 
//	 * via {@link ModelFactory} and populated with {@link NamespaceUtils#getNamespaces()}.
//	 * 
//	 * Sets {@link #getBatchMaxSize()} to 10k triples
//	 *  
//	 */
//	public RDFBatchProcessor ()
//	{
//		super ();
//		this.setBatchFactory ( () -> 
//		{
//			Model m = ModelFactory.createDefaultModel ();
//			m.setNsPrefixes ( NamespaceUtils.getNamespaces () );
//			return m;
//		});
//		
//		this.setBatchMaxSize ( 10000 );
//	}
//}
