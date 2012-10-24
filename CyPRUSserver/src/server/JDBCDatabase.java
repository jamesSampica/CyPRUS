package server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author James Sampica
 */
public class JDBCDatabase {
	private String url;
	
	public JDBCDatabase(String user, String password){
		url = "jdbc:mysql://localhost/mysql?&user=" + user + "&password=" + password;
		
		try {   
			Class.forName ("com.mysql.jdbc.Driver");
		} 
		catch (Exception e) {
			System.out.println ("Unable to load driver.");
			//e.printStackTrace();
		}
	}
	public String queryDatabase(String query)  throws ClassNotFoundException,IOException {
		try {
			
			Connection connection = DriverManager.getConnection(url);
			Statement stmt = connection.createStatement();
			ResultSet  rs = stmt.executeQuery(query);
			StringBuilder sb = new StringBuilder();
			
			while (rs.next()){
				sb.append(rs.getString( "id") +"\t"+
						rs.getString( "name") + "\t"+
						rs.getString("homezip") + "\t"+
						rs.getInt("salary") + "\t" +
						rs.getString("dept") + "\n");

			}
			
			stmt.close();
			connection.close();
			return sb.toString();
			
		} catch (SQLException sqle){ 
			System.out.println("An sql error occurred " + sqle.getErrorCode());
		}
		return "";  
	}
}
