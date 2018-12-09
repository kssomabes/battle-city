package battlecity;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Iterator;

public class GameServer implements Runnable, Constants{
	int numPlayers = 0; // the required number of players in the game
	int currentNum = 0; // counter for current number of players
	DatagramSocket serverSocket = null;
	Thread thread = new Thread(this);
	String receivedDataString;
	int gameStage = WAITING_FOR_PLAYERS;
	GameState game;

	public GameServer(int numPlayers) {
		this.numPlayers = numPlayers; 
		this.game = new GameState();
		try {
			serverSocket = new DatagramSocket(null);
			InetSocketAddress address = new InetSocketAddress(IPADD, PORT);
			serverSocket.bind(address);
			serverSocket.setSoTimeout(4000);
		}catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		thread.start();
	}
	
	public void run() {
		System.out.println("In run!");
		int powerupsadded = 0;
		while(true) {
			try {
				Thread.sleep(1);
			} catch (Exception e) {}
			
//			Get the data using DatagramPacket
			byte[] buf = new byte[1024];
			DatagramPacket packetReceived = new DatagramPacket(buf, buf.length);
			
			try {
				serverSocket.receive(packetReceived);
			}catch (Exception e) {}
			
//			Pre-process received data
			receivedDataString = new String(buf);
//			remove excess bytes 
			receivedDataString = receivedDataString.trim();
//			if (receivedDataString.length() > 0) System.out.println("Server received: " + receivedDataString);

//			Handle received data
//			if (receivedDataString.length() == 0) continue; // continue if empty, removed due to switch-case issue
			
			if(Math.random() < 0.001) { // powerup randomly spawn
				broadcast("POWERUPS:POWERUP" + powerupsadded + ":" + Math.random()*750 + ":" + Math.random()*750);
				powerupsadded++;
			}
			
			switch (gameStage) {
			// TO-DO: Serialization 
				case WAITING_FOR_PLAYERS:
					if (receivedDataString.startsWith("CONNECT")){
						// A user is trying to connect, broadcast this to other users if valid 
						// TO-DO: Improve regex in case of names with spaces
						String tokens[] = receivedDataString.split(" ");
						// tokens[1] = playerName
						NetPlayer player = new NetPlayer(tokens[1], packetReceived.getAddress(), packetReceived.getPort());
						// Update gameState if new user has connected
						boolean updated = game.addPlayer(tokens[1].trim(), player);
						// Check if it is really a new user
						if (updated){
							this.currentNum++;
							System.out.println("Sending CONNECTED " + tokens[1] + " " + player.getCoordinates());	
							broadcast("CONNECTED " + tokens[1] + " " + player.getCoordinates());
						}
						System.out.println(this.currentNum);
						// Check if the required number of players has been met
						if (this.currentNum == this.numPlayers){
							broadcast("LOADING");
							broadcast(game.toString()); // initial positions 
							gameStage = GAME_START;
						}
					}else if (receivedDataString.startsWith("DISCONNECT")) {
//						A user disconnected even before game starts, broadcast this and decrement number of players
						String tokens[] = receivedDataString.split(" ");
//						Update gameState to remove player
						boolean updated = game.removePlayer(tokens[1].trim());
//						Check if updated
						if (updated) {
							this.currentNum--;
							System.out.println("Sending DISCONNECTED " + tokens[1]);
							broadcast("DISCONNECTED " + tokens[1]);
						}
					}	
//					else{
//						System.out.println("Unhandled packet message: " + receivedDataString);
//					}
					break;
				case GAME_START:
					System.out.println("Game is starting!");
					broadcast("START");
					gameStage = IN_PROGRESS;
					break;
				case IN_PROGRESS:
					if (receivedDataString.startsWith("DISCONNECT")) {
//						A user disconnected even before game starts, broadcast this and decrement number of players
						String tokens[] = receivedDataString.split(" ");
//						Update gameState to remove player
						boolean updated = game.removePlayer(tokens[1].trim());
//						Check if updated
						if (updated) {
							this.currentNum--;
							System.out.println("Sending DISCONNECTED " + tokens[1]);
							broadcast("DISCONNECTED " + tokens[1]);
						}
					}else if (receivedDataString.startsWith("PLAYER")) {
//						Format: PLAYER <name> <x> <y>
						String [] playerInfo = receivedDataString.split(" ");
						String pname = playerInfo[1];
						double x = Double.parseDouble(playerInfo[2].trim());
						double y = Double.parseDouble(playerInfo[3].trim());
						NetPlayer player = (NetPlayer) this.game.getPlayers().get(pname);					  
						player.setCoordinates(x, y);
						
						// Since server received a player packet update the game state
						game.update(pname, player); 

						// Broadcast the updated game state to all the players 
						broadcast(game.toString());
					}else {
						broadcast(receivedDataString);
					}
					break;
				case GAME_END:
					break;
			}
			
		}
	}

// For sending data to all players
	public void broadcast (String msg){
		// 
		for(Iterator ite=game.getPlayers().keySet().iterator();ite.hasNext();){
			String name = (String)ite.next();
			NetPlayer player= (NetPlayer) game.getPlayers().get(name);			
			send(player,msg);	// call Send method
		}
	}

	public void send (NetPlayer player, String msg){
		// Construct message
		DatagramPacket packet; 
		byte [] buf = msg.getBytes();
		packet = new DatagramPacket(buf, buf.length, player.getAddress(), player.getPort());
		try {
			serverSocket.send(packet); // send Message 
		}	catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void spawnPosition() {
//		Limit number of players for the game
//		Depending on the map, give the four coordinates of the spawn positions

	}
	
	public static void main(String[] args){
		try {
			new GameServer(Integer.parseInt(args[0]));
		} catch (Exception e) {
			System.out.println("Usage: java GameServer <number of users>");
		}
	}
}
