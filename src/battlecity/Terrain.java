package battlecity;

import javafx.scene.image.*;

public class Terrain {
//	Constants for possible Terrain Types
	public static final char NONE = 'E';
	public static final char BRICK = 'B';

	Image img; 
	public ImageView imgview = new ImageView();
	
	int life = 2;
	int locX; 
	int locY;
	
	private char terrainType = 'E'; // default is none 

	public Terrain(char initialType, int locX, int locY){
//		Constructor
		setTerrain(initialType);
		setImage(initialType);
		this.locX = locX;
		this.locY = locY;
		this.imgview.setX(locX);
		this.imgview.setY(locY);
		this.imgview.setFitWidth(30);
		this.imgview.setFitHeight(30);
	}
	
	void setTerrain(char newType){
		this.terrainType = newType;
		setImage(newType);
	}
	
	void setImage(char newType) {
		if (newType == BRICK) {
			this.img = new Image(getClass().getClassLoader().getResource("brick.png").toString());
			this.imgview.setImage(this.img);
		}else {
			this.img = new Image(getClass().getClassLoader().getResource("empty.png").toString());
			this.imgview.setImage(this.img);
		}
	} 
}
