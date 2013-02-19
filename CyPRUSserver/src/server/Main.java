package server;

import java.io.Console;
import java.util.Scanner;

public class Main {
    private static Console console;

	public static void main(String[] args) {
    	//console = System.console();
	    if (console == null) {
	        System.out.println("Couldn't get Console instance");
	        //System.exit(0);
	    }
	    
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
		System.out.print("What port would you like to listen on? ");
		int port = s.nextInt();
		
		System.out.print("What is your database username? ");
		String userName = s.next();
		
		System.out.print("What is your database password? ");
		String pWord = s.next();
		
		return new Server(port, userName, pWord);
	}
}
