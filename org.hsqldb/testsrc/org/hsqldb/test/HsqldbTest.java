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
		 * Damit der Test funktioniert, müssen vorher folgende Aktionen
		 * ausgeführt werden:
		 * - HSQL Database Manager starten (runDatabaseManager.bat)
		 * - Connect
		 *     - Recent Settings: HSQL Database Engine
		 *     - Setting Name: HSQL Database Engine (Name beliebig wählbar)
		 *     - Type: HSQL Database Engine Standalone
		 *     - Driver: org.hsqldb.jdbcDriver
		 *     - jdbc:hsqldb:file:db/testDB (frei wählbar ab file:)
		 *     - User: SA (frei wählbar)
		 *     - Passwort: (frei wählbar)
		 * - im Menü "Command" auf die Option "Test Script"
		 *     - Ausführen mit "Execute SQL"
		 * - im Menü "Options" auf die Option "Insert test data"
		 * - im Menü "Command" auf die Option "SHUTDOWN"
		 *     - Ausführen mit "Execute SQL"
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
