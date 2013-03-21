package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;

import support.Vehicle;
import utils.VehicleImageUtils;

/**
 * The database class that holds database connection information and 
 * provides methods of interacting with the database
 * @author James Sampica
 */
public class JDBCDatabase {
	
	private static String url;
	private static String username;
	private static String password;
	
	private JDBCDatabase(){
	}
	
	/**
	 * Sets up an active connection to the database
	 * This method should be called prior to any method calls
	 * @param user the username of the database
	 * @param pass the password of the database
	 */
	public static void setupJDBCDatabase(String user, String pass){
		username = user;
		password = pass;
		
		url = "jdbc:mysql://localhost/cyprus?&user=" + username + "&password=" + password;
		
		try {   
			Class.forName ("com.mysql.jdbc.Driver");

			ObjectPool<Connection> connectionPool = new GenericObjectPool<Connection>(null);

			ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url,null);

			@SuppressWarnings("unused") //Actually is used by the pool
			PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);
			
			Class.forName("org.apache.commons.dbcp.PoolingDriver");
			PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");

			driver.registerPool("cyprusPool",connectionPool);

		}
		catch (Exception e) {
			//System.out.println ("Unable to load driver.");
			Logger.getLogger(JDBCDatabase.class.getName()).log(Level.SEVERE, "Unable to load driver.");
		}
	}
	
	/**
	 * Inserts a parking pass that expires after a date
	 * @param v the vehicle that the pass is intended for
	 * @param d the desired date of expiration
	 * @return true if successful, false otherwise
	 */
	public static boolean insertVehicleDatePass(Vehicle v, Date d){
		
		Connection connection = null;
		PreparedStatement stmt = null;
        ResultSet rset = null;
		
		try {
			connection = DriverManager.getConnection("jdbc:apache:commons:dbcp:cyprusPool");
			StringBuilder sb = new StringBuilder();

			Timestamp expireDate = new Timestamp(d.getTime());

			sb.append("replace into date_passes");
			sb.append("(platenumber, lotnumber, expirationdate) ");
			sb.append("values ( ");
			sb.append("?, ?, ?");
			sb.append(")");
			
			stmt = connection.prepareStatement(sb.toString());
			stmt.setString(1, v.getPlateNumber());
			stmt.setString(2, v.getLotNumber());
			stmt.setTimestamp(3, expireDate);
			
			stmt.executeUpdate();

		} catch (SQLException sqle){ 
			//System.out.println("An sql error occurred: " + sqle.getErrorCode());
			Logger.getLogger(JDBCDatabase.class.getName()).log(Level.WARNING, "An sql error occurred: " + sqle.getErrorCode());
			return false;
		} finally {
            try { if (rset != null) rset.close(); } catch(Exception e) {}
            try { if (stmt != null) stmt.close(); } catch(Exception e) {}
            try { if (connection != null) connection.close(); } catch(Exception e) {}
        }
		
		return true;
	}
	
	/**
	 * Inserts a parking pass based on time (in hours) for a vehicle
	 * @param v the vehicle the pass is intended for
	 * @param hours the amount of hours the pass is for
	 * @return true if successful, false otherwise
	 */
	public static boolean insertVehicleTimePass(Vehicle v, int hours){
		
		Connection connection = null;
		PreparedStatement stmt = null;
        ResultSet rset = null;
		
		try {
			connection = DriverManager.getConnection("jdbc:apache:commons:dbcp:cyprusPool");
			StringBuilder sb = new StringBuilder();

			sb.append("replace into time_passes");
			sb.append("(platenumber, lotnumber, hours) ");
			sb.append("values ( ");
			sb.append("?, ?, ?");
			sb.append(")");
			
			stmt = connection.prepareStatement(sb.toString());
			stmt.setString(1, v.getPlateNumber());
			stmt.setString(2, v.getLotNumber());
			stmt.setInt(3, hours);

			stmt.executeUpdate();

		} catch (SQLException sqle){ 
			//System.out.println("An sql error occurred: " + sqle.getErrorCode());
			Logger.getLogger(JDBCDatabase.class.getName()).log(Level.WARNING, "An sql error occurred: " + sqle.getErrorCode());
			return false;
		} finally {
            try { if (rset != null) rset.close(); } catch(Exception e) { }
            try { if (stmt != null) stmt.close(); } catch(Exception e) { }
            try { if (connection != null) connection.close(); } catch(Exception e) { }
        }
		
		return true;
	}
	
	/**
	 * A query to determine if a vehicle has a valid date pass
	 * @param v the vehicle to test
	 * @return the date object if the vehicle has a valid pass, null otherwise
	 */
	public static Date queryVehicleDatePass(Vehicle v){
		
		Connection connection = null;
		Statement stmt = null;
        ResultSet rset = null;
        Date expirationDate = null;
        
		try {
			connection = DriverManager.getConnection("jdbc:apache:commons:dbcp:cyprusPool");
			stmt = connection.createStatement();
			StringBuilder sb = new StringBuilder();

			sb.append("select * from date_passes ");
			sb.append("where platenumber='"+v.getPlateNumber()+"' ");
			sb.append("and lotnumber='"+v.getLotNumber()+"'");
			
			rset = stmt.executeQuery(sb.toString());
			
			if(rset.next()){
				expirationDate = rset.getTimestamp("expirationdate");
			}
			
			
		} catch (SQLException sqle){ 
			//System.out.println("An sql error occurred: " + sqle.getErrorCode());
			Logger.getLogger(JDBCDatabase.class.getName()).log(Level.WARNING, "An sql error occurred: " + sqle.getErrorCode());
		} finally {
            try { if (rset != null) rset.close(); } catch(Exception e) { }
            try { if (stmt != null) stmt.close(); } catch(Exception e) { }
            try { if (connection != null) connection.close(); } catch(Exception e) { }
        }
		
		return expirationDate;
	}
	
	/**
	 * Makes a query to determine if a vehicle has a valid time pass
	 * @param v the vehicle to test
	 * @return the amount of time (in hours) of the time pass, 0 otherwise
	 */
	public static int queryVehicleTimePass(Vehicle v){
		
		Connection connection = null;
		Statement stmt = null;
        ResultSet rset = null;
        int hours = 0;
        
		try {
			connection = DriverManager.getConnection("jdbc:apache:commons:dbcp:cyprusPool");
			stmt = connection.createStatement();
			StringBuilder sb = new StringBuilder();

			sb.append("select * from time_passes ");
			sb.append("where platenumber='"+v.getPlateNumber()+"' ");
			sb.append("and lotnumber='" +v.getLotNumber()+"'");
			
			rset = stmt.executeQuery(sb.toString());
			
			if(rset.next()){
				hours = rset.getInt("hours");
			}

		} catch (SQLException sqle){ 
			//System.out.println("An sql error occurred: " + sqle.getErrorCode());
			Logger.getLogger(JDBCDatabase.class.getName()).log(Level.WARNING, "An sql error occurred: " + sqle.getErrorCode());
		} finally {
            try { if (rset != null) rset.close(); } catch(Exception e) { }
            try { if (stmt != null) stmt.close(); } catch(Exception e) { }
            try { if (connection != null) connection.close(); } catch(Exception e) { }
        }
		
		return hours;
	}
	
	/**
	 * Stores a vehicles plate/lot and violation date information
	 * @param vehicle the vehicle to store
	 */
	public static void storeParkingViolation(Vehicle vehicle){
		
		Connection connection = null;
		PreparedStatement stmt = null;
		
		try {
			connection = DriverManager.getConnection("jdbc:apache:commons:dbcp:cyprusPool");
			StringBuilder sb = new StringBuilder();

			Timestamp violationDate = new Timestamp(vehicle.getGraceEndDate().getTime());
			
			sb.append("insert into vehicle_violations");
			sb.append("(platenumber, lotnumber, violationdate) ");
			sb.append("values ( ");
			sb.append("?, ?, ?");
			sb.append(")");
			
			stmt = connection.prepareStatement(sb.toString());
			stmt.setString(1, vehicle.getPlateNumber());
			stmt.setString(2, vehicle.getLotNumber());
			stmt.setTimestamp(3, violationDate);

			
			stmt.executeUpdate();
			
			VehicleImageUtils.saveImageBytesForVehicle(vehicle);
			

		} catch (SQLException sqle){ 
			//System.out.println("An sql error occurred: " + sqle.getErrorCode());
			Logger.getLogger(JDBCDatabase.class.getName()).log(Level.WARNING, "An sql error occurred: " + sqle.getErrorCode());
		} finally {
            try { if (stmt != null) stmt.close(); } catch(Exception e) { }
            try { if (connection != null) connection.close(); } catch(Exception e) { }
        }

	}
	
	/**
	 * A search query on vehicle violations. Checks all columns against a key to determine likeness
	 * @param key the search key
	 * @return a set of vehicles that have information like the key
	 */
	public static ArrayList<Vehicle> queryVehicleViolations(String key) {
		
		ArrayList<Vehicle> queryResultList = new ArrayList<Vehicle>();
		
		Connection connection = null;
		Statement stmt = null;
        ResultSet rset = null;
		
		try {
			
			connection = DriverManager.getConnection("jdbc:apache:commons:dbcp:cyprusPool");
			stmt = connection.createStatement();
			StringBuilder sb = new StringBuilder();
			
			sb.append("select * from vehicle_violations where platenumber like ");
			sb.append("'%" + key + "%'");		
			sb.append(" or lotnumber like ");
			sb.append("'%" + key + "%'");
			sb.append("or violationdate like ");
			sb.append("'%" + key + "%'");
			
			rset = stmt.executeQuery(sb.toString());
			
			while (rset.next()){
				
				Vehicle v = new Vehicle();
				Timestamp violationDate = rset.getTimestamp("violationdate");
				if(violationDate != null){
					v.setGraceEndDate(new Date(violationDate.getTime()));
				}
				v.setLotNumber(rset.getString("lotnumber"));
				v.setPlateNumber(rset.getString("platenumber"));
				
				VehicleImageUtils.getImageFileBytesForVehicle(v);
				
				queryResultList.add(v);
			
			}

		} catch (SQLException sqle){ 
			//System.out.println("An sql error occurred " + sqle.getErrorCode());
			Logger.getLogger(JDBCDatabase.class.getName()).log(Level.WARNING, "An sql error occurred " + sqle.getErrorCode());
		} finally {
            try { if (rset != null) rset.close(); } catch(Exception e) { }
            try { if (stmt != null) stmt.close(); } catch(Exception e) { }
            try { if (connection != null) connection.close(); } catch(Exception e) { }
        }
		
		return queryResultList;  
	}
	
}
