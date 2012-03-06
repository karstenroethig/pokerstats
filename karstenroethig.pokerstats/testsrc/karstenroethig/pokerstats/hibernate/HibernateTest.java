package karstenroethig.pokerstats.hibernate;

import java.util.List;

import junit.framework.TestCase;
import karstenroethig.pokerstats.model.Participation;
import karstenroethig.pokerstats.model.Tournament;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateTest extends TestCase {

	private SessionFactory sessionFactory;

	@Override
	protected void setUp() throws Exception {
		
		// A SessionFactory is set up once for an application
        sessionFactory = new Configuration()
                .configure() // configures settings from hibernate.cfg.xml
                .buildSessionFactory();
	}

	@Override
	protected void tearDown() throws Exception {
		
		if ( sessionFactory != null ) {
			sessionFactory.close();
		}
	}

	@SuppressWarnings({ "unchecked" })
	public void testBasicUsage() {
		
		Tournament tournament = new Tournament();
//		tournament.setDatum( "Mo., 08.08.2011 19:30" );
//		tournament.setBezeichnung( "The Big $162 [$50K Guaranteed]" );
		
		Tournament tournament2 = new Tournament();
//		tournament2.setDatum( "Di., 09.08.2011 20:00" );
//		tournament2.setBezeichnung( "$10.50 NL Hold'em [Knockout, Heads-Up, Turbo]" );
		
		Tournament tournament3 = new Tournament();
//		tournament3.setDatum( "Mi., 10.08.2011 21:30" );
//		tournament3.setBezeichnung( "$11 +R NL Hold'em [$70K Guaranteed]" );
		
		Participation participation = new Participation();
//		participation.setDatum( "Mo., 08.08.2011 19:30" );
//		participation.setBezeichnung( "The Big $162 [$50K Guaranteed]" );
//		participation.setPlatzierung( 1 );
//		participation.setTeilnehmer( 900 );
		
		Participation participation2 = new Participation();
//		participation2.setDatum( "Di., 09.08.2011 19:30" );
//		participation2.setBezeichnung( "The Big $162 [$50K Guaranteed]" );
//		participation2.setPlatzierung( 13 );
//		participation2.setTeilnehmer( 900 );
		
		Participation participation3 = new Participation();
//		participation3.setDatum( "Di., 09.08.2011 20:00" );
//		participation3.setBezeichnung( "$10.50 NL Hold'em [Knockout, Heads-Up, Turbo]" );
//		participation3.setPlatzierung( 1234 );
//		participation3.setTeilnehmer( 3000 );
		
		Participation participation4 = new Participation();
//		participation4.setDatum( "Mi., 10.08.2011 21:30" );
//		participation4.setBezeichnung( "$11 +R NL Hold'em [$70K Guaranteed]" );
//		participation4.setPlatzierung( 256 );
//		participation4.setTeilnehmer( 500 );
		
		// create a couple of events...
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.save( tournament );
		session.save( tournament2 );
		session.save( tournament3 );
		session.save( participation );
		session.save( participation2 );
		session.save( participation3 );
		session.save( participation4 );
		session.getTransaction().commit();
		session.close();

		// now lets pull events from the database and list them
		session = sessionFactory.openSession();
//        session.beginTransaction();
        @SuppressWarnings("rawtypes")
		List result = session.createQuery( "from Tournament" ).list();
		for ( Tournament tour : (List<Tournament>) result ) {
			System.out.println( "Tournament: " + tour.getDesciption() );
		}
//        session.getTransaction().commit();
        session.close();
	}
}
