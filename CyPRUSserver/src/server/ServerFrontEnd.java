package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerFrontEnd {
	public static void main(String args[]){
		ServerProvider server = new ServerProvider(queryPort());
		server.run();
	}
	public static int queryPort()
	{
		try {
			BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Enter port to start CyPRUS Server: ");
			return Integer.parseInt(bufferRead.readLine());
			
		} catch (IOException e)
		{
			System.out.println("IOException caught");
			return -1;
		}
		catch (NumberFormatException e)
		{
			System.out.println("NumberFormatException caught");
			return -1;
		}
	}
	public static String queryPword(){
		try {
			BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Enter database username (if applicable): ");
			return bufferRead.readLine();

		} catch (IOException e)
		{
			System.out.println("IOException caught");
			return "";
		}
	}
	public static String queryUsername(){
		try {
			BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Enter database password (if applicable): ");
			return bufferRead.readLine();

		} catch (IOException e)
		{
			System.out.println("IOException caught");
			return "";
		}
	}
	public static void info(String message){
		System.out.println(message);
	}
}
