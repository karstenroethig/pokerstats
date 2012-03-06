package karstenroethig.pokerstats.model.stats;

import java.util.Date;

import karstenroethig.pokerstats.util.DateUtils;

public class StatsDataWeekModel extends AbstractStatsDataModel {

	@Override
	protected String getKey( Date date ) {
		return DateUtils.getWeekOfYear( date );
	}

}
