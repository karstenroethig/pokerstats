package karstenroethig.pokerstats.model.stats;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import karstenroethig.pokerstats.enums.StatsTimeUnitEnum;
import karstenroethig.pokerstats.hibernate.HibernateUtil;
import karstenroethig.pokerstats.model.Participation;
import karstenroethig.pokerstats.model.Tournament;

import org.hibernate.Query;
import org.hibernate.Session;

public class StatsModel {

	private Tournament tournament = null;
	
	private StatsTimeUnitEnum statsTimeUnit = StatsTimeUnitEnum.OVERALL;
	
	private Date dateFrom = null;
	
	private Date dateTo = null;
	
	public StatsModel() {
	}
	
	@SuppressWarnings("unchecked")
	public IStatsDataModel calcStats() throws Exception {
		
		Session session = null;
		
		try {
			session = HibernateUtil.openSession();

			Map<String, Object> params = new HashMap<String, Object>();
			StringBuffer hql = new StringBuffer();
			
			hql.append( "from  Participation " );
			hql.append( "where 1 = 1 " );
			
			if( tournament != null ) {
				hql.append( "and tournament = :tournament " );
				params.put( "tournament", tournament );
			}
			
			if( dateFrom != null ) {
				hql.append( "and date >= :dateFrom ");
				params.put( "dateFrom", dateFrom );
			}
			
			if( dateTo != null ) {
				hql.append( "and date <= :dateTo " );
				params.put( "dateTo", dateTo );
			}
			
			Query query = session.createQuery( hql.toString() );
			
			for( String paramName : params.keySet() ) {
				query.setParameter( paramName, params.get( paramName ) );
			}
			
			List<Participation> result = (List<Participation>)query.list();
			
			// Statistiken erstellen
			IStatsDataModel statsDataModel = statsTimeUnit.createStatsDataModel();
			
			for( Participation participation : result ) {
				statsDataModel.add( participation );
			}
			
			// TODO statsDataModel.fillEmptyDates(dateStart, dateEnd);
			
			return statsDataModel;
			
		} finally {
			HibernateUtil.closeQuietly( session );
		}
	}

	public void setTournament(Tournament tournament) {
		this.tournament = tournament;
	}

	public void setStatsTimeUnit(StatsTimeUnitEnum statsTimeUnit) {
		this.statsTimeUnit = statsTimeUnit;
	}

	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}

	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}
}
