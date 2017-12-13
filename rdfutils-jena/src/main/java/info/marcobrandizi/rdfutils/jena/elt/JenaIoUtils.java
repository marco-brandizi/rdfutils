package info.marcobrandizi.rdfutils.jena.elt;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>1 Dec 2017</dd></dl>
 *
 */
public class JenaIoUtils
{
	public static Pair<RDFFormat, Lang> getLangOrFormat ( String langOrFormat )
	{
		try
		{
			Lang jlang = null;
			RDFFormat jfmt = null;
			try {
				jlang = (Lang) Lang.class.getField ( langOrFormat ).get ( null );
			}
			catch ( NoSuchFieldException ex ) 
			{
				try {
					jfmt = (RDFFormat) RDFFormat.class.getField ( langOrFormat ).get ( null );
				} 
				catch ( NoSuchFieldException ex1 ) {
					throw new IllegalArgumentException ( "Cannot find any RDF language or format for '" + langOrFormat + "'" );
				}
			}
			return Pair.of ( jfmt, jlang );
		}
		catch ( IllegalArgumentException | IllegalAccessException | SecurityException ex )
		{
			throw new IllegalArgumentException ( String.format ( 
				"Internal error while getting RDF language '%s': %s", langOrFormat, ex.getMessage () ), 
				ex 
			);
		}
	}
}
