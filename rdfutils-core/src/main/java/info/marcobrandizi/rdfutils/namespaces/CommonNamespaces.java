package info.marcobrandizi.rdfutils.namespaces;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>16 Jan 2017</dd></dl>
 *
 */
public class CommonNamespaces implements Namespaces
{

	@Override
	public Map<String, String> getNamespaces ()
	{
		return Collections.unmodifiableMap ( new HashMap <String, String>() {{
			put ( "rdf", 						"http://www.w3.org/1999/02/22-rdf-syntax-ns#" ); 
		  put ( "rdfs", 					"http://www.w3.org/2000/01/rdf-schema#" );
		  put ( "owl", 						"http://www.w3.org/2002/07/owl#" );
		  put ( "dc", 						"http://purl.org/dc/elements/1.1/" );
		  put ( "dcterms", 				"http://purl.org/dc/terms/" );
		  put ( "foaf", 					"http://xmlns.com/foaf/0.1/" );
			put ( "xsd", 						"http://www.w3.org/2001/XMLSchema#" );
		}});
	}
	
}
