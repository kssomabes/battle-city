package battlecity;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Iterator;
import javafx.geometry.Point2D;

public class GameState{

	private Map players = new HashMap <String, NetPlayer>();
	private Map spawnPoints = new HashMap <Integer, Point2D>();
	private Map playerColor = new HashMap <Integer, String>();
	
	public GameState(){
		spawnPoints.put(1, new Point2D(50, 50));
		spawnPoints.put(2, new Point2D(50, 700));
		spawnPoints.put(3, new Point2D(700, 700));
		spawnPoints.put(4, new Point2D(700, 50));
		
		playerColor.put(1, "tank1.png");
		playerColor.put(2, "tank2.png");
		playerColor.put(3, "tank3.png");
		playerColor.put(4, "tank4.png");
	}

// for updating game state adding or removing players
	public boolean addPlayer(String name, NetPlayer player){
		Object y = players.get(name);
		if (y == null){
			// If add: the player in packet is a new player, not a redundant packet
			// If remove: the player in packet is a valid player
			Point2D receivedCoord = spawnPlayer();
			String color = spawnPlayerColor();
			player.setCoordinates(receivedCoord.getX(), receivedCoord.getY());
			player.setColor(color);
			players.put(name, player);
			return true;
		}
		return false;
	}
	
//	For updating the game state
	public void update(String name, NetPlayer netp) {
		players.put(name, netp); // updates key-value pair name with netp
	} 
	
	public boolean removePlayer(String name) {
		Object y = players.get(name);
		if (y != null) {
			players.remove(name);
			return true;
		}
		return false;
	}
	
	public void removeAllPlayer() {
		players = new HashMap <String, NetPlayer>();
	}
	public int getPlayerCount() {
		return players.size();
	}
	
// TODO catch if the maximum number of players has been met
	public Point2D spawnPlayer() {
		Integer index = -1;
		Object y = null; 
		
		while (y == null) {
			index = new Random().nextInt(4) + 1;
			y = spawnPoints.get(index);
		}
		
		Point2D cnv = (Point2D) y;
//		Remove in spawnPoints so that it can no longer be chosen again
		spawnPoints.remove(index);
		return cnv;
	}
	
	public String spawnPlayerColor() {
		Integer index = -1;
		String y = null; 
		
		while (y == null) {
			index = new Random().nextInt(4) + 1;
			y = (String) playerColor.get(index);
		}
		
		playerColor.remove(index);
		return y;
	}
	
	// As of now only passes the player information
		public String toString(){
			String retval = "";
			for(Iterator ite = players.keySet().iterator(); ite.hasNext();){
				String name = (String)ite.next();
				NetPlayer player = (NetPlayer) players.get(name);
				retval += player.toString() + ":";
			}
			return retval;
		}
	
	public Map getPlayers(){
		return players;
	}
}
