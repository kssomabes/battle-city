package battlecity;

import java.io.*;
import java.net.*;

public class Client implements Runnable {

	private static Socket clientSocket = null;
	private static PrintStream outputStream = null;
	private static DataInputStream inputStream = null;
	private static BufferedReader inputLine = null;
	private static boolean closed = false;

	public void run() {
		// stops listening for response when "Sayonara" is recieved from Server
		String responseLine;
		try {
			while ((responseLine = inputStream.readLine()) != null) {
				System.out.println(responseLine);
				if (responseLine == "*** Sayonara ***")
					break;
			}
			closed = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		try {
			String host = args[0];
			int portNumber = Integer.valueOf(args[1]).intValue();

			clientSocket = new Socket(host, portNumber);
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			outputStream = new PrintStream(clientSocket.getOutputStream());
			inputStream = new DataInputStream(clientSocket.getInputStream());
		} catch (UnknownHostException e) {
			System.err.println("Unknown host");
		} catch(IOException e){
			System.out.println("Cannot find (or disconnected from) Server");
		} catch(ArrayIndexOutOfBoundsException e){
			System.out.println("Usage: java Client <server ip> <port no.>");
		}

		if (clientSocket != null && outputStream != null && inputStream != null) {
			try {
				// creates thread that reads input from the server
				new Thread(new Client()).start();
				while (!closed) {
					outputStream.println(inputLine.readLine().trim());
				}
				// closing time
				outputStream.close();
				inputStream.close();
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}