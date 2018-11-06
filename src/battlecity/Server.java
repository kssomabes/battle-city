package battlecity;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;

public class Server {

	private static ServerSocket serverSocket = null;
	private static Socket clientSocket = null;

	// server can accept up to MAX_NUMBER_OF_CLIENTS connections.
	private static final int MAX_NUMBER_OF_CLIENTS = 10;
	private static final ArrayList<ClientsThread> clientThread = new ArrayList<ClientsThread>(
			Collections.nCopies(MAX_NUMBER_OF_CLIENTS,null));

	public static void main(String args[]) {

		// opens a server socket on a port (must be greater than 1023)
		try {
			int port = Integer.valueOf(args[0]).intValue();
			serverSocket = new ServerSocket(port);
			System.out.println("Server listening at port:" + port);
		} catch(IOException e){
            System.out.println("Usage: java Server <port no.>\n"+
					"Make sure to use valid ports (greater than 1023)");
			System.exit(0);
        } catch(ArrayIndexOutOfBoundsException e){
            System.out.println("Usage: java Server <port no.>\n"+
					"Insufficient arguments given.");
			System.exit(0);
        }

		// creates a client socket for each connection and pass to clientThread
		while (true) {
			try {
				clientSocket = serverSocket.accept();
				int i = 0;
				for (i = 0; i < MAX_NUMBER_OF_CLIENTS; i++) {
					if (clientThread.get(i) == null) {
						clientThread.set(i,new ClientsThread(clientSocket, clientThread));
						clientThread.get(i).start();
						break;
					}
				}
				if (i == MAX_NUMBER_OF_CLIENTS) {
					PrintStream outputStream = new PrintStream(clientSocket.getOutputStream());
					outputStream.println("Server too busy. Try later.");
					outputStream.close();
					clientSocket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}