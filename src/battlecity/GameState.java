package battlecity;

import java.util.HashMap;
import java.util.Map;

public class GameState{

	private Map players = new HashMap <String, NetPlayer>();

	public GameState(){

	}

// for updating game state adding or removing players
	public boolean addPlayer(String name, NetPlayer player){
		Object y = players.get(name);
		if (y == null){
			// If add: the player in packet is a new player, not a redundant packet
			// If remove: the player in packet is a valid player
			players.put(name, player);
			return true;
		}
		return false;
	}
	
	public boolean removePlayer(String name) {
		Object y = players.get(name);
		if (y == null) {
			players.remove(name);
			return true;
		}
		return false;
	}
	
	public Map getPlayers(){
		return players;
	}
}
