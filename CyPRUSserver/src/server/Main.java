package server;

import java.io.Console;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
	    
	    Scanner s = new Scanner(System.in);
	    final Server server = createServerFromInputs(s);
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				server.start();
			};
		};
		new Thread(r).start();

		
		boolean serverRunning = true;
		
		while (serverRunning) {
			System.out.println( " -- Options -- " );
			System.out.println("1 - Exit");
			int opcao = s.nextInt();
			switch (opcao) {
			case 1:
				server.setConnected(false);
				serverRunning = false;
				break;
			default:
				System.out.println("Command doesn't exist...");
			}
		}
		
		s.close();
	}
	private static Server createServerFromInputs(Scanner s){
		
		int port = 0;
		boolean notValid = true;
		while(notValid){
			System.out.print("What port would you like to listen on? ");
			try{
				String temp = s.next();
				port = Integer.valueOf(temp);
				notValid = false;
			}
			catch(NumberFormatException e){
				
			}
		}
		
		System.out.print("What is your database username? ");
		String userName = s.next();
		
		System.out.print("What is your database password? ");
		String pWord = s.next();
		
		return new Server(port, userName, pWord);
	}
}
