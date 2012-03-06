package karstenroethig.pokerstats.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import karstenroethig.pokerstats.enums.PrizeScalingTypeEnum;
import karstenroethig.pokerstats.hibernate.HibernateUtil;
import karstenroethig.pokerstats.util.MoneyUtils;
import karstenroethig.pokerstats.validation.ValidationResult;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;

public class TournamentModel {

	private volatile static TournamentModel singleton;
	
	private static List<Tournament> tournaments;
	
	private TournamentModel() {
		tournaments = new ArrayList<Tournament>();
		
		loadData();
	}
	
	public static TournamentModel getInstance() {
		
		if( singleton == null ) {
			synchronized ( TournamentModel.class ) {
				if( singleton == null ) {
					singleton = new TournamentModel();
				}
			}
		}
		
		return singleton;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void loadData() {
		
		tournaments.clear();
		Session session = null;
		
		try {
			session = HibernateUtil.openSession();
			String hql = "from Tournament";
			List result = session.createQuery( hql ).list();
			
			tournaments.addAll( result );
			
		} catch( HibernateException ex ) {
			ex.printStackTrace();
		} finally {
			HibernateUtil.closeQuietly( session );
		}
	}
	
	public List<Tournament> getTournaments() {
		return tournaments;
	}
	
	public Tournament loadTournament( Long id ) {
		
		Tournament tournament = null;
		Session session = null;
		
		try {
			session = HibernateUtil.openSession();
			tournament = (Tournament)session.get( Tournament.class, id );
			
			Hibernate.initialize( tournament.getPrizeScalings() );
			
			for( PrizeScaling prizeScaling : tournament.getPrizeScalings() ) {
				Hibernate.initialize( prizeScaling.getPrizePercentages() );
			}
			
		} catch( HibernateException ex ) {
			ex.printStackTrace();
		} finally {
			HibernateUtil.closeQuietly( session );
		}
		
		return tournament;
	}
	
	public Tournament createNewTournament() {
		
		Tournament tournament = new Tournament();
		
		tournament.setDesciption( StringUtils.EMPTY );
		tournament.setBuyinPrize( 0L );
		tournament.setBuyinFee( 0L );
		
		return tournament;
	}
	
	public PrizeScaling createNewPrizeScaling( Tournament tournament ) {
		
		PrizeScaling prizeScaling = new PrizeScaling();
		
		if( tournament.getPrizeScalings() != null
				&& tournament.getPrizeScalings().isEmpty() == false ) {
			
			PrizeScaling previous = tournament.getPrizeScalings().get( tournament.getPrizeScalings().size() - 1 );
			
			if( previous.getParticipantsFrom() != null ) {
				prizeScaling.setParticipantsFrom( previous.getParticipantsTo() + 1 );
			}
		}
		
		tournament.addPrizeScaling( prizeScaling );
		
		return prizeScaling;
	}
	
	public PrizePercentage createNewPrizePercentage( PrizeScaling prizeScaling ) {
		
		PrizePercentage prizePercentage = new PrizePercentage();
		
		synchronized ( TournamentModel.class ) {
			
			int size = prizeScaling.getPrizePercentages().size();
			
			prizePercentage.setPlace( ++size );
			prizeScaling.addPrizePercentage( prizePercentage );
			
		}
		
		return prizePercentage;
	}
	
	public boolean removePrizeScaling( PrizeScaling prizeScaling ) {
		
		Tournament tournament = prizeScaling.getTournament();
		
		if( tournament != null ) {
			return tournament.removePrizeScaling( prizeScaling );
		}
		
		return false;
	}
	
	public void clonePrizeScalings( Tournament tournamentSrc, Tournament tournamentDest ) {
		
		if( tournamentSrc == null || tournamentDest == null ) {
			return;
		}
		
		tournamentSrc = loadTournament( tournamentSrc.getId() );
		
		// Bestehende Preisverteilungen l�schen
		Set<PrizeScaling> prizeScalingsRemove = new HashSet<PrizeScaling>();
		
		prizeScalingsRemove.addAll( tournamentDest.getPrizeScalings() );
		
		for( PrizeScaling prizeScaling : prizeScalingsRemove ) {
			removePrizeScaling( prizeScaling );
		}
		
		// Preisverteilung von Quelle clonen
		for( PrizeScaling prizeScaling : tournamentSrc.getPrizeScalings() ) {
			
			PrizeScaling prizeScalingColne = prizeScaling.clone();
			
			tournamentDest.addPrizeScaling( prizeScalingColne );
		}
		
	}
	
	public void refreshPrizePercentageList( PrizeScaling prizeScaling ) {
		
		if( prizeScaling == null
				|| prizeScaling.getParticipantsInPrizes() == null
				|| prizeScaling.getParticipantsInPrizes() < 0 ) {
			return;
		}
		
		int max = Math.max( prizeScaling.getPrizePercentages().size(),
				prizeScaling.getParticipantsInPrizes() );
		Set<PrizePercentage> remove = new HashSet<PrizePercentage>();
		
		for( int i = 0; i < max; i++ ) {
			
			// Hinzuf�gen
			if( ( i + 1 ) > prizeScaling.getPrizePercentages().size() ) {
				createNewPrizePercentage( prizeScaling );
			}
			
			// L�schen
			if( ( i + 1 ) > prizeScaling.getParticipantsInPrizes() ) {
				
				PrizePercentage prizePercentage = prizeScaling.getPrizePercentages().get( i );
				
				remove.add( prizePercentage );
			}
		}
		
		for( PrizePercentage prizePercentage : remove ) {
			prizeScaling.removePrizePercentage( prizePercentage );
		}
		
	}
	
	public void updatePercentages( PrizeScaling prizeScaling, int from, int to, Integer percentage ) {
		
		for( PrizePercentage prizePercentage : prizeScaling.getPrizePercentages() ) {
			
			if( prizePercentage.getPlace() >= from && prizePercentage.getPlace() <= to ) {
				prizePercentage.setPercentage( percentage );
			}
		}
	}
	
	public void updateAmounts( PrizeScaling prizeScaling, int from, int to, Integer amount ) {
		
		Long amountLong = null;
		
		if( amount != null ) {
			amountLong = amount.longValue();
		}
		
		for( PrizePercentage prizePercentage : prizeScaling.getPrizePercentages() ) {
			
			if( prizePercentage.getPlace() >= from && prizePercentage.getPlace() <= to ) {
				prizePercentage.setAmount( amountLong );
			}
		}
	}
	
	private void cleanUpBeforeSave( Tournament tournament ) {
		
		PrizeScalingTypeEnum prizeScalingType = PrizeScalingTypeEnum.getByKey( tournament.getPrizeScalingType() );
		
		for( PrizeScaling prizeScaling : tournament.getPrizeScalings() ) {
			
			for( PrizePercentage prizePercentage : prizeScaling.getPrizePercentages() ) {
				
				if( prizeScalingType == PrizeScalingTypeEnum.PERCENTAGE ) {
					prizePercentage.setAmount( null );
				} else if( prizeScalingType == PrizeScalingTypeEnum.AMOUNT ) {
					prizePercentage.setPercentage( null );
				}
			}
		}
	}
	
	public SaveResult save( Tournament tournament ) {
		
		if( tournament == null ) {
			throw new RuntimeException( "tournament == null -> kann nicht gespeichert werden" );
		}
		
		cleanUpBeforeSave( tournament );
		
		Session session = null;
		Transaction tx = null;
		
		boolean rollback = false;
		String errorMessage = null;
		Throwable th = null;
		
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			
			session.saveOrUpdate( tournament );
			
			tx.commit();
		} catch( StaleObjectStateException ex ) {
			
			errorMessage = "Die Daten wurden bereits durch einen anderen Prozess bearbeitet."
					+ " Damit keine Daten verloren gehen, wird der Speichervorgang abgebrochen.";
			th = ex;
			
		} catch( HibernateException ex ) {
			
			rollback = true;
			errorMessage = "Fehler beim Speichern des Turniers.";
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
			
			HibernateUtil.closeQuietly( session );
			
			if( errorMessage != null && th != null ) {
				return new SaveResult( errorMessage, th );
			}
			
		}
		
		loadData();
		
		return SaveResult.SUCCESS;	
	}
	
	public ValidationResult validate( Tournament tournament ) {
		
		if( tournament == null ) {
			throw new RuntimeException( "tournament == null -> kann nicht validiert werden" );
		}
		
		ValidationResult validationResult = new ValidationResult();
		
		/*
		 * Beschreibung pr�fen
		 */
		if( StringUtils.isBlank( tournament.getDesciption() ) ) {
			validationResult.addError( "F�r 'Beschreibung' ist eine Angabe erforderlich.", "description" );
		} else if( tournament.getDesciption().length() > Tournament.MAX_LENGTH_DESCRIPTION ) {
			validationResult.addError( "Der angegebene Wert f�r 'Beschreibung' ist zu lang (max. "
					+ Tournament.MAX_LENGTH_DESCRIPTION + " Zeichen).", "description" );
		}
		
		/*
		 * Buy-in (Preisgeldanteil) pr�fen
		 */
		if( tournament.getBuyinPrize() == null ) {
			validationResult.addError( "F�r 'Buy-in (Preisgeldanteil)' ist eine Angabe erforderlich.", "buyinPrize" );
		} else if( tournament.getBuyinPrize() <= 0 ) {
			validationResult.addError( "Der Wert f�r 'Buy-in (Preisgeldanteil)' muss gr��er als 0 sein.", "buyinPrize" );
		} else if( tournament.getBuyinPrize() > Tournament.MAX_BUYIN_PRIZE ) {
			validationResult.addError( "Der angegeben Wert f�r 'Buy-in (Preisgeldanteil)' ist zu gro� (max. "
					+ MoneyUtils.formatAmount( new Long( Tournament.MAX_BUYIN_PRIZE ), true ) + ").", "buyinPrize" );
		}
		
		/*
		 * Buy-in (Geb�hrenanteil) pr�fen
		 */
		if( tournament.getBuyinFee() == null ) {
			validationResult.addError( "F�r 'Buy-in (Geb�hrenanteil)' ist eine Angabe erforderlich.", "buyinFee" );
		} else if( tournament.getBuyinFee() <= 0 ) {
			validationResult.addError( "Der Wert f�r 'Buy-in (Geb�hrenanteil)' muss gr��er als 0 sein.", "buyinFee" );
		} else if( tournament.getBuyinFee() > Tournament.MAX_BUYIN_FEE ) {
			validationResult.addError( "Der angegeben Wert f�r 'Buy-in (Geb�hrenanteil)' ist zu gro� (max. "
					+ MoneyUtils.formatAmount( new Long( Tournament.MAX_BUYIN_FEE ), true ) + ").", "buyinFee" );
		}
		
		/*
		 * Rebuy
		 */
		if( tournament.getRebuy() == null ) {
			validationResult.addError( "F�r 'Rebuy m�glich' muss eine Auswahl getroffen werden.", "rebuy" );
		}
		
		/*
		 * Preisverteilungen pr�fen
		 */
		PrizeScalingTypeEnum prizeScalingType = null;
		
		if( tournament.getPrizeScalingType() != null ) {
			prizeScalingType = PrizeScalingTypeEnum.getByKey( tournament.getPrizeScalingType() );
		}
		
		if( prizeScalingType == null ) {
			validationResult.addError( "Die Art der Angabe der Preisverteilung muss angegeben werden.", "prizeScalingType" );
		}
		
		if( tournament.getPrizeScalings().isEmpty() ) {
			validationResult.addError( "Es muss eine Preisverteilung angegeben weden.", "prizeScalings");
		}
		
		for( int i = 0; i < tournament.getPrizeScalings().size(); i++ ) {
			
			PrizeScaling prizeScaling = tournament.getPrizeScalings().get( i );
			PrizeScaling previous = ( i == 0 ? null : tournament.getPrizeScalings().get( i - 1 ) );
			
			String errorPrefix = "Preisverteilung Zeile " + ( i + 1 ) + ": ";
			
			// Teilnehmer von
			if( prizeScaling.getParticipantsFrom() == null ) {
				validationResult.addError( errorPrefix + "F�r 'Teilnehmer von' muss ein Wert angegeben werden.",
						"prizeScalings.participantsFrom");
			} else if( prizeScaling.getParticipantsFrom() > Participation.MAX_PARTICIPANTS ) {
				validationResult.addError( errorPrefix + "Der Wert f�r 'Teilnehmer von' ist zu gro� (max. "
						+ Participation.MAX_PARTICIPANTS + ").", "prizeScalings.participantsFrom");
			}
			
			// Teilnehmer bis
			if( prizeScaling.getParticipantsTo() == null ) {
				validationResult.addError( errorPrefix + "F�r 'Teilnehmer bis' muss ein Wert angegeben werden.",
						"prizeScalings.participantsTo");
			} else if( prizeScaling.getParticipantsTo() > Participation.MAX_PARTICIPANTS ) {
					validationResult.addError( errorPrefix + "Der Wert f�r 'Teilnehmer bis' ist zu gro� (max. "
						+ Participation.MAX_PARTICIPANTS + ").", "prizeScalings.participantsTo");
			}
			
			// bezahlte Pl�tze
			if( prizeScaling.getParticipantsInPrizes() == null ) {
				validationResult.addError( errorPrefix + "F�r 'bezahlte Pl�tze' muss ein Wert angegeben werden.",
						"prizeScalings.participantsInPrizes");
			} else if( prizeScaling.getParticipantsInPrizes() < 1 ) {
				validationResult.addError( errorPrefix + "'bezahlte Pl�tze' muss gr��er als 0 sein.",
						"prizeScalings.participantsInPrizes" );
			} else if( prizeScaling.getParticipantsFrom() != null
					&& prizeScaling.getParticipantsInPrizes() > prizeScaling.getParticipantsFrom() ) {
				validationResult.addError( errorPrefix + "'bezahlte Pl�tze' darf nicht gr��er sein als 'Teilnehmer von'.",
						"prizeScalings.participantsInPrizes");
			}
			
			// Daten im Vergleich zum Vorg�nge pr�fen
			if( previous != null ) {
				
				if( prizeScaling.getParticipantsFrom() != null
						&& previous.getParticipantsTo() != null ) {
					int diff = prizeScaling.getParticipantsFrom() - previous.getParticipantsTo();
					
					if( diff != 1 ) {
						validationResult.addError( errorPrefix
								+ "'Teilnehmer von' muss genau um 1 gr��er sein als 'Teilnehmer bis' vom Vorg�nger.",
								"prizeScalings.participantsFrom" );
					}
				}
			}
			
			previous = prizeScaling;
			
			// Prozents�tze pr�fen
			if( prizeScaling.getPrizePercentages().isEmpty() ) {
				validationResult.addError( errorPrefix + "Es m�ssen Prozents�tze f�r die einzelnen Pl�tze angegeben weden.",
						"prizeScalings.prizePercentage");
			} else if( prizeScaling.getParticipantsInPrizes() != null
					&& prizeScaling.getPrizePercentages().size() != prizeScaling.getParticipantsInPrizes() ) {
				validationResult.addError( errorPrefix
						+ "'bezahlte Pl�tze' muss genau mit der Anzahl der Prozents�tze f�r die einzelnen Pl�tze �bereinstimmen.",
						"prizeScalings.prizePercentage");
			}
			
			int percentage = 0;
			int previousPercentage = -1;
			
			long amount = 0l;
			long previousAmount = -1l;
			
			for( int j = 0; j < prizeScaling.getPrizePercentages().size(); j++ ) {
				
				PrizePercentage prizePercentage = prizeScaling.getPrizePercentages().get( j );
				
				if( prizePercentage.getPlace() == null ) {
					validationResult.addError( errorPrefix + "F�r 'Platz' muss ein Wert angegeben weden.",
							"prizeScalings.prizePercentage.place" );
				} else if( prizePercentage.getPlace() != ( j + 1 ) ) {
					validationResult.addError( errorPrefix + "Der Wert f�r 'Platz' muss fortlaufend angegeben werden.",
							"prizeScalings.prizePercentage.place" );
				}
				
				if( prizeScalingType == PrizeScalingTypeEnum.PERCENTAGE ) {
					
					if( prizePercentage.getPercentage() == null ) {
						validationResult.addError( errorPrefix + "F�r 'Preisgeld' muss ein Wert angegeben werden.",
								"prizeScalings.prizePercentage.percentage" );
					} else if( prizePercentage.getPercentage().equals( 0 ) ) {
						validationResult.addError( errorPrefix + "Der Prozentsatz f�r 'Preisgeld' muss gr��er als 0% sein.",
								"prizeScalings.prizePercentage.percentage" );
					} else {
						percentage += prizePercentage.getPercentage();
						
						if( previousPercentage != -1
								&& prizePercentage.getPercentage() > previousPercentage ) {
							validationResult.addError( errorPrefix + "Der Prozentsatz f�r 'Preisgeld' darf nicht steigen.",
									"prizeScalings.prizePercentage.percentage" );
						}
						
						previousPercentage = prizePercentage.getPercentage();
					}
				} else if( prizeScalingType == PrizeScalingTypeEnum.AMOUNT ) {
					
					if( prizePercentage.getAmount() == null ) {
						validationResult.addError( errorPrefix + "F�r 'Preisgeld' muss ein Wert angegeben werden.",
								"prizeScalings.prizePercentage.amount" );
					} else if( prizePercentage.getAmount().equals( 0L ) ) {
						validationResult.addError( errorPrefix + "Der Betrag f�r 'Preisgeld' muss gr��er als 0,00 sein.",
								"prizeScalings.prizePercentage.amount" );
					} else {
						amount += prizePercentage.getAmount();
						
						if( previousAmount != -1
								&& prizePercentage.getAmount() > previousAmount ) {
							validationResult.addError( errorPrefix + "Der Betrag f�r 'Preisgeld' darf nicht steigen.",
									"prizeScalings.prizePercentage.amount" );
						}
						
						previousAmount = prizePercentage.getAmount();
					}
				}
			}
			
			if( percentage != 100000000 && prizeScalingType == PrizeScalingTypeEnum.PERCENTAGE ) {
				validationResult.addError( errorPrefix + "Die Summe f�r 'Preisgeld' muss genau 100% ergeben.",
						"prizeScalings.prizePercentage.percentage" );
			}
			
			if( prizeScalingType == PrizeScalingTypeEnum.AMOUNT
					&& tournament.getBuyinPrize() != null
					&& prizeScaling.getParticipantsFrom() != null
					&& prizeScaling.getParticipantsTo() != null ) {
				
				long amountMin = tournament.getBuyinPrize() * prizeScaling.getParticipantsFrom();
				long amountMax = tournament.getBuyinPrize() * prizeScaling.getParticipantsTo();
				
				if( amount < amountMin ) {
					validationResult.addError( errorPrefix + "Die Summe f�r 'Preisgeld' darf nicht kleiner sein als der Faktor aus 'Buy-in (Geweinnanteil)' und 'Teilnehmer von'.",
							"prizeScalings.prizePercentage.amount" );
				} else if( amount > amountMax ) {
					validationResult.addError( errorPrefix + "Die Summe f�r 'Preisgeld' darf nicht gr��er sein als der Faktor aus 'Buy-in (Geweinnanteil)' und 'Teilnehmer bis'.",
							"prizeScalings.prizePercentage.amount" );
				}
			}
			
		}
		
		return validationResult;
	}
	
}
