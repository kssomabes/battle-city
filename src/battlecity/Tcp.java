package battlecity;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import proto.PlayerProtos.Player;
import proto.TcpPacketProtos.TcpPacket;
import proto.TcpPacketProtos.TcpPacket.ChatPacket;
import proto.TcpPacketProtos.TcpPacket.ConnectPacket;
import proto.TcpPacketProtos.TcpPacket.DisconnectPacket;
import proto.TcpPacketProtos.TcpPacket.PacketType;
import proto.TcpPacketProtos.TcpPacket.PlayerListPacket;
import battlecity.ChatController;

public class Tcp implements Runnable{
	private static final String HASCONNECTED = "has connected";
	private static Socket clientSocket = null;
	private static OutputStream outputStream = null;
	private static DataInputStream inputStream = null;
	private static BufferedReader inputLine = null;
	private static boolean closed = false;
	private Thread thread = new Thread(this);
	private static Player newPlayer = null;
	public ChatController controller;
	public static String username;
	
	public Tcp(String username, Socket clientsocket, OutputStream outputStream, DataInputStream inputStream, Player newPlayer, ChatController con) {
//		Constructor
		try {
			this.clientSocket = clientsocket;
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			Tcp.outputStream = outputStream;
			Tcp.inputStream = inputStream;
			Tcp.username = username;
			Tcp.newPlayer = newPlayer;
			this.controller = con; 
			TCPConnect();
		} catch (UnknownHostException e) {
			System.err.println("Unknown host");
		} catch(IOException e){
			System.out.println("Cannot find (or disconnected from) Server");
		}
	}
	
	public void TCPConnect() throws IOException{
		int serverOutputLength = 0; 
		byte[] serverOutput = null;
		if (clientSocket != null && outputStream != null && inputStream != null) {
			try {
				thread.start();
//				Get the list of players currently in lobby upon entering the lobby 
				PlayerListPacket playerList = PlayerListPacket.newBuilder()
					.setType(PacketType.PLAYER_LIST)
					.build();
				outputStream.write(playerList.toByteArray());
				serverOutputLength = 0; // reset to 0
				serverOutput = new byte[0]; // reset byte [] 
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
    public static void send(String msg) throws IOException {
    	if (msg != "") {
		TcpPacket.ChatPacket sendChat = TcpPacket.ChatPacket.newBuilder()
		        .setType(PacketType.CHAT)
		        .setMessage(msg)
		        .setPlayer(newPlayer)
		        .build();
		outputStream.write(sendChat.toByteArray());
		}
    }
    
	public void run() {
//		Server output after connecting to Lobby Chat
		try {
			int msgLen = 0;
			byte[] msg = new byte[0];
			while (msgLen == 0) {
				msgLen = inputStream.available();
				msg = new byte[msgLen];
				inputStream.readFully(msg);
								
				if (msgLen != 0) {
					TcpPacket received = TcpPacket.parseFrom(msg);
					if (received.getType() == PacketType.CHAT){
						ChatPacket chatreceived = TcpPacket.ChatPacket.parseFrom(msg);
						controller.addToChat(chatreceived);
					}else if(received.getType() == PacketType.CONNECT){
						ConnectPacket newUserConnect = TcpPacket.ConnectPacket.parseFrom(msg);
						controller.addAsServer(newUserConnect.getPlayer().getName() + " connected to the lobby.");
						PlayerListPacket updatePlayerList = PlayerListPacket.newBuilder()
								.setType(PacketType.PLAYER_LIST)
								.build();
						outputStream.write(updatePlayerList.toByteArray());
					}else if(received.getType() == PacketType.DISCONNECT){
						DisconnectPacket userDisconnect = TcpPacket.DisconnectPacket.parseFrom(msg);
						controller.addAsServer(userDisconnect.getPlayer().getName() + " disconnected from the lobby.");
//						Someone disconnected, update player list
						PlayerListPacket updatePlayerList = PlayerListPacket.newBuilder()
								.setType(PacketType.PLAYER_LIST)
								.build();
						outputStream.write(updatePlayerList.toByteArray());

					}else if(received.getType() == PacketType.PLAYER_LIST){
						PlayerListPacket playerList= TcpPacket.PlayerListPacket.parseFrom(msg);
						controller.setUserList(playerList);
					}
				}
				msgLen = 0; // reset msgLen for the if condition
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void closeStream() {
		try {
			inputStream.close();
			outputStream.close();
			thread.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
