package battlecity;
import battlecity.Map;
import java.util.ArrayList;
import proto.PlayerProtos.Player;

public class Game {
	Map map;
	ArrayList <Player> players; 
	
	public Game(ArrayList <Player> connectedPlayers) {
		this.map = new Map();
		this.players = new ArrayList <Player>(); 
	}
	
	Map getMap() {
		return this.map;
	}
	
}
