package karstenroethig.pokerstats.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class DateUtils {

	private static final DateFormat df = new SimpleDateFormat( "dd.MM.yyyy" );
	
	public static String fomatDate( Date date ) {
		
		if( date == null ) {
			return StringUtils.EMPTY;
		}
		
		return df.format( date );
	}
	
	public static Date createDate( int year, int month, int day ) {
		
		long ms = new GregorianCalendar( year, month, day ).getTimeInMillis();

        return new Date( ms );
	}
	
	public static int[] getDateValues( Date date ) {
		
		if( date == null ) {
			return new int[] {0, 0, 0};
		}
		
		Calendar cal = Calendar.getInstance( Locale.GERMANY );
        cal.setTime( date );

        int year = cal.get( Calendar.YEAR );
        int month = cal.get( Calendar.MONTH );
        int day = cal.get( Calendar.DAY_OF_MONTH );
        
        return new int[] { year, month, day };
	}
	
	public static String getWeekOfYear( Date date ) {
		
		if( date == null ) {
			return StringUtils.EMPTY;
		}
		
		Calendar calendar = Calendar.getInstance( Locale.GERMANY );
		calendar.setTime( date );
		
		int weekOfYear = calendar.get( Calendar.WEEK_OF_YEAR );
		int year = calendar.get( Calendar.YEAR );
		
		StringBuffer week = new StringBuffer();
		
		week.append( "KW" );
		
		if( weekOfYear < 10 ) {
			week.append( "0" );
		}
		
		week.append( weekOfYear );
		week.append( "/" );
		week.append( year );
		
		return week.toString();
	}
	
	public static String getMonthOfYear( Date date ) {
		
		if( date == null ) {
			return StringUtils.EMPTY;
		}
		
		Calendar calendar = Calendar.getInstance( Locale.GERMANY );
		calendar.setTime( date );
		
		int month = calendar.get( Calendar.MONTH );
		int year = calendar.get( Calendar.YEAR );
		
		StringBuffer ret = new StringBuffer();
		
		switch ( month ) {
		
			case Calendar.JANUARY:
				ret.append( "Jan" );
				break;
		
			case Calendar.FEBRUARY:
				ret.append( "Feb" );
				break;
		
			case Calendar.MARCH:
				ret.append( "Mär" );
				break;
		
			case Calendar.APRIL:
				ret.append( "Apr" );
				break;
		
			case Calendar.MAY:
				ret.append( "Mai" );
				break;
		
			case Calendar.JUNE:
				ret.append( "Jun" );
				break;
		
			case Calendar.JULY:
				ret.append( "Jul" );
				break;
		
			case Calendar.AUGUST:
				ret.append( "Aug" );
				break;
		
			case Calendar.SEPTEMBER:
				ret.append( "Sep" );
				break;
		
			case Calendar.OCTOBER:
				ret.append( "Okt" );
				break;
		
			case Calendar.NOVEMBER:
				ret.append( "Nov" );
				break;
		
			case Calendar.DECEMBER:
				ret.append( "Dez" );
				break;

			default:
				break;
		}
		
		ret.append( " " );
		ret.append( year );
		
		return ret.toString();
	}
	
	public static String getYear( Date date ) {
		
		if( date == null ) {
			return StringUtils.EMPTY;
		}
		
		Calendar calendar = Calendar.getInstance( Locale.GERMANY );
		calendar.setTime( date );
		
		int year = calendar.get( Calendar.YEAR );
		
		return year + StringUtils.EMPTY;
	}

    /**
     * Ermittelt die Datumswerte für jeden Tag zwischen dem angegebenen Start- und dem Enddatum.
     *
     * @param   start  Startdatum für die Ermittlung
     * @param   ende   Enddatum für die Ermittlung
     *
     * @return  Liste von Datumswerten für jeden Tag zwischen dem Start- und Enddatum.
     */
    public static Set<Date> findDatesBetweenStartAndEnd( Date start, Date ende ) {
        Set<Date> dates = new HashSet<Date>();

        if( start == null || ende == null ) {
        	return dates;
        }
        
        // Startwerte ermitteln
        Calendar cal = Calendar.getInstance( Locale.GERMANY );
        cal.setTime( start );

        int year = cal.get( Calendar.YEAR );
        int month = cal.get( Calendar.MONTH );
        int day = cal.get( Calendar.DAY_OF_MONTH );

        // Endwerte ermitteln
        cal = Calendar.getInstance( Locale.GERMANY );
        cal.setTime( ende );

        int yearEnde = cal.get( Calendar.YEAR );
        int monthEnde = cal.get( Calendar.MONTH );
        int dayEnde = cal.get( Calendar.DAY_OF_MONTH );

        // Alle Tage zwischen Start und Ende ermitteln
        for( ; year <= yearEnde; year++ ) {

            for( ; month <= Calendar.DECEMBER; month++ ) {

                if( ( month > monthEnde ) && ( year == yearEnde ) ) {
                    continue;
                }

                for( ; day <= findLastDayOfMonth( year, month ); day++ ) {

                    if( ( day > dayEnde ) && ( month == monthEnde ) && ( year == yearEnde ) ) {
                        continue;
                    }

                    dates.add( DateUtils.createDate( year, month, day ) );
                }

                day = 1;
            }

            month = Calendar.JANUARY;
        }

        return dates;
    }

    /**
     * Prüft, ob es sich bei dem angegebenen Datum um den letzten Tag des Monats handelt.
     *
     * @param   date  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean isLastDayOfMonth( Date date ) {
        Calendar cal = Calendar.getInstance( Locale.GERMANY );
        cal.setTime( date );

        int year = cal.get( Calendar.YEAR );
        int month = cal.get( Calendar.MONTH );
        int day = cal.get( Calendar.DAY_OF_MONTH );

        return day == findLastDayOfMonth( year, month );
    }

    /**
     * Liefert den letzten Tag des angegebenen Monats im angegebenen Jahr.
     *
     * @param   year   DOCUMENT ME!
     * @param   month  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static int findLastDayOfMonth( int year, int month ) {
        Calendar cal = Calendar.getInstance( Locale.GERMANY );
        cal.set( year, month, 1 );

        return cal.getActualMaximum( Calendar.DAY_OF_MONTH );
    }
}
