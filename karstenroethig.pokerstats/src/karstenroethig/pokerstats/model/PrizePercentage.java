package karstenroethig.pokerstats.model;

import java.io.Serializable;

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
public class PrizePercentage implements Serializable {

	/** Use serialVersionUID for interoperability. */
	private static final long serialVersionUID = 7224649806112592599L;

	@Id
	@GeneratedValue( strategy = GenerationType.AUTO )
	private Long id;
	
	@JoinColumn( name = "priceScaling" )
    @ManyToOne
	private PrizeScaling prizeScaling;
	
	/** Platzierung. */
	private Integer place;
	
	/** Gewinnanteil in Prozent (100000000 für 100,000000%). */
	private Integer percentage;
	
	/** Gewinnanteil als Betrag. */
	private Long amount;
	
	@Version
	private Integer version;
	
	public PrizePercentage() {
	}

	public PrizeScaling getPrizeScaling() {
		return prizeScaling;
	}

	public void setPrizeScaling(PrizeScaling prizeScaling) {
		this.prizeScaling = prizeScaling;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getPlace() {
		return place;
	}

	public void setPlace(Integer place) {
		this.place = place;
	}

	public Integer getPercentage() {
		return percentage;
	}

	public void setPercentage(Integer percentage) {
		this.percentage = percentage;
	}

	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
	
	public PrizePercentage clone() {
		
		PrizePercentage prizePercentageClone = new PrizePercentage();
		
		prizePercentageClone.setPlace( getPlace() );
		prizePercentageClone.setPercentage( getPercentage() );
		
		return prizePercentageClone;
	}

	@Override
	public boolean equals(Object obj) {
		
		if( obj == null ) { return false; }
		if( obj == this ) { return true; }
		if( obj.getClass() != getClass() ) { return false; }
		
		PrizePercentage prizePercentage = (PrizePercentage)obj;
		
		return new EqualsBuilder()
//			.appendSuper( super.equals( obj ) )
			.append( id, prizePercentage.getId() )
			.append( place, prizePercentage.getPlace() )
			.append( percentage, prizePercentage.getPercentage() )
			.isEquals();
	}
	
	@Override
	public int hashCode() {
		
		return new HashCodeBuilder( 199, 233 )
			.append( id )
			.append( place )
			.append( percentage )
			.toHashCode();
	}
	
	@Override
	public String toString() {
		
		return new ToStringBuilder( this )
			.append( "place", place )
			.append( "percentage", percentage )
			.toString();
	}

}
