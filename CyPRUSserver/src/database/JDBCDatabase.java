package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import support.Vehicle;
import utils.VehicleImageUtils;

/**
 * @author James Sampica
 */
public class JDBCDatabase {
	
	private String url;
	private String username;
	private String password;
	
	private static JDBCDatabase jdbcDatabase;
	
	public JDBCDatabase(){
		if(jdbcDatabase != null){
			throw new UnsupportedOperationException("This is a singleton class");
		}
	}
	
	public static void setupJDBCDatabase(String user, String password){
		JDBCDatabase db = getDatabase();
		db.setUsername(user);
		db.setPassword(password);
		
		db.setURL("jdbc:mysql://localhost/cyprus?&user=" + db.getUsername() + "&password=" + db.getPassword());
		
		try {   
			Class.forName ("com.mysql.jdbc.Driver");
		} 
		catch (Exception e) {
			System.out.println ("Unable to load driver.");
			//e.printStackTrace();
		}
	}
	
	public void storeParkingViolation(Vehicle vehicle){
		try {
			Connection connection = DriverManager.getConnection(url);
			StringBuilder sb = new StringBuilder();

			Timestamp violationDate = new Timestamp(vehicle.getGraceEndDate());

			sb.append("insert into vehicle_violations");
			sb.append("(platenumber, lotnumber, violationdate) ");
			sb.append("values ( ");
			sb.append("?, ?, ?");
			sb.append(")");
			
			PreparedStatement stmt = connection.prepareStatement(sb.toString());
			stmt.setString(1, vehicle.getPlateNumber());
			stmt.setString(2, vehicle.getLotNumber());
			stmt.setTimestamp(3, violationDate);

			
			stmt.executeUpdate();

		} catch (SQLException sqle){ 
			System.out.println("An sql error occurred: " + sqle.getErrorCode());
		}

	}
	
	public ArrayList<Vehicle> queryVehicleViolations(String key) {
		
		ArrayList<Vehicle> queryResultList = new ArrayList<Vehicle>();
		
		try {
			
			Connection connection = DriverManager.getConnection(url);
			Statement stmt = connection.createStatement();
			StringBuilder sb = new StringBuilder();
			
			sb.append("select * from vehicle_violations where platenumber or lotnumber or violationdate like ");
			sb.append("'%" + key + "%'");
			
			ResultSet  rs = stmt.executeQuery(sb.toString());
			
			while (rs.next()){
				
				Vehicle v = new Vehicle();
				Timestamp violationDate = rs.getTimestamp("violationdate");
				if(violationDate != null){
					v.setEntryDate(violationDate.getTime());
				}
				v.setGraceEndDate(0);
				v.setLotNumber(rs.getString("lotnumber"));
				v.setPlateNumber(rs.getString("platenumber"));
				
				VehicleImageUtils.getImageFileBytesForVehicle(v);
				
				queryResultList.add(v);
			}
			
			stmt.close();
			connection.close();
			
		} catch (SQLException sqle){ 
			System.out.println("An sql error occurred " + sqle.getErrorCode());
		}
		
		return queryResultList;  
	}
	
	public String getUsername(){
		return username;
	}
	
	public void setUsername(String username){
		this.username = username;
	}
	
	public String getPassword(){
		return password;
	}
	
	public void setPassword(String password){
		this.password = password;
	}
	
	public String getURL(){
		return url;
	}
	
	public void setURL(String url){
		this.url = url;
	}
	
	public static JDBCDatabase getDatabase(){
		if(jdbcDatabase == null){
			jdbcDatabase = new JDBCDatabase();
		}
		return jdbcDatabase;
	}
}
