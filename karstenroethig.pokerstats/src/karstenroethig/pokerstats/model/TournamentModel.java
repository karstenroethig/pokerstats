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
		
		// Bestehende Preisverteilungen löschen
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
			
			// Hinzufügen
			if( ( i + 1 ) > prizeScaling.getPrizePercentages().size() ) {
				createNewPrizePercentage( prizeScaling );
			}
			
			// Löschen
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
		 * Beschreibung prüfen
		 */
		if( StringUtils.isBlank( tournament.getDesciption() ) ) {
			validationResult.addError( "Für 'Beschreibung' ist eine Angabe erforderlich.", "description" );
		} else if( tournament.getDesciption().length() > Tournament.MAX_LENGTH_DESCRIPTION ) {
			validationResult.addError( "Der angegebene Wert für 'Beschreibung' ist zu lang (max. "
					+ Tournament.MAX_LENGTH_DESCRIPTION + " Zeichen).", "description" );
		}
		
		/*
		 * Buy-in (Preisgeldanteil) prüfen
		 */
		if( tournament.getBuyinPrize() == null ) {
			validationResult.addError( "Für 'Buy-in (Preisgeldanteil)' ist eine Angabe erforderlich.", "buyinPrize" );
		} else if( tournament.getBuyinPrize() <= 0 ) {
			validationResult.addError( "Der Wert für 'Buy-in (Preisgeldanteil)' muss größer als 0 sein.", "buyinPrize" );
		} else if( tournament.getBuyinPrize() > Tournament.MAX_BUYIN_PRIZE ) {
			validationResult.addError( "Der angegeben Wert für 'Buy-in (Preisgeldanteil)' ist zu groß (max. "
					+ MoneyUtils.formatAmount( new Long( Tournament.MAX_BUYIN_PRIZE ), true ) + ").", "buyinPrize" );
		}
		
		/*
		 * Buy-in (Gebührenanteil) prüfen
		 */
		if( tournament.getBuyinFee() == null ) {
			validationResult.addError( "Für 'Buy-in (Gebührenanteil)' ist eine Angabe erforderlich.", "buyinFee" );
		} else if( tournament.getBuyinFee() <= 0 ) {
			validationResult.addError( "Der Wert für 'Buy-in (Gebührenanteil)' muss größer als 0 sein.", "buyinFee" );
		} else if( tournament.getBuyinFee() > Tournament.MAX_BUYIN_FEE ) {
			validationResult.addError( "Der angegeben Wert für 'Buy-in (Gebührenanteil)' ist zu groß (max. "
					+ MoneyUtils.formatAmount( new Long( Tournament.MAX_BUYIN_FEE ), true ) + ").", "buyinFee" );
		}
		
		/*
		 * Rebuy
		 */
		if( tournament.getRebuy() == null ) {
			validationResult.addError( "Für 'Rebuy möglich' muss eine Auswahl getroffen werden.", "rebuy" );
		}
		
		/*
		 * Preisverteilungen prüfen
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
				validationResult.addError( errorPrefix + "Für 'Teilnehmer von' muss ein Wert angegeben werden.",
						"prizeScalings.participantsFrom");
			} else if( prizeScaling.getParticipantsFrom() > Participation.MAX_PARTICIPANTS ) {
				validationResult.addError( errorPrefix + "Der Wert für 'Teilnehmer von' ist zu groß (max. "
						+ Participation.MAX_PARTICIPANTS + ").", "prizeScalings.participantsFrom");
			}
			
			// Teilnehmer bis
			if( prizeScaling.getParticipantsTo() == null ) {
				validationResult.addError( errorPrefix + "Für 'Teilnehmer bis' muss ein Wert angegeben werden.",
						"prizeScalings.participantsTo");
			} else if( prizeScaling.getParticipantsTo() > Participation.MAX_PARTICIPANTS ) {
					validationResult.addError( errorPrefix + "Der Wert für 'Teilnehmer bis' ist zu groß (max. "
						+ Participation.MAX_PARTICIPANTS + ").", "prizeScalings.participantsTo");
			}
			
			// bezahlte Plätze
			if( prizeScaling.getParticipantsInPrizes() == null ) {
				validationResult.addError( errorPrefix + "Für 'bezahlte Plätze' muss ein Wert angegeben werden.",
						"prizeScalings.participantsInPrizes");
			} else if( prizeScaling.getParticipantsInPrizes() < 1 ) {
				validationResult.addError( errorPrefix + "'bezahlte Plätze' muss größer als 0 sein.",
						"prizeScalings.participantsInPrizes" );
			} else if( prizeScaling.getParticipantsFrom() != null
					&& prizeScaling.getParticipantsInPrizes() > prizeScaling.getParticipantsFrom() ) {
				validationResult.addError( errorPrefix + "'bezahlte Plätze' darf nicht größer sein als 'Teilnehmer von'.",
						"prizeScalings.participantsInPrizes");
			}
			
			// Daten im Vergleich zum Vorgänge prüfen
			if( previous != null ) {
				
				if( prizeScaling.getParticipantsFrom() != null
						&& previous.getParticipantsTo() != null ) {
					int diff = prizeScaling.getParticipantsFrom() - previous.getParticipantsTo();
					
					if( diff != 1 ) {
						validationResult.addError( errorPrefix
								+ "'Teilnehmer von' muss genau um 1 größer sein als 'Teilnehmer bis' vom Vorgänger.",
								"prizeScalings.participantsFrom" );
					}
				}
			}
			
			previous = prizeScaling;
			
			// Prozentsätze prüfen
			if( prizeScaling.getPrizePercentages().isEmpty() ) {
				validationResult.addError( errorPrefix + "Es müssen Prozentsätze für die einzelnen Plätze angegeben weden.",
						"prizeScalings.prizePercentage");
			} else if( prizeScaling.getParticipantsInPrizes() != null
					&& prizeScaling.getPrizePercentages().size() != prizeScaling.getParticipantsInPrizes() ) {
				validationResult.addError( errorPrefix
						+ "'bezahlte Plätze' muss genau mit der Anzahl der Prozentsätze für die einzelnen Plätze übereinstimmen.",
						"prizeScalings.prizePercentage");
			}
			
			int percentage = 0;
			int previousPercentage = -1;
			
			long amount = 0l;
			long previousAmount = -1l;
			
			for( int j = 0; j < prizeScaling.getPrizePercentages().size(); j++ ) {
				
				PrizePercentage prizePercentage = prizeScaling.getPrizePercentages().get( j );
				
				if( prizePercentage.getPlace() == null ) {
					validationResult.addError( errorPrefix + "Für 'Platz' muss ein Wert angegeben weden.",
							"prizeScalings.prizePercentage.place" );
				} else if( prizePercentage.getPlace() != ( j + 1 ) ) {
					validationResult.addError( errorPrefix + "Der Wert für 'Platz' muss fortlaufend angegeben werden.",
							"prizeScalings.prizePercentage.place" );
				}
				
				if( prizeScalingType == PrizeScalingTypeEnum.PERCENTAGE ) {
					
					if( prizePercentage.getPercentage() == null ) {
						validationResult.addError( errorPrefix + "Für 'Preisgeld' muss ein Wert angegeben werden.",
								"prizeScalings.prizePercentage.percentage" );
					} else if( prizePercentage.getPercentage().equals( 0 ) ) {
						validationResult.addError( errorPrefix + "Der Prozentsatz für 'Preisgeld' muss größer als 0% sein.",
								"prizeScalings.prizePercentage.percentage" );
					} else {
						percentage += prizePercentage.getPercentage();
						
						if( previousPercentage != -1
								&& prizePercentage.getPercentage() > previousPercentage ) {
							validationResult.addError( errorPrefix + "Der Prozentsatz für 'Preisgeld' darf nicht steigen.",
									"prizeScalings.prizePercentage.percentage" );
						}
						
						previousPercentage = prizePercentage.getPercentage();
					}
				} else if( prizeScalingType == PrizeScalingTypeEnum.AMOUNT ) {
					
					if( prizePercentage.getAmount() == null ) {
						validationResult.addError( errorPrefix + "Für 'Preisgeld' muss ein Wert angegeben werden.",
								"prizeScalings.prizePercentage.amount" );
					} else if( prizePercentage.getAmount().equals( 0L ) ) {
						validationResult.addError( errorPrefix + "Der Betrag für 'Preisgeld' muss größer als 0,00 sein.",
								"prizeScalings.prizePercentage.amount" );
					} else {
						amount += prizePercentage.getAmount();
						
						if( previousAmount != -1
								&& prizePercentage.getAmount() > previousAmount ) {
							validationResult.addError( errorPrefix + "Der Betrag für 'Preisgeld' darf nicht steigen.",
									"prizeScalings.prizePercentage.amount" );
						}
						
						previousAmount = prizePercentage.getAmount();
					}
				}
			}
			
			if( percentage != 100000000 && prizeScalingType == PrizeScalingTypeEnum.PERCENTAGE ) {
				validationResult.addError( errorPrefix + "Die Summe für 'Preisgeld' muss genau 100% ergeben.",
						"prizeScalings.prizePercentage.percentage" );
			}
			
			if( prizeScalingType == PrizeScalingTypeEnum.AMOUNT
					&& tournament.getBuyinPrize() != null
					&& prizeScaling.getParticipantsFrom() != null
					&& prizeScaling.getParticipantsTo() != null ) {
				
				long amountMin = tournament.getBuyinPrize() * prizeScaling.getParticipantsFrom();
				long amountMax = tournament.getBuyinPrize() * prizeScaling.getParticipantsTo();
				
				if( amount < amountMin ) {
					validationResult.addError( errorPrefix + "Die Summe für 'Preisgeld' darf nicht kleiner sein als der Faktor aus 'Buy-in (Geweinnanteil)' und 'Teilnehmer von'.",
							"prizeScalings.prizePercentage.amount" );
				} else if( amount > amountMax ) {
					validationResult.addError( errorPrefix + "Die Summe für 'Preisgeld' darf nicht größer sein als der Faktor aus 'Buy-in (Geweinnanteil)' und 'Teilnehmer bis'.",
							"prizeScalings.prizePercentage.amount" );
				}
			}
			
		}
		
		return validationResult;
	}
	
}
