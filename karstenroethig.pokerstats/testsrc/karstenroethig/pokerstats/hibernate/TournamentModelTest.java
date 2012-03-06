package karstenroethig.pokerstats.hibernate;

import java.util.List;

import org.hibernate.Session;

import junit.framework.TestCase;
import karstenroethig.pokerstats.model.Tournament;
import karstenroethig.pokerstats.model.TournamentModel;

public class TournamentModelTest extends TestCase {

	@SuppressWarnings("unchecked")
	public void testSave() throws Exception {
		
		TournamentModel model = TournamentModel.getInstance();
		
		Tournament tournament = model.createNewTournament();
		
		tournament.setDesciption( "The Big $162 [$50K Guaranteed]" );
		tournament.setBuyinPrize( 10000L );
		tournament.setBuyinFee( 900L );
		
		model.save( tournament );
		
		Session session = HibernateUtil.openSession();
		List<Tournament> result = session.createQuery( "from Tournament" ).list();
		
		Tournament t2 = null;
		
		for ( Tournament tour : result ) {
			System.out.println( "Tournament: " + tour.getDesciption() + " -> " + tour.getBuyinFee() );
			t2 = tour;
		}
		
        session.close();
        
        t2.setBuyinFee( 1000L );
        
        model.save( t2 );
        
        session = HibernateUtil.openSession();
		result = session.createQuery( "from Tournament" ).list();
		
		for ( Tournament tour : (List<Tournament>) result ) {
			System.out.println( "Tournament: " + tour.getDesciption() + " -> " + tour.getBuyinFee() );
		}
		
        session.close();
        
	}
}
