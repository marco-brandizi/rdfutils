package info.marcobrandizi.rdfutils;

import org.junit.Assert;
import org.junit.Test;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>3 Oct 2018</dd></dl>
 *
 */
public class XsdMapperTest
{
	@Test
	public void testFallbackCase ()
	{
		Object x = XsdMapper.javaValueWithDefault ( null, "0.95" );
		Assert.assertEquals ( "Wrong returned class!", String.class, x.getClass () );
	}
}
