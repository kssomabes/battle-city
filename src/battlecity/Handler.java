//package battlecity;
//
//import java.awt.Graphics;
//import java.util.LinkedList;
//
//import battlecity.GameObject;
//
//public class Handler {
//
//    LinkedList<GameObject> object = new LinkedList<GameObject>();
//
//    public void tick() {
//        // Runs through all the gameobjects
//        for (int i = 0; i < object.size(); i++) {
//            GameObject tempObject = object.get(i);
//
//            tempObject.tick();
//        }
//    }
//
//    public void render(Graphics g) {
//        // Runs through all the gameobjects
//        for (int i = 0; i < object.size(); i++) {
//            GameObject tempObject = object.get(i);
//
//            tempObject.render(g);
//        }
//    }
//
//    public void addObject(GameObject tempObject) {
//        object.add(tempObject);
//    }
//
//    public void removeObject(GameObject tempObject) {
//        object.remove(tempObject);
//    }
//}