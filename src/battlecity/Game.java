package battlecity;
import battlecity.Map;
public class Game {
	Map map;
	public Game() {
		this.map = new Map();
	}
	
	Map getMap() {
		return this.map;
	}
	
}
