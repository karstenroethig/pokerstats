package karstenroethig.pokerstats.model.stats;

import java.util.Date;

import karstenroethig.pokerstats.model.Participation;

public interface IStatsDataModel {

	public void add( Participation participation );
	
	public void fillEmptyDates( Date dateStart, Date dateEnd );
}
