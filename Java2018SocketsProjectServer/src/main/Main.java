/*
 * Java Assessment Sockets
 * by: Valdar Rudman
 * Student ID: R00081134
 */

package main;

import socketConnection.SocketServer;

/**
 * Launch server
 * @author valdar
 *
 */
public class Main {
	
	private final static String PATH = "sockets\\server";
	
	public static void main(String[] args) {
		
		// Starting our server
		SocketServer s = new SocketServer(PATH);
		s.runServer();
		
	}

}
