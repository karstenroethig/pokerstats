package karstenroethig.pokerstats.util;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class MoneyUtilsTest extends TestCase {

	public void notestFormatPercentage() throws Exception {
		
		List<Integer> percentages = new ArrayList<Integer>();
		
		percentages.add(25);
		percentages.add(100000000);
		percentages.add(23450000);
		percentages.add(62511000);
		percentages.add(1);
		percentages.add(73726120);
		
		
		for(Integer percentage : percentages){
			System.out.println(percentage + " -> "+ MoneyUtils.formatPercentage(percentage, true));
		}
	}
	
	public void notestParsePercentage() throws Exception {
		
		List<Integer> percentages = new ArrayList<Integer>();
		
		percentages.add(25);
		percentages.add(100000000);
		percentages.add(23450000);
		percentages.add(62511000);
		percentages.add(1);
		percentages.add(73726120);
		
		
		for(Integer percentage : percentages){
			System.out.println(percentage + " -> "+ MoneyUtils.parsePercentage(MoneyUtils.formatPercentage(percentage, false)));
		}
	}
	
	public void testCalcPercentage() throws Exception {
		System.out.println( "20% von 600,00 sind " + MoneyUtils.calcPercentage( 60000L, 20000000, true ) );
	}
}
