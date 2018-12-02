package battlecity;

import javafx.geometry.Point2D;
import javafx.scene.Node;

/**
 * GameObject
 */
public class GameObject {

    private Node view;
    private Point2D position = new Point2D(0, 0);

    private final int STOP = 0;
    private final int UP = 1;
    private final int DOWN = 2;
    private final int LEFT = 3;
    private final int RIGHT = 4;
    private int lastDirection = 1;
    private int direction = 0;
    private int invulnerableTimeLeft = 0;
    private int timeToLive = 360;

    private Boolean alive = true;

    public GameObject(Node view) {
        this.view = view;
    }

    public void update() {
        view.setTranslateX(view.getTranslateX() + position.getX());
        view.setTranslateY(view.getTranslateY() + position.getY());
    }

    public void setPosition(Point2D position) {
        this.position = position;
    }

    public Point2D getPosition() {
        return this.position;
    }

    public Node getView() {
        return view;
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
    }

    public void goDown(int speed) {
        int x = 0, y;
        if(getPosition().getY() == -1) y = 0;
        else y = 1;
        setPosition((new Point2D(x, y)).normalize().multiply(speed));
        if(y==0) direction = STOP;
        direction = DOWN;
        lastDirection = DOWN;
    }

    public void goLeft(int speed) {
        int x, y = 0;
        if(getPosition().getX() == 1) x = 0;
        else x = -1;
        setPosition((new Point2D(x, y)).normalize().multiply(speed));
        if(x==0) direction = STOP;
        direction = LEFT;
        lastDirection = LEFT;
    }

    public void goRight(int speed) {
        int x, y = 0;
        if(getPosition().getX() == -1) x = 0;
        else x = 1;
        setPosition((new Point2D(x, y)).normalize().multiply(speed));
        if(x==0) direction = STOP;
        direction = RIGHT;
        lastDirection = RIGHT;
    }

    public void bounce(int speed) {  // May bug, di ko alam kung bakit iyun yung nangyayari
//        System.out.println("direction = " + direction);
        if (direction == UP) {
            goDown(speed);
            goDown(speed);
        }
        else if (direction == DOWN) {
            goUp(speed);
            goUp(speed);
        }
        else if (direction == LEFT) {
            goRight(speed);
            goRight(speed);
        }
        else if (direction == RIGHT) {
            goLeft(speed);
            goLeft(speed);
        }
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