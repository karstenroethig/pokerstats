package karstenroethig.pokerstats.model.stats;

import java.util.Date;

import karstenroethig.pokerstats.model.Participation;
import karstenroethig.pokerstats.util.CompareToUtils;

public class StatsData implements Comparable<StatsData> {
	
	private String time;

	/** Datum. */
	private Date date;
	
	/** Buyin. */
	private Long buyin;
	
	/** Presigeld. */
	private Long prizeMoney;
	
	/** Gewinn/Verlust. */
	private Long benefit;
	
	public StatsData() {
		this.buyin = 0L;
		this.prizeMoney = 0L;
		this.benefit = 0L;
	}
	
	public void add( String time, Participation participation ) {
		
		if( participation == null ) {
			return;
		}
		
		if( this.time == null ) {
			this.time = time;
		}
		
		if( date == null ) {
			date = participation.getDate();
		}
		
		if( participation.getBuyinPrize() != null ) {
			buyin += participation.getBuyinPrize();
		}
		
		if( participation.getBuyinFee() != null ) {
			buyin += participation.getBuyinFee();
		}
		
		if( participation.getPrizeMoney() != null ) {
			prizeMoney += participation.getPrizeMoney();
		}
		
		if( participation.getBenefit() != null ) {
			benefit += participation.getBenefit();
		}
	}
	
	public void setTime( String time, Date date ) {
		
		if( time == null || date == null ) {
			return;
		}
		
		this.time = time;
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

	public Long getBuyin() {
		return buyin;
	}

	public Long getPrizeMoney() {
		return prizeMoney;
	}

	public Long getBenefit() {
		return benefit;
	}

	@Override
	public int compareTo( StatsData statsData ) {

		if( statsData == null ) {
			return 1;
		}
		
		return CompareToUtils.compare( this.getDate(), statsData.getDate() );
	}
}
