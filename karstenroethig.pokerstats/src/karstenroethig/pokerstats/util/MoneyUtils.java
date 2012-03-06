package karstenroethig.pokerstats.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.commons.lang.StringUtils;

public class MoneyUtils {

	private static final NumberFormat FORMAT_AMOUNT = new DecimalFormat( ",##0.00" );
	
	private static final NumberFormat FORMAT_PERCENTAGE = new DecimalFormat( ",##0.00####" );
	
	public static final String CURRENCY_SIGN_DOLLAR = "$";
	
	public static final String PERCENTAGE_SIGN = "%";
	
	public static final Integer PERCENTAGE_ONE = 1000000;
	
	public static String formatAmount( Long amount, boolean addCurrency ) {
		
		if( amount == null ) {
			return StringUtils.EMPTY;
		}
		
		double amountDouble = amount.doubleValue() / 100.0;
		
		String value = FORMAT_AMOUNT.format( amountDouble );
		
		return addCurrency ? value + " " + CURRENCY_SIGN_DOLLAR : value;
	}
	
	public static Integer parseAmount( String value ) {
		
		if( StringUtils.isBlank( value ) ) {
			return null;
		}
		
		value = StringUtils.trim( value );
		
		if( StringUtils.isNumeric( value ) ) {
			return Integer.parseInt( value ) * 100;
		}
		
		if( value.matches( "[0-9]*[,][0-9]*" ) && StringUtils.equals( value, ",") == false ) {
			
			String result = StringUtils.EMPTY;
			boolean comma = false;
			int afterComma = 0;
			int beforeComma = 0;
			
			for( int i = 0; i < value.length(); i++ ) {
				
				char c = value.charAt( i );
				
				if( c == ',' ) {
					comma = true;
				} else {
					result += c;
					
					if( comma ) {
						afterComma++;
					}else{
						beforeComma++;
					}
				}
				
				if( afterComma == 2 ) {
					break;
				}
			}
			
			result = StringUtils.rightPad(result, beforeComma + 2, "0");
			
			return Integer.parseInt( result );
		}
		
		return null;
	}
	
	public static String formatPercentage( Integer percentage, boolean addPercentageSign ) {
		
		if( percentage == null ) {
			return StringUtils.EMPTY;
		}
		
		double percentageDouble = percentage.doubleValue() / (PERCENTAGE_ONE * 1.0);
		
		String value = FORMAT_PERCENTAGE.format( percentageDouble );
		
		return addPercentageSign ? value + " " + PERCENTAGE_SIGN : value;
	}
	
	public static Integer parsePercentage( String value ) {
		
		if( StringUtils.isBlank( value ) ) {
			return null;
		}
		
		value = StringUtils.trim( value );
		
		if( StringUtils.isNumeric( value ) ) {
			return Integer.parseInt( value ) * PERCENTAGE_ONE;
		}
		
		if( value.matches( "[0-9]*[,][0-9]*" ) && StringUtils.equals( value, ",") == false ) {
			
			String result = StringUtils.EMPTY;
			boolean comma = false;
			int afterComma = 0;
			int beforeComma = 0;
			
			for( int i = 0; i < value.length(); i++ ) {
				
				char c = value.charAt( i );
				
				if( c == ',' ) {
					comma = true;
				} else {
					result += c;
					
					if( comma ) {
						afterComma++;
					}else{
						beforeComma++;
					}
				}
				
				if( afterComma == 6 ) {
					break;
				}
			}
			
			result = StringUtils.rightPad(result, beforeComma + 6, "0");
			
			return Integer.parseInt( result );
		}
		
		return null;
	}
	
	/**
	 * Berechnet den Prozentsatz von einem Betrag.
	 * 
	 * @param amount        Der Betrag in Cent (100 für 1,00 $/€).
	 * @param percentage    Der Prozentsatz (1 für 0,000001%).
	 * @param tradingRound  Soll kfm. gerundet (true) oder abgeschnitten (false) werden?
	 * @return              Der berechnete Prozentsatz in Cent.
	 */
	public static Long calcPercentage( Long amount, Integer percentage, boolean tradingRound ) {
		
		if( amount == null || percentage == null ) {
			return null;
		}
		
		double p = percentage * 1.0;
		p = p / 1000000.0;
		
		double g = amount * 1.0;
		
		Double w = g / 100.0 * p;
		
		// kfm. Runden?
		if( tradingRound ) {
			w = w + 0.5;
		}
		
		return w.longValue();
		
	}
}
