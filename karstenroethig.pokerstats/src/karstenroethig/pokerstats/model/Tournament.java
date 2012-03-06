package karstenroethig.pokerstats.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.OrderBy;

@Entity
public class Tournament implements Serializable {

	/** Use serialVersionUID for interoperability. */
	private static final long serialVersionUID = 1327674143006473771L;
	
	public static final int MAX_LENGTH_DESCRIPTION = 250;
	
	public static final int MAX_BUYIN_PRIZE = 99999999;
	
	public static final int MAX_BUYIN_FEE = 9999999;

	@Id
	@GeneratedValue( strategy = GenerationType.AUTO )
	private Long id;

	/** Beschreibung des Turniers. */
	private String desciption;
	
	/** Buy-in Preisgeldanteil. */
	private Long buyinPrize;
	
	/** Buy-in Gebührenanteil. */
	private Long buyinFee;
	
	/** Gibt an, ob Rebuys erlaubt sind. */
	private Boolean rebuy;
	
	/** Art der Angabe des Preisgeldes. */
	private Integer prizeScalingType;
	
	/** Staffelung der bezahlten Plätze. */
	@Cascade( value = org.hibernate.annotations.CascadeType.ALL )
	@OneToMany( mappedBy = "tournament", orphanRemoval = true )
	@OrderBy( clause = "participantsFrom asc" )
	private List<PrizeScaling> prizeScalings;
	
	/** Teilnahmen an dem Turnier. */
	@Cascade( value = org.hibernate.annotations.CascadeType.ALL )
	@OneToMany(
			fetch = FetchType.LAZY,
			mappedBy = "tournament",
			orphanRemoval = true )
	@OrderBy( clause = "date asc" )
	private List<Participation> participations;
	
	@Version
	private Integer version;
	
	public Tournament() {
	}
	
	public boolean addPrizeScaling( PrizeScaling prizeScaling ) {
		prizeScaling.setTournament( this );
		
		return getPrizeScalings().add( prizeScaling );
	}
	
	public boolean removePrizeScaling( PrizeScaling prizeScaling ) {
		prizeScaling.setTournament( null );
		
		return getPrizeScalings().remove( prizeScaling );
	}
	
	public boolean addParticipation( Participation participation ) {
		participation.setTournament( this );
		
		return getParticipations().add( participation );
	}
	
	public boolean removeParticipation( Participation participation ) {
		participation.setTournament( null );
		
		return getParticipations().remove( participation );
	}
	
	public List<PrizeScaling> getPrizeScalings() {
		
		if( prizeScalings == null ) {
			this.prizeScalings = new ArrayList<PrizeScaling>();
		}
		
		return prizeScalings;
	}

	public void setPrizeScalings(List<PrizeScaling> prizeScalings) {
		this.prizeScalings = prizeScalings;
	}

	public List<Participation> getParticipations() {
		
		if( participations == null ) {
			this.participations = new ArrayList<Participation>();
		}
		
		return participations;
	}

	public void setParticipations(List<Participation> participations) {
		this.participations = participations;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDesciption() {
		return desciption;
	}

	public void setDesciption(String desciption) {
		this.desciption = desciption;
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

	public Boolean getRebuy() {
		return rebuy;
	}

	public void setRebuy(Boolean rebuy) {
		this.rebuy = rebuy;
	}

	public Integer getPrizeScalingType() {
		return prizeScalingType;
	}

	public void setPrizeScalingType(Integer prizeScalingType) {
		this.prizeScalingType = prizeScalingType;
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
		
		Tournament tournament = (Tournament)obj;
		
		return new EqualsBuilder()
//			.appendSuper( super.equals( obj ) )
			.append( id, tournament.getId() )
			.append( desciption, tournament.getDesciption() )
			.append( buyinPrize, tournament.getBuyinPrize() )
			.append( buyinFee, tournament.getBuyinFee() )
			.append( rebuy, tournament.getRebuy() )
			.append( prizeScalingType, tournament.getPrizeScalingType() )
			.isEquals();
	}
	
	@Override
	public int hashCode() {
		
		return new HashCodeBuilder( 17, 37 )
			.append( id )
			.append( desciption )
			.append( buyinPrize )
			.append( buyinFee )
			.append( rebuy )
			.append( prizeScalingType )
			.toHashCode();
	}
	
	@Override
	public String toString() {
		
		return new ToStringBuilder( this )
			.append( "description", desciption )
			.append( "buyinPrize", buyinPrize )
			.append( "buyinFee", buyinFee )
			.append( "rebuy", rebuy )
			.append( "prizeScalingType", prizeScalingType )
			.toString();
	}
}
