package battlecity;

import java.awt.Graphics;
import java.awt.Rectangle;

public abstract class GameObject {

    protected int x, y; // Location
    protected float velX = 0, velY = 0;

    public GameObject(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Every object needs to update (position, etc.)
    public abstract void tick();
    // Every object needs to draw something (or appear to be something)
    public abstract void render(Graphics g);
    // Every object needs to get bounds for collision detection
    public abstract Rectangle getBounds();

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public float getVelX() {
        return velX;
    }

    public void setVelX(float velX) {
        this.velX = velX;
    }

    public float getVelY() {
        return velY;
    }

    public void setVelY(float velY) {
        this.velY = velY;
    }
}