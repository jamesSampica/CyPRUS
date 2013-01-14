package database;

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
		
		db.setURL("jdbc:mysql://localhost/mysql?&user=" + db.getUsername() + "&password=" + db.getPassword());
		
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
