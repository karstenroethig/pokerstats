package karstenroethig.pokerstats.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Entity
public class Participation implements Serializable {
	
	/** Use serialVersionUID for interoperability. */
	private static final long serialVersionUID = 491211965929530903L;
	
	public static final int MAX_PARTICIPANTS = 1000000;
	
	public static final int MAX_REBUYS = 1000000;

	@Id
	@GeneratedValue( strategy = GenerationType.AUTO )
	private Long id;
	
	/** Das Turnier, an dem teilgenommen wurde. */
    @JoinColumn( name = "tournament" )
    @ManyToOne
	private Tournament tournament;
	
	/** Teilnahmedatum. */
	private Date date;
	
	/** Anzahl der Teilnehmer. */
	private Integer participants;
	
	/** Platzierung. */
	private Integer place;
	
	/** Anzahl der eigenen Rebuys. */
	private Integer rebuys;
	
	/** Anzahl der Rebuys anderer Teilnehmer. */
	private Integer rebuysOthers;
	
	/** Anzahl der eigenen Add-ons. */
	private Integer addons;
	
	/** Anzahl der Add-ons anderer Teilnehmer. */
	private Integer addonsOthers;
	
	/** Buy-in Preisgeldanteil. */
	private Long buyinPrize;
	
	/** Buy-in Gebühranteil. */
	private Long buyinFee;
	
	/** Gesamtes Preisgeld des gespielten Turniers. */
	private Long prizeMoneyTotal;
	
	/** Presigeld. */
	private Long prizeMoney;
	
	/** Gewinn/Verlust der Teilnahme. */
	private Long benefit;
	
	@Version
	private Integer version;
	
	public Participation() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Tournament getTournament() {
		return tournament;
	}

	public void setTournament(Tournament tournament) {
		this.tournament = tournament;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Integer getParticipants() {
		return participants;
	}

	public void setParticipants(Integer participants) {
		this.participants = participants;
	}

	public Integer getPlace() {
		return place;
	}

	public void setPlace(Integer place) {
		this.place = place;
	}

	public Integer getRebuys() {
		return rebuys;
	}

	public void setRebuys(Integer rebuys) {
		this.rebuys = rebuys;
	}

	public Integer getRebuysOthers() {
		return rebuysOthers;
	}

	public void setRebuysOthers(Integer rebuysOthers) {
		this.rebuysOthers = rebuysOthers;
	}

	public Integer getAddons() {
		return addons;
	}

	public void setAddons(Integer addons) {
		this.addons = addons;
	}

	public Integer getAddonsOthers() {
		return addonsOthers;
	}

	public void setAddonsOthers(Integer addonsOthers) {
		this.addonsOthers = addonsOthers;
	}

	public Long getBuyinPrize() {
		return buyinPrize;
	}

	public void setBuyinPrize(Long buyinPrize) {
		this.buyinPrize = buyinPrize;
	}

	public Long getBuyinFee() {
		return buyinFee;
	}

	public void setBuyinFee(Long buyinFee) {
		this.buyinFee = buyinFee;
	}

	public Long getPrizeMoneyTotal() {
		return prizeMoneyTotal;
	}

	public void setPrizeMoneyTotal(Long prizeMoneyTotal) {
		this.prizeMoneyTotal = prizeMoneyTotal;
	}

	public Long getPrizeMoney() {
		return prizeMoney;
	}

	public void setPrizeMoney(Long prizeMoney) {
		this.prizeMoney = prizeMoney;
	}

	public Long getBenefit() {
		return benefit;
	}

	public void setBenefit(Long benefit) {
		this.benefit = benefit;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Override
	public boolean equals(Object obj) {
		
		if( obj == null ) { return false; }
		if( obj == this ) { return true; }
		if( obj.getClass() != getClass() ) { return false; }
		
		Participation participation = (Participation)obj;
		
		return new EqualsBuilder()
//			.appendSuper( super.equals( obj ) )
			.append( id, participation.getId() )
			.append( tournament, participation.getTournament() )
			.append( date, participation.getDate() )
			.append( participants, participation.getParticipants() )
			.append( place, participation.getPlace() )
			.isEquals();
	}
	
	@Override
	public int hashCode() {
		
		return new HashCodeBuilder( 71, 97 )
			.append( id )
			.append( tournament )
			.append( date )
			.append( participants )
			.append( place )
			.toHashCode();
	}
	
	@Override
	public String toString() {
		
		return new ToStringBuilder( this )
			.append( "tournament", tournament )
			.append( "date", date )
			.append( "participants", participants )
			.append( "place", place )
			.toString();
	}
}
