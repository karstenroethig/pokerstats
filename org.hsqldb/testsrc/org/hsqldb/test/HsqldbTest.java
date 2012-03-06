package org.hsqldb.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

public class HsqldbTest extends TestCase {

	public void testDatabase() throws Exception {
		
		/*
		 * Damit der Test funktioniert, m�ssen vorher folgende Aktionen
		 * ausgef�hrt werden:
		 * - HSQL Database Manager starten (runDatabaseManager.bat)
		 * - Connect
		 *     - Recent Settings: HSQL Database Engine
		 *     - Setting Name: HSQL Database Engine (Name beliebig w�hlbar)
		 *     - Type: HSQL Database Engine Standalone
		 *     - Driver: org.hsqldb.jdbcDriver
		 *     - jdbc:hsqldb:file:db/testDB (frei w�hlbar ab file:)
		 *     - User: SA (frei w�hlbar)
		 *     - Passwort: (frei w�hlbar)
		 * - im Men� "Command" auf die Option "Test Script"
		 *     - Ausf�hren mit "Execute SQL"
		 * - im Men� "Options" auf die Option "Insert test data"
		 * - im Men� "Command" auf die Option "SHUTDOWN"
		 *     - Ausf�hren mit "Execute SQL"
		 */
		
		try{
			Class.forName( "org.hsqldb.jdbcDriver" );
		}catch( ClassNotFoundException ex ) {
			fail( "Treiberklasse nicht gefunden!" );
		}
		
		Connection conn = null;
		
		try{
			conn = DriverManager.getConnection( "jdbc:hsqldb:file:db/testDB;shutdown=true", "SA", "" );
			
			Statement stm = conn.createStatement();
			
			String sql = "select * from Customer";
			ResultSet rs = stm.executeQuery( sql );
			
			while( rs.next() ) {
				
				String id = rs.getString( 1 );
				String firstName = rs.getString( 2 );
				String lastName = rs.getString( 3 );
				
				System.out.println( id + ", " + firstName + " " + lastName );
			}
			
			rs.close();
			stm.close();
		}catch( SQLException ex ) {
			ex.printStackTrace();
			
			fail( ex.getMessage() );
		}finally{
			if( conn != null ) {
				try{
					conn.close();
				}catch( Exception ex ) {
					ex.printStackTrace();
					
					fail( ex.getMessage() );
				}
			}
		}
	}
}
