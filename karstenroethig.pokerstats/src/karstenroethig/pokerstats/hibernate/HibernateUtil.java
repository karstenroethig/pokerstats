package karstenroethig.pokerstats.hibernate;

import java.net.URL;

import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

	private static final SessionFactory sessionFactory;
	
	static {
		
		// Create the SessionFactory from hibernate.cfg.xml
		URL url = HibernateUtil.class.getClassLoader().getResource( "hibernate.cfg.xml" );
		
		sessionFactory = new Configuration()
			.configure( url ).buildSessionFactory();
	}
	
	public static Session openSession() throws HibernateException {
		
		Session session = sessionFactory.openSession();
		
		session.setFlushMode( FlushMode.COMMIT );
		
		return session;
	}
	
	public static void closeSessionFactory() {
		
		try {
			sessionFactory.close();
		}catch( HibernateException ex ) {
			// Nothing to do
		}
	}
	
	public static void closeQuietly( Session session ) {

		if( session != null && session.isOpen() ) {
			
			try{
				session.close();
			}catch(HibernateException ex) {
				// Nothing to do
			}
		}
	}
}
