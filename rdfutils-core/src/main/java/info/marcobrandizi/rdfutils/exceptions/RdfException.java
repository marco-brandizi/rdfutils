package info.marcobrandizi.rdfutils.exceptions;

/**
 * Used to notify about a problem with an RDF framework.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>21 Nov 2017</dd></dl>
 *
 */
@SuppressWarnings ( "serial" )
public class RdfException extends RuntimeException
{

	public RdfException ( String message, Throwable cause ) {
		super ( message, cause );
	}

	public RdfException ( String message ) {
		super ( message );
	}

}
