package karstenroethig.pokerstats.model.stats;

import java.util.Date;

import karstenroethig.pokerstats.util.DateUtils;

public class StatsDataOverallModel extends AbstractStatsDataModel {

	@Override
	protected String getKey( Date date ) {
		return DateUtils.fomatDate( date );
	}
}
