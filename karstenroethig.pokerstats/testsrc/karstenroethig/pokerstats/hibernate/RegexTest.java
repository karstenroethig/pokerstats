package karstenroethig.pokerstats.hibernate;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import karstenroethig.pokerstats.util.MoneyUtils;

public class RegexTest extends TestCase {

	public void testRegex() throws Exception {
		
		String regex = "[0-9]*[,][0-9]*";
		
		Map<String, Boolean> values = new HashMap<String, Boolean>();
		
		values.put( "123,34", true );
		values.put( "123,3", true );
		values.put( "123,", true );
		values.put( "123,34a", false );
//		values.put( ",", false );
		values.put( ",9", true );
		values.put( ",34", true );
		values.put( "9,3456", true );
		
		for( String key : values.keySet() ) {
			boolean result = values.get( key );
			
			if( result ) {
				assertTrue( key.matches( regex ) );
				
				System.out.println( key + " -> " + MoneyUtils.parsePercentage( key ) );
			} else {
				assertFalse( key.matches( regex ) );
			}
		}
	}
}
