/*
 * Java Assessment Sockets
 * by: Valdar Rudman
 * Student ID: R00081134
 */

package socketConnection;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Observable;
import java.util.Observer;

import monitor.FileShare;
import monitor.Monitor;

/*
 * Thread that runs on the server servicing a client that has connected
 */
public class ServerRunnable extends Thread implements Observer {

	// client that has connected
	private Socket clientSocket = null;
	
	private SocketAddress address;
	
	//read in information from client
	private ObjectInputStream in;
	
	//write out information to client
	private ObjectOutputStream out;
	
	//Monitor used to monitor server folder
	private FileShare monitor;
	
	private Thread[] clients;
	private int maxClientCounts;
	
	/*
	 * Start client server thread and save client and monitor in server
	 */
	public ServerRunnable(Socket clientSocket, FileShare monitor, Thread[] clients) {
		
		this.clientSocket = clientSocket;
		this.address = clientSocket.getRemoteSocketAddress();
		this.monitor = monitor;
	
		this.clients = clients;
		this.maxClientCounts = clients.length;
	}
	
	/*
	 * When thread starts, creates connections and starts listening for anything from client 
	 */
	@Override
	public void run() {
		
		int maxClientsCount = this.maxClientCounts;
		Thread[] clients = this.clients;
	
		try {
			
			
			
			System.out.println("New Client connected:\n\t" + address);
			
			// setting up connections with client
			in = new ObjectInputStream(this.clientSocket.getInputStream());
			out = new ObjectOutputStream(this.clientSocket.getOutputStream());
			
			// write to client. What is in our server folder
			Object[] list = {"list", this.monitor.getFiles()};
			out.writeObject(list);
			
		}
		catch(IOException e) {
			
			e.printStackTrace();
			
		}
		
		/*
		 * while loop that listens to see if client sends anything
		 */
		while(true) {
			
			try {
				
				Object[] readIn = (Object[]) in.readObject();
				
				//if client sends leave, send bye and  close connections and exit
				if(readIn[0].equals("leave")) {
					
					Object[] send = {"bye"};
					
					out.writeObject(send);
					out.close();
					in.close();
					clientSocket.close();
					
					synchronized (this) {
						
				        for (int i = 0; i < maxClientsCount; i++) {
				        	
				          if (clients[i] == this) { 
				        	  
				            clients[i] = null;
				            
				          }
				        }
				      }
					
					System.out.println("client Disconnected:\n\t" + address);
					
					return;
					
				}
				/*
				 *  if client asks to download file
				 */
				else if(readIn[0].equals("download")) {
					
					//open file
					this.monitor.openFile(((Monitor) monitor).getFolderPath() + "\\" + (String) readIn[1]);
					
					//get bytes of file
					byte[] bytes = this.monitor.getFileBytes();
					
					//prepare array to send to client
					Object[] media = {"media", (String)readIn[1], bytes};
					
					//close file
					this.monitor.closeFile();
					
					//send information to client-
					out.writeObject(media);
					
					System.out.println("File downloaded by client:\n\t" + address);
					
				}
				/*
				 * client want to upload
				 */
				else if(readIn[0].equals("upload")) {
					
					//synchronize so only one upload at a time
					synchronized (this) {
						
						monitor.upload((String)readIn[1], (byte[])readIn[2]);
						
						System.out.println("File uploaded by client:\n\t" + address);
						
					}
					
				}
				
			} catch (IOException | ClassNotFoundException e) {

				try {
					
					out.close();
					in.close();
					clientSocket.close();
					
					System.out.println("client Disconnected:\n\t" + address);
					
					synchronized (this) {
						
				        for (int i = 0; i < maxClientsCount; i++) {
				        	
				          if (clients[i] == this) { 
				        	  
				            clients[i] = null;
				            
				          }
				        }
				      }
					
					return;
					
				} catch (IOException e1) {
				
					e1.printStackTrace();
				}
				
			}

		}
		
	}

	/*
	 * Updates observers/ clients 
	 */
	@Override
	public void update(Observable o, Object arg) {
		
		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
		
					Object[] send = {"list", (File[])arg};
					
					out.writeObject(send);
					
					System.out.println("Client updated:\n\t" + address);

				}
				catch(IOException e) {
					
					System.out.println(e.getMessage());
					
				}
			}
			
		}).start();
		
	}

}
