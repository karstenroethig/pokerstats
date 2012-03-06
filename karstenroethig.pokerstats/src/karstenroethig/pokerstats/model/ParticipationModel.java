package karstenroethig.pokerstats.model;

import java.util.ArrayList;
import java.util.List;

import karstenroethig.pokerstats.enums.PrizeScalingTypeEnum;
import karstenroethig.pokerstats.hibernate.HibernateUtil;
import karstenroethig.pokerstats.util.MoneyUtils;
import karstenroethig.pokerstats.validation.ValidationResult;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;

public class ParticipationModel {

	private volatile static ParticipationModel singleton;
	
	private static List<Participation> participations;
	
	private ParticipationModel() {
		participations = new ArrayList<Participation>();
		
		loadData();
	}
	
	public static ParticipationModel getInstance() {
		
		if( singleton == null ) {
			synchronized ( ParticipationModel.class ) {
				if( singleton == null ) {
					singleton = new ParticipationModel();
				}
			}
		}
		
		return singleton;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void loadData() {
		
		participations.clear();
		Session session = null;
		
		try {
			session = HibernateUtil.openSession();
			String hql = "from Participation";
			List result = session.createQuery( hql ).list();
			
			participations.addAll( result );
			
		} catch( HibernateException ex ) {
			ex.printStackTrace();
		} finally {
			HibernateUtil.closeQuietly( session );
		}
	}
	
	public List<Participation> getParticipations() {
		return participations;
	}
	
	public Participation loadParticipation( Long id ) {
		
		Participation participation = null;
		Session session = null;
		
		try {
			session = HibernateUtil.openSession();
			participation = (Participation)session.get( Participation.class, id );
			
		} catch( HibernateException ex ) {
			ex.printStackTrace();
		} finally {
			HibernateUtil.closeQuietly( session );
		}
		
		return participation;
	}
	
	public Participation createNewParticipation() {
		
		Participation participation = new Participation();
		
		return participation;
	}
	
	public Participation createNewParticipation( Tournament tournament ) {
		
		Participation participation = new Participation();
		
		if( tournament != null && tournament.getId() != null ) {
			participation.setTournament( tournament );
			
			tournament = TournamentModel.getInstance().loadTournament( tournament.getId() );
			
			if( tournament.getPrizeScalings().size() == 1 ) {
				PrizeScaling prizeScaling = tournament.getPrizeScalings().get( 0 );
				
				if( prizeScaling.getParticipantsFrom() != null
						&& prizeScaling.getParticipantsFrom().equals( prizeScaling.getParticipantsTo() ) ) {
					participation.setParticipants( prizeScaling.getParticipantsFrom() );
				}
			}
		}
		
		return participation;
	}
	
	public boolean calcSummaryProperties( Participation participation ) {
		
		if( participation == null || participation.getTournament() == null ) {
			return false;
		}
		
		Tournament tournament = participation.getTournament();
		
		if( tournament.getId() != null ) {
			tournament = TournamentModel.getInstance().loadTournament( tournament.getId() );
		}
		
		int participants = 0;
		int place = 0;
		int rebuys = 0;
		int rebuysOthers = 0;
		int addons = 0;
		int addonsOthers = 0;
		
		if( participation.getParticipants() != null ) {
			participants = participation.getParticipants();
		}
		
		if( participation.getPlace() != null ) {
			place = participation.getPlace();
		}
		
		if( participation.getRebuys() != null ) {
			rebuys = participation.getRebuys();
		}
		
		if( participation.getRebuysOthers() != null ) {
			rebuysOthers = participation.getRebuysOthers();
		}
		
		if( participation.getAddons() != null ) {
			addons = participation.getAddons();
		}
		
		if( participation.getAddonsOthers() != null ) {
			addonsOthers = participation.getAddonsOthers();
		}
		
		Long buyinPrize = tournament.getBuyinPrize()
				+ ( rebuys * tournament.getBuyinPrize() )
				+ ( addons * tournament.getBuyinPrize() );
		Long buyinFee = tournament.getBuyinFee();
		
		Long prizeMoneyTotal = tournament.getBuyinPrize()
				* ( participants + rebuys + rebuysOthers + addons + addonsOthers );
		Long prizeMoney = 0L;
		
		for( PrizeScaling prizeScaling : tournament.getPrizeScalings() ) {
			
			if( participants < prizeScaling.getParticipantsFrom()
					|| participants > prizeScaling.getParticipantsTo() ) {
				continue;
			}
			
			PrizeScalingTypeEnum prizeScalingType = PrizeScalingTypeEnum.getByKey( tournament.getPrizeScalingType() );
			
			for( PrizePercentage prizePercentage : prizeScaling.getPrizePercentages() ) {
				
				if( place == prizePercentage.getPlace() ) {
					
					if( prizeScalingType == PrizeScalingTypeEnum.PERCENTAGE ) {
						prizeMoney = MoneyUtils.calcPercentage( prizeMoneyTotal, prizePercentage.getPercentage(), true );
					}else if( prizeScalingType == PrizeScalingTypeEnum.AMOUNT ) {
						prizeMoney = prizePercentage.getAmount() == null ? 0L : prizePercentage.getAmount();
					}
					
					break;
				}
			}
			
			break;
		}
		
		Long benefit = prizeMoney - buyinPrize - buyinFee;
		
		boolean anythingChanged = false;
		
		if( buyinPrize.equals( participation.getBuyinPrize() ) == false ) {
			participation.setBuyinPrize( buyinPrize );
			anythingChanged = true;
		}
		
		if( buyinFee.equals( participation.getBuyinFee() ) == false ) {
			participation.setBuyinFee( buyinFee );
			anythingChanged = true;
		}
		
		if( prizeMoneyTotal.equals( participation.getPrizeMoneyTotal() ) == false ) {
			participation.setPrizeMoneyTotal( prizeMoneyTotal );
			anythingChanged = true;
		}
		
		if( prizeMoney.equals( participation.getPrizeMoney() ) == false ) {
			participation.setPrizeMoney( prizeMoney );
			anythingChanged = true;
		}
		
		if( benefit.equals( participation.getBenefit() ) == false ) {
			participation.setBenefit( benefit );
			anythingChanged = true;
		}
		
		return anythingChanged;
	}
	
	public SaveResult save( Participation participation ) {
		
		if( participation == null ) {
			throw new RuntimeException( "participation == null -> kann nicht gespeichert werden" );
		}
		
		Session session = null;
		Transaction tx = null;
		
		boolean rollback = false;
		String errorMessage = null;
		Throwable th = null;
		
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			
			session.saveOrUpdate( participation );
			
			tx.commit();
		} catch( StaleObjectStateException ex ) {
			
			errorMessage = "Die Daten wurden bereits durch einen anderen Prozess bearbeitet."
					+ " Damit keine Daten verloren gehen, wird der Speichervorgang abgebrochen.";
			th = ex;
			
		} catch( HibernateException ex ) {
			
			rollback = true;
			errorMessage = "Fehler beim Speichern der Turnier-Teilnahme.";
			th = ex;
			
		} finally {
			
			if( rollback ) {
				try {
					if( tx != null && tx.isActive() ) {
						tx.rollback();
					}
				}catch( HibernateException ex) {
					// Nothing to do
				}
			}
			
			if( session != null && session.isOpen() ) {
				try{
					session.close();
				}catch(HibernateException ex) {
					// Nothing to do
				}
			}
			
			if( errorMessage != null && th != null ) {
				return new SaveResult( errorMessage, th );
			}
			
		}
		
		loadData();
		
		return SaveResult.SUCCESS;	
	}
	
	public ValidationResult validate( Participation participation ) {
		
		if( participation == null ) {
			throw new RuntimeException( "participation == null -> kann nicht validiert werden" );
		}
		
		ValidationResult validationResult = new ValidationResult();
		
		/*
		 * Turnier pr�fen
		 */
		if( participation.getTournament() == null ) {
			validationResult.addError( "F�r 'Turnier' ist eine Angabe erforderlich.", "tournament" );
		}
		
		/*
		 * Datum pr�fen
		 */
		if( participation.getDate() == null ) {
			validationResult.addError( "F�r 'Datum' ist eine Angabe erforderlich.", "date" );
		}
		
		/*
		 * Teilnehmer pr�fen
		 */
		if( participation.getParticipants() == null ) {
			validationResult.addError( "F�r 'Teilnehmer' ist eine Angabe erforderlich.", "participants" );
		} else if( participation.getParticipants() <= 0 ) {
			validationResult.addError( "Der Wert f�r 'Teilnehmer' muss gr��er als 0 sein.", "particiants" );
		} else if( participation.getParticipants() > Participation.MAX_PARTICIPANTS ) {
			validationResult.addError( "Der angegeben Wert f�r 'Teilnehmer' ist zu gro� (max. "
					+ Participation.MAX_PARTICIPANTS + ").", "participants" );
		}
		
		/*
		 * Platzierung pr�fen
		 */
		if( participation.getPlace() == null ) {
			validationResult.addError( "F�r 'Platzierung' ist eine Angabe erforderlich.", "place" );
		} else if( participation.getPlace() <= 0 ) {
			validationResult.addError( "Der Wert f�r 'Platzierung' muss gr��er als 0 sein.", "place" );
		} else if( participation.getPlace() > Participation.MAX_PARTICIPANTS ) {
			validationResult.addError( "Der angegeben Wert f�r 'Platzierung' ist zu gro� (max. "
					+ Participation.MAX_PARTICIPANTS + ").", "place" );
		}
		
		if( participation.getParticipants() != null
				&& participation.getPlace() != null
				&& participation.getParticipants() < participation.getPlace() ) {
			validationResult.addError( "Der Wert f�r 'Platzierung' darf nicht gr��er sein als der Wert f�r 'Teilnehmer'.", "place" );
		}
		
		/*
		 * Rebuys pr�fen
		 */
		if( participation.getRebuys() == null ) {
			validationResult.addError( "F�r 'Rebuys (eigene)' ist eine Angabe erforderlich.", "rebuys" );
		} else if( participation.getRebuys() < 0 ) {
			validationResult.addError( "Der Wert f�r 'Rebuys (eigene)' darf nicht kleiner als 0 sein.", "rebuys" );
		} else if( participation.getRebuys() > Participation.MAX_REBUYS ) {
			validationResult.addError( "Der angegeben Wert f�r 'Rebuys (eigene)' ist zu gro� (max. "
					+ Participation.MAX_REBUYS + ").", "rebuys" );
		}
		
		if( participation.getRebuysOthers() == null ) {
			validationResult.addError( "F�r 'Rebuys (andere)' ist eine Angabe erforderlich.", "rebuysOthers" );
		} else if( participation.getRebuysOthers() < 0 ) {
			validationResult.addError( "Der Wert f�r 'Rebuys (andere)' darf nicht kleiner als 0 sein.", "rebuysOthers" );
		} else if( participation.getRebuysOthers() > Participation.MAX_REBUYS ) {
			validationResult.addError( "Der angegeben Wert f�r 'Rebuys (andere)' ist zu gro� (max. "
					+ Participation.MAX_REBUYS + ").", "rebuysOthers" );
		}
		if( participation.getAddons() == null ) {
			validationResult.addError( "F�r 'Add-ons (eigene)' ist eine Angabe erforderlich.", "addons" );
		} else if( participation.getAddons() < 0 ) {
			validationResult.addError( "Der Wert f�r 'Add-ons (eigene)' darf nicht kleiner als 0 sein.", "addons" );
		} else if( participation.getAddons() > Participation.MAX_REBUYS ) {
			validationResult.addError( "Der angegeben Wert f�r 'Add-ons (eigene)' ist zu gro� (max. "
					+ Participation.MAX_REBUYS + ").", "addons" );
		}
		
		if( participation.getAddonsOthers() == null ) {
			validationResult.addError( "F�r 'Add-ons (andere)' ist eine Angabe erforderlich.", "addonsOthers" );
		} else if( participation.getAddonsOthers() < 0 ) {
			validationResult.addError( "Der Wert f�r 'Add-ons (andere)' darf nicht kleiner als 0 sein.", "addonsOthers" );
		} else if( participation.getAddonsOthers() > Participation.MAX_REBUYS ) {
			validationResult.addError( "Der angegeben Wert f�r 'Add-ons (andere)' ist zu gro� (max. "
					+ Participation.MAX_REBUYS + ").", "addonsOthers" );
		}
		
		/*
		 * Berechnete Werte pr�fen
		 */
		if( participation.getBuyinPrize() == null
				|| participation.getBuyinFee() == null
				|| participation.getPrizeMoneyTotal() == null
				|| participation.getPrizeMoney() == null
				|| participation.getBenefit() == null ) {
			validationResult.addError( "Die berechneten Werte der Turnier-Teilnahme sind unvollst�ndig.", StringUtils.EMPTY );
		}
		
		return validationResult;
	}
	
}
