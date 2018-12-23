package socketConnection;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;
import monitor.FileShare;
import monitor.Monitor;

public class SocketServer extends Observable {

	//port number for server
	private final int PORT_NUMBER = 5555;
	//servers socket
	private ServerSocket serverSocket = null;
	
	//Max number of clients able to connect to server and array of clients
	private static final int maxClientsCount = 2;
	private static final ServerRunnable[] clients = new ServerRunnable[maxClientsCount];
	
	private FileShare monitor;
	
	/*
	 * Create a server socket that takes the path of the server folder
	 */
	public SocketServer(String path) {
		
		File f = new File(path);
		
		if(!f.exists())
			f.mkdirs();
		
		monitor = new Monitor(path);
		
		new Thread((Runnable) monitor).start();
		
	}
	
	/*
	 * Start server
	 */
	public void runServer() {
		
		try {
			
			//start server
			serverSocket = new ServerSocket(PORT_NUMBER);
			
			System.out.println("Server Running on Port: " + PORT_NUMBER);
			
		}
		catch(IOException e) {
			
			System.out.println(e.getMessage());
			
		}
		
		
		/*
		 * wait for a client to wait to connect. When client wants to connect starts a serverRunnable for client. Adds it the observer list
		 */
		while(true) {
			
			try {

				Socket clientSocket = serverSocket.accept();
				
				int i = 0;
		        for (i = 0; i < maxClientsCount; i++) {
		        	
		          if (clients[i] == null) {
		        	  
		        	  ServerRunnable sr = new ServerRunnable(clientSocket, this.monitor, clients);
		        	  
		        	  ((Observable) monitor).addObserver(sr);
		        	  
		        	  (clients[i] = sr).start();
		            
		        	  break;
		            
		          }
		        }
		        
		        if (i == maxClientsCount) {
		        
		        	System.out.println("Client tried to connect -- Server full");
		        	
		        	clientSocket.close();
		          
		        }
				
			}
			catch(IOException e) {
				
				System.out.println(e.getMessage());
				
			}
			
		}
		
	}

}
