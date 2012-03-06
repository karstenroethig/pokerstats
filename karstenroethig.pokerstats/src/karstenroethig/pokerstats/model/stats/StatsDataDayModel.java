package karstenroethig.pokerstats.model.stats;

import java.util.Date;

public class StatsDataDayModel extends AbstractStatsDataModel {

	@Override
	protected String getKey( Date date ) {
		return "Gesamt";
	}
	
	@Override
	public void fillEmptyDates( Date dateStart, Date dateEnd ) {
		// Nothing to do
	}
}
