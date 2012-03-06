package karstenroethig.pokerstats.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.OrderBy;

@Entity
public class PrizeScaling implements Serializable {

	/** Use serialVersionUID for interoperability. */
	private static final long serialVersionUID = -2538615584896085938L;

	@Id
	@GeneratedValue( strategy = GenerationType.AUTO )
	private Long id;

	/** Das zugehörige Turnier. */
    @JoinColumn( name = "tournament" )
    @ManyToOne
	private Tournament tournament;
	
	/** Teilnehmeranzahl von. */
	private Integer participantsFrom;
	
	/** Teilnehmeranzahl bis. */
	private Integer participantsTo;
	
	/** Anzahl der Teilnehmer, an die Preisgelder vergeben werden. */
	private Integer participantsInPrizes;

	/** Preisanteil der einzelnen Platzierungen. */
	@Cascade( value = org.hibernate.annotations.CascadeType.ALL )
	@OneToMany( mappedBy = "prizeScaling",
			orphanRemoval = true )
	@OrderBy( clause = "place asc" )
	private List<PrizePercentage> prizePercentages;
	
	@Version
	private Integer version;
	
	public PrizeScaling() {
	}
	
	public boolean addPrizePercentage( PrizePercentage prizePercentage ) {
		prizePercentage.setPrizeScaling( this );
		
		return getPrizePercentages().add( prizePercentage );
	}
	
	public boolean removePrizePercentage( PrizePercentage prizePercentage ) {
		prizePercentage.setPrizeScaling( null );
		
		return getPrizePercentages().remove( prizePercentage );
	}

	public Tournament getTournament() {
		return tournament;
	}

	public void setTournament(Tournament tournament) {
		this.tournament = tournament;
	}

	public List<PrizePercentage> getPrizePercentages() {
		if( prizePercentages == null ) {
			this.prizePercentages = new ArrayList<PrizePercentage>();
		}
		
		return prizePercentages;
	}

	public void setPrizePercentages(List<PrizePercentage> prizePercentages) {
		this.prizePercentages = prizePercentages;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getParticipantsFrom() {
		return participantsFrom;
	}

	public void setParticipantsFrom(Integer participantsFrom) {
		this.participantsFrom = participantsFrom;
	}

	public Integer getParticipantsTo() {
		return participantsTo;
	}

	public void setParticipantsTo(Integer participantsTo) {
		this.participantsTo = participantsTo;
	}

	public Integer getParticipantsInPrizes() {
		return participantsInPrizes;
	}

	public void setParticipantsInPrizes(Integer participantsInPrizes) {
		this.participantsInPrizes = participantsInPrizes;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
	
	public PrizeScaling clone() {
		
		PrizeScaling prizeScalingClone = new PrizeScaling();
		
		prizeScalingClone.setParticipantsFrom( getParticipantsFrom() );
		prizeScalingClone.setParticipantsTo( getParticipantsTo() );
		prizeScalingClone.setParticipantsInPrizes( getParticipantsInPrizes() );
		
		for( PrizePercentage prizePercentage : getPrizePercentages() ) {
			
			PrizePercentage prizePercentageClone = prizePercentage.clone();
			
			prizeScalingClone.addPrizePercentage( prizePercentageClone );
		}
		
		return prizeScalingClone;
	}

	@Override
	public boolean equals(Object obj) {
		
		if( obj == null ) { return false; }
		if( obj == this ) { return true; }
		if( obj.getClass() != getClass() ) { return false; }
		
		PrizeScaling scaling = (PrizeScaling)obj;
		
		return new EqualsBuilder()
//			.appendSuper( super.equals( obj ) )
			.append( id, scaling.getId() )
			.append( participantsFrom, scaling.getParticipantsFrom() )
			.append( participantsTo, scaling.getParticipantsTo() )
			.append( participantsInPrizes, scaling.getParticipantsInPrizes() )
			.isEquals();
	}
	
	@Override
	public int hashCode() {
		
		return new HashCodeBuilder( 137, 163 )
			.append( id )
			.append( participantsFrom )
			.append( participantsTo )
			.append( participantsInPrizes )
			.toHashCode();
	}
	
	@Override
	public String toString() {
		
		return new ToStringBuilder( this )
			.append( "participantsFrom", participantsFrom )
			.append( "participantsTo", participantsTo )
			.append( "participantsInPrizes", participantsInPrizes )
			.toString();
	}

}
