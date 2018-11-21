package battlecity;

import battlecity.Terrain;

public class Map {
	
	Terrain[][] cells;
	
	public Map() {
//		hard-coded map
		
		cells = new Terrain[25][25];
		
		for (int i = 0; i < 25; i++) {
			for (int j = 0; j < 25; j++) {
				cells[i][j] = new Terrain(Terrain.NONE, i*30, j*30);
			}
		}
//		Set fixed areas of bricks
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				cells[i*2][j*2].setTerrain(Terrain.BRICK);
			}
		}
		
	}
	
	Terrain[][] getTerrain(){
		return this.cells;
	}
}
