package karstenroethig.pokerstats.enums;

import karstenroethig.pokerstats.model.stats.IStatsDataModel;
import karstenroethig.pokerstats.model.stats.StatsDataDayModel;
import karstenroethig.pokerstats.model.stats.StatsDataMonthModel;
import karstenroethig.pokerstats.model.stats.StatsDataOverallModel;
import karstenroethig.pokerstats.model.stats.StatsDataWeekModel;
import karstenroethig.pokerstats.model.stats.StatsDataYearModel;

import org.apache.commons.lang.StringUtils;

public enum StatsTimeUnitEnum {

	OVERALL( "Gesamt" ) {
		
		@Override
		public IStatsDataModel createStatsDataModel() {
			return new StatsDataOverallModel();
		}
	},
	
	PER_DAY( "pro Tag" ) {
		
		@Override
		public IStatsDataModel createStatsDataModel() {
			return new StatsDataDayModel();
		}
	},
	
	PER_WEEK( "pro Woche" ) {
		
		@Override
		public IStatsDataModel createStatsDataModel() {
			return new StatsDataWeekModel();
		}
	},
	
	PER_MONTH( "pro Monat" ) {
		
		@Override
		public IStatsDataModel createStatsDataModel() {
			return new StatsDataMonthModel();
		}
	},
	
	PER_YEAR( "pro Jahr" ) {
		
		@Override
		public IStatsDataModel createStatsDataModel() {
			return new StatsDataYearModel();
		}
	};
	
	private String description;
	
	private StatsTimeUnitEnum( String description ) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public IStatsDataModel createStatsDataModel() {
		return null;
	}
	
	public static String[] getValues() {
		
		StatsTimeUnitEnum[] enums = StatsTimeUnitEnum.values();
		String[] values = new String[enums.length];
		
		for( int i = 0; i < enums.length; i++ ) {
			values[i] = enums[i].getDescription();
		}
		
		return values;
	}
	
	public static StatsTimeUnitEnum getByDesciption( String description ) {
		
		StatsTimeUnitEnum[] enums = StatsTimeUnitEnum.values();
		
		for( int i = 0; i < enums.length; i++ ) {
			if( StringUtils.equals( description, enums[i].getDescription() ) ) {
				return enums[i];
			}
		}
		
		return null;
	}
}
