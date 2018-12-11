//package battlecity;
//
//import java.awt.Color;
//import java.awt.Graphics;
//import java.awt.Rectangle;
//
//import battlecity.GameObject;
//
//public class Box extends GameObject {
//
//    public Box(int x, int y) {
//        super(x, y);
//
//        velX = 1;
//    }
//
//    @Override
//    public void tick() {
//        x += velX;
//        y += velY;
//    }
//
//    @Override
//    public void render(Graphics g) {
//        g.setColor(Color.BLUE);
//        g.fillRect(x, y, 30, 30);
//    }
//
//    @Override
//    public Rectangle getBounds() {
//        return null;
//    }
//
//}