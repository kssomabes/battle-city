package battlecity;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import proto.PlayerProtos.Player;
import proto.PlayerProtos.*;
import proto.TcpPacketProtos.*;
import proto.TcpPacketProtos.TcpPacket.PacketType;
import proto.TcpPacketProtos.TcpPacket.*;
import java.nio.ByteBuffer;

public class Client implements Runnable {

	private static Socket clientSocket = null;
	private static OutputStream outputStream = null;
	private static DataInputStream inputStream = null;
	private static BufferedReader inputLine = null;
	private static boolean closed = false;

	public void printPlayer(Player p){
		System.out.println("Name: " + p.getName() + " [" + p.getId() +"]");
	}
	
	public void run() {
//		Server output after connecting to Lobby Chat
		try {
			int msgLen =0;
			byte[] msg = new byte[0];
			while (true) {
				msgLen =0;
				msgLen = inputStream.available();
				msg = new byte[msgLen];
				inputStream.readFully(msg);
								
				if (msgLen != 0) {
					TcpPacket received = TcpPacket.parseFrom(msg);
					if (received.getType() == PacketType.CHAT){
						ChatPacket chatreceived = TcpPacket.ChatPacket.parseFrom(msg);
						System.out.println(chatreceived.getPlayer().getName() + ": " + chatreceived.getMessage());
					}else if(received.getType() == PacketType.CONNECT){
						ConnectPacket newUserConnect = TcpPacket.ConnectPacket.parseFrom(msg);
						System.out.println(newUserConnect.getPlayer().getName() + " connected to the lobby.");
					}else if(received.getType() == PacketType.DISCONNECT){
						DisconnectPacket userDisconnect = TcpPacket.DisconnectPacket.parseFrom(msg);
						System.out.println(userDisconnect.getPlayer().getName() + " disconnected from the lobby.");
						
						PlayerListPacket updatePlayerList = PlayerListPacket.newBuilder()
								.setType(PacketType.PLAYER_LIST)
								.build();
						outputStream.write(updatePlayerList.toByteArray());

					}else if(received.getType() == PacketType.PLAYER_LIST){
						PlayerListPacket playerList= TcpPacket.PlayerListPacket.parseFrom(msg);
						int numConnected = playerList.getPlayerListCount();
						System.out.println(numConnected + " users in the lobby:");
						
						for(int i=0; i < numConnected; i++){
							printPlayer(playerList.getPlayerList(i));
						}
						
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException{

		try {
			clientSocket = new Socket("202.92.144.45", 80);
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			outputStream = clientSocket.getOutputStream();
			inputStream = new DataInputStream(clientSocket.getInputStream());
		} catch (UnknownHostException e) {
			System.err.println("Unknown host");
		} catch(IOException e){
			System.out.println("Cannot find (or disconnected from) Server");
		} catch(ArrayIndexOutOfBoundsException e){
			System.out.println("Usage: java Client <server ip> <port no.>");
		}
		
		Client client = new Client();

		if (clientSocket != null && outputStream != null && inputStream != null) {
			try {
				// creates thread that reads input from the server
				
				System.out.println("Welcome to 202.92.144.45!");
				Scanner sc = new Scanner(System.in);
				
				int choice = 0;
				String name = null;
				System.out.println("Enter your name:");
				name = inputLine.readLine().trim();
				
				while (choice != 1 || choice != 2) {
					System.out.println("Options");
					System.out.println("[1] Create Lobby");
					System.out.println("[2] Join Lobby");
					choice = sc.nextInt();
					if (choice == 1 || choice == 2) break;
				}
				
				CreateLobbyPacket receivedCL = null;
				ConnectPacket connectPacket = null;
				ConnectPacket receivedC = null;
				ErrLdnePacket lobbyNotFound = null;

				Player newPlayer = Player.newBuilder()
						.setName(name)
						.build();
				String lobbyId = null;
				int serverOutputLength = 0; 
				byte[] serverOutput = null; 
				
// CREATE LOBBY OPTION
				if (choice == 1){
					
					System.out.print("Enter the maximum number of players: ");
					int max = sc.nextInt();
					CreateLobbyPacket createLobby = CreateLobbyPacket.newBuilder()
							.setType(PacketType.CREATE_LOBBY)
							.setMaxPlayers(max)
							.build();
					outputStream.write(createLobby.toByteArray());
					
//					Initialize byte []
					serverOutput = new byte[0];
					while(serverOutputLength == 0 ) {
						serverOutputLength = inputStream.available();
						serverOutput = new byte[serverOutputLength];
						inputStream.readFully(serverOutput);
					}
					
					receivedCL = TcpPacket.CreateLobbyPacket.parseFrom(serverOutput);

				}else if (choice == 2){
					System.out.print("Enter lobby ID: ");
					sc = new Scanner(System.in);
					lobbyId = sc.nextLine().trim(); 
				}
				
				String terLobbyId = (choice == 1) ? receivedCL.getLobbyId() : lobbyId;

				// Try to connect to the LobbyId 
				connectPacket = ConnectPacket.newBuilder()
						.setType(PacketType.CONNECT)
						.setLobbyId(terLobbyId)
						.setPlayer(newPlayer)
						.build();
	
				outputStream.write(connectPacket.toByteArray());
				
				serverOutputLength = 0; // reset to 0
				serverOutput = new byte[0]; // reset byte [] 
				while(serverOutputLength == 0 ) {
					serverOutputLength = inputStream.available();
					serverOutput = new byte[serverOutputLength];
					inputStream.readFully(serverOutput);
				}
								
//				Get the received packet
				
				TcpPacket receivedPacket = TcpPacket.parseFrom(serverOutput);
				
				if (receivedPacket.getType() == PacketType.CONNECT){
					receivedC = TcpPacket.ConnectPacket.parseFrom(serverOutput);
					if (receivedC.isInitialized()){
						System.out.println("Successfully connected to " + receivedC.getLobbyId());	
					}else{
						System.out.println("Something went wrong, failed to connect to " + terLobbyId);
						closed = true;
					}
				}else if(receivedPacket.getType() == PacketType.ERR_LDNE || receivedPacket.getType() == PacketType.ERR_LFULL || receivedPacket.getType() == PacketType.ERR){
					System.out.println(receivedPacket);
					closed = true; 
				}


				while (!closed && (!clientSocket.isClosed() || !(clientSocket.isInputShutdown() || clientSocket.isOutputShutdown()))){
//					Check if connection is active
//					Start chat
					Thread thread = new Thread(client);
					thread.start();

					PlayerListPacket playerList = PlayerListPacket.newBuilder()
							.setType(PacketType.PLAYER_LIST)
							.build();
					outputStream.write(playerList.toByteArray());

					
					if (closed || clientSocket.isClosed() || (clientSocket.isInputShutdown() || clientSocket.isOutputShutdown())){
						System.out.println("Connection is closed. Exitting...");
						break;
					}
					
					serverOutputLength = inputStream.available();
					serverOutput = new byte[serverOutputLength];
					inputStream.readFully(serverOutput);

//					For sending messages to server / other clients
					
					String message = inputLine.readLine();
					if (message != "") {
						if (message.equals("exit")){
//							Send disconnect packet
							DisconnectPacket thisDisconnect = DisconnectPacket.newBuilder()
									.setType(PacketType.DISCONNECT)
									.build();
							thread.stop();
							outputStream.write(thisDisconnect.toByteArray());
							closed = true;
						}  
						
				        TcpPacket.ChatPacket sendChat = TcpPacket.ChatPacket.newBuilder()
				        		.setType(PacketType.CHAT)
				        		.setMessage(message)
				        		.setPlayer(newPlayer)
				        		.build();
				        
						outputStream.write(sendChat.toByteArray());
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				outputStream.close();
				inputStream.close();
				clientSocket.close();
			}
		}
	}
}