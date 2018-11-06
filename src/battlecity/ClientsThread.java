package battlecity;

/**
 * Contains all clients and allows the server send all incoming messages to every client
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;

class ClientsThread extends Thread {

	private DataInputStream inputStream = null;
	private PrintStream outputStream = null;
	private Socket clientSocket = null;
	private final ArrayList<ClientsThread> clientThread;
	private int MAX_NUMBER_OF_CLIENTS;

	public ClientsThread(Socket clientSocket, ArrayList<ClientsThread> clientThread) {
		this.clientSocket = clientSocket;
		this.clientThread = clientThread;
		MAX_NUMBER_OF_CLIENTS = clientThread.size();
	}

	public void run() {
		int MAX_NUMBER_OF_CLIENTS = this.MAX_NUMBER_OF_CLIENTS;
		ArrayList<ClientsThread> clientThread = this.clientThread;
		String name;

		try {
			inputStream = new DataInputStream(clientSocket.getInputStream());
			outputStream = new PrintStream(clientSocket.getOutputStream());
			outputStream.println("Enter your name:");
			name = inputStream.readLine().trim();

			// welcome the new client
			outputStream.println("Welcome " + name + "!");
			synchronized (this) {
				for (int i = 0; i < MAX_NUMBER_OF_CLIENTS; i++) {
					if (clientThread.get(i) != null && clientThread.get(i) != this) {
						clientThread.get(i).outputStream.println("*** " + name + " joined ***");
					}
				}
			}
			// start the conversation
			while (true) {
				String line = inputStream.readLine();
				if (line.startsWith("/quit")) {
					break;
				}
				// sends message to all clients.
				synchronized (this) {
					for (int i = 0; i < MAX_NUMBER_OF_CLIENTS; i++) {
						if (clientThread.get(i) != null) {
							clientThread.get(i).outputStream.println(name + " says: " + line);
						}
					}
				}
			}
			synchronized (this) {
				for (int i = 0; i < MAX_NUMBER_OF_CLIENTS; i++) {
					if (clientThread.get(i) != null && clientThread.get(i) != this) {
						clientThread.get(i).outputStream.println("*** " + name + " left ***");
					}
				}
			}
			outputStream.println("*** Sayonara ***");

			// sets the current thread to null so server can accept a new client
			synchronized (this) {
				for (int i = 0; i < MAX_NUMBER_OF_CLIENTS; i++) {
					if (clientThread.get(i) == this) {
						clientThread.set(i,null);
					}
				}
			}
			// closing time
			inputStream.close();
			outputStream.close();
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}