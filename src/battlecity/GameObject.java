package battlecity;

import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

/**
 * GameObject
 */
public class GameObject implements Constants {

    private Point2D position = new Point2D(0, 0);

    private final int STOP = 0;
    private final int UP = 1;
    private final int DOWN = 2;
    private final int LEFT = 3;
    private final int RIGHT = 4;
    private int lastDirection = 1;
    private int direction = 0;
    private int invulnerableTimeLeft = 0;
    private int timeToLive = 360;	// powerup
    private int cooldown = 0;
    private int life = 0;
    private int damage = 1;
    
    private ImageView imgView;

    private Boolean alive = true;

    public GameObject(String imgLocation, int height, int width) {
    	InputStream is = getClass().getClassLoader().getResourceAsStream(imgLocation);
		Image img = new Image(is);
        this.imgView = new ImageView(img);
        this.imgView.setFitHeight(height);
        this.imgView.setFitWidth(width);
    }

    public void update() {
        imgView.setTranslateX(imgView.getTranslateX() + position.getX());
        imgView.setTranslateY(imgView.getTranslateY() + position.getY());
    }
	
    public void updateplayer(List<GameObject> blocks) {
    	boolean willCollide = false;
    	
    	for (GameObject block : blocks) {
    		if (block.getView().getTranslateX() >= this.getView().getTranslateX() - 22 && block.getView().getTranslateX() <= this.getView().getTranslateX() + 22) {
                
    			if (this.direction == UP){
	    			if (block.getView().getTranslateY() + 23 >= this.getView().getTranslateY() - 1 && block.getView().getTranslateY() <= this.getView().getTranslateY() - 1) {
	                	willCollide = true;
	                	break;
	                }
	    		}
    			if (this.direction == DOWN) {
	                if (block.getView().getTranslateY() <= this.getView().getTranslateY() + 24 && block.getView().getTranslateY() >= this.getView().getTranslateY() + 24) {
	                	willCollide = true;
	                	break;
	                }
    			}
    		}
    		if (block.getView().getTranslateY() >= this.getView().getTranslateY() - 22 && block.getView().getTranslateY() <= this.getView().getTranslateY() + 22) {
    			if (this.direction == LEFT){
	    			if (block.getView().getTranslateX() + 23 >= this.getView().getTranslateX() - 1 && block.getView().getTranslateX() <= this.getView().getTranslateX() - 1) {
	                	willCollide = true;
	                	break;
	                }
	    		}
    			if (this.direction == RIGHT) {
	                if (block.getView().getTranslateX() <= this.getView().getTranslateX() + 24 && block.getView().getTranslateX() >= this.getView().getTranslateX() + 24) {
	                	willCollide = true;
	                	break;
	                }
    			}
    		}
    	}
    	if (!willCollide) {
	    	imgView.setTranslateX(imgView.getTranslateX() + position.getX());
	        imgView.setTranslateY(imgView.getTranslateY() + position.getY());
    	}
    }

    public void setPosition(Point2D position) {
        this.position = position;
    }

    public Point2D getPosition() {
        return this.position;
    }

    public ImageView getView() {
        return imgView;
    }

    public Boolean isAlive() {
        return alive;
    }

    public Boolean isDead() {
        return !alive;
    }

    public void setAlive(Boolean alive) {
        this.alive = alive;
    }

    public void goUp(int speed) {
        int x = 0, y;
        if(getPosition().getY() == 1) y = 0;
        else y = -1;
        setPosition((new Point2D(x, y)).normalize().multiply(speed));
        if(y==0) direction = STOP;
        direction = UP;
        lastDirection = UP;
        imgView.setRotate(0); 
    }

    public void goDown(int speed) {
        int x = 0, y;
        if(getPosition().getY() == -1) y = 0;
        else y = 1;
        setPosition((new Point2D(x, y)).normalize().multiply(speed));
        if(y==0) direction = STOP;
        direction = DOWN;
        lastDirection = DOWN;
        imgView.setRotate(180);
    }

    public void goLeft(int speed) {
        int x, y = 0;
        if(getPosition().getX() == 1) x = 0;
        else x = -1;
        setPosition((new Point2D(x, y)).normalize().multiply(speed));
        if(x==0) direction = STOP;
        direction = LEFT;
        lastDirection = LEFT;
        imgView.setRotate(270);
    }

    public void goRight(int speed) {
        int x, y = 0;
        if(getPosition().getX() == -1) x = 0;
        else x = 1;
        setPosition((new Point2D(x, y)).normalize().multiply(speed));
        if(x==0) direction = STOP;
        direction = RIGHT;
        lastDirection = RIGHT;
        imgView.setRotate(90);
    }
    
    public void stopTank() {
    	setPosition(new Point2D(0, 0));
    	direction = STOP;
    }
    
    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public int getCooldown() {
        return cooldown;
    }
    
    public void updateCooldown() {
    	cooldown -= 1;
    }
    
    public void setLife(int life) {
        this.life = life;
    }

    public int getLife() {
        return life;
    }
    
    public void hit() {
    	life -= 1;
    }
    
    public void setPower() {
        invulnerableTimeLeft = 180; // Ito yung 3 seconds since 60 fps yung game
    }

    public void diminishPower() {
        invulnerableTimeLeft -= 1;
    }

    public int getDirection() {
        return direction;
    }

    public int getLastDirection() {
        return lastDirection;
    }

    /**
     *
     *  PowerUp methods
     */

    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public void updateTimeToLive() {
        timeToLive -= 1;
    }

    public Boolean isColliding(GameObject other) {
        return getView().getBoundsInParent().intersects(other.getView().getBoundsInParent());
    }

}