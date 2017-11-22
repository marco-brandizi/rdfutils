package info.marcobrandizi.rdfutils.owlapi;

import java.util.Map.Entry;

import org.semanticweb.owlapi.formats.PrefixDocumentFormat;

import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>21 Nov 2017</dd></dl>
 *
 */
public class OwlApiNamespaceUtils
{
	/**
	 * Copies all the namespaces into the structure that OWLAPI uses to build its output 
	 */
	public static void copy2OwlApi ( PrefixDocumentFormat owlApiPrefixes )
	{
		for ( Entry<String, String> nse: NamespaceUtils.getNamespaces ().entrySet () )
			owlApiPrefixes.setPrefix ( nse.getKey (), nse.getValue () );
	}
}
