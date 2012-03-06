package karstenroethig.pokerstats.model.stats;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import karstenroethig.pokerstats.model.Participation;
import karstenroethig.pokerstats.util.DateUtils;

import org.apache.commons.lang.StringUtils;


public abstract class AbstractStatsDataModel implements IStatsDataModel {
	
	private Map<String, StatsData> datas = new HashMap<String, StatsData>();
	
	public void add( Participation participation ) {
		
		if( participation == null ) {
			return;
		}
		
		String key = getKey( participation.getDate() );
		
		if( StringUtils.isBlank( key ) ) {
			return;
		}
		
		StatsData data;
		
		if( datas.containsKey( key ) ) {
			data = datas.get( key );
		} else {
			data = new StatsData();
			datas.put( key, data );
		}
		
		data.add( key, participation );
	}
	
	public void fillEmptyDates( Date dateStart, Date dateEnd ) {
		
		Set<Date> dates = DateUtils.findDatesBetweenStartAndEnd( dateStart, dateEnd );
		
		for( Date date : dates ) {
			
			String key = getKey( date );
			
			if( StringUtils.isBlank( key ) || datas.containsKey( key ) ) {
				continue;
			}
			
			StatsData statsData = new StatsData();
			statsData.setTime( key, date );
			
			datas.put( key, statsData );
		}
	}
	
	protected abstract String getKey( Date date );
}
