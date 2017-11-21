package info.marcobrandizi.rdfutils.namespaces;

import java.util.Map;

/**
 * If you implement your own version of this and put it into META-INF/info.marcobrandizi.rdfutils.namespaces.Namespaces,
 * as per the <a href = "https://docs.oracle.com/javase/tutorial/ext/basics/spi.html">SPI contract</A>, 
 * then the namespaces returned by {@link #getNamespaces()} will be available in {@link NamespaceUtils}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>16 Jan 2017</dd></dl>
 *
 */
public interface Namespaces
{
	public Map<String, String> getNamespaces ();
}
