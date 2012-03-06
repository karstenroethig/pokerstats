package karstenroethig.pokerstats.enums;

import org.apache.commons.lang.StringUtils;

public enum PrizeScalingTypeEnum {

	PERCENTAGE( 1, "als Prozentsatz" ),
	
	AMOUNT( 2, "als Betrag" );
	
	private int key;
	
	private String description;
	
	private PrizeScalingTypeEnum( int key, String description ) {
		this.key = key;
		this.description = description;
	}

	public Integer getKey() {
		return key;
	}

	public String getDescription() {
		return description;
	}
	
	public static String[] getValues() {
		
		PrizeScalingTypeEnum[] enums = PrizeScalingTypeEnum.values();
		String[] values = new String[enums.length];
		
		for( int i = 0; i < enums.length; i++ ) {
			values[i] = enums[i].getDescription();
		}
		
		return values;
	}
	
	public static PrizeScalingTypeEnum getByKey( int key ) {
		
		PrizeScalingTypeEnum[] enums = PrizeScalingTypeEnum.values();
		
		for( int i = 0; i < enums.length; i++ ) {
			if( enums[i].getKey() == key ) {
				return enums[i];
			}
		}
		
		return null;
	}
	
	public static PrizeScalingTypeEnum getByDescription( String description ) {
		
		PrizeScalingTypeEnum[] enums = PrizeScalingTypeEnum.values();
		
		for( int i = 0; i < enums.length; i++ ) {
			if( StringUtils.equals( enums[i].getDescription(), description ) ) {
				return enums[i];
			}
		}
		
		return null;
	}
}
