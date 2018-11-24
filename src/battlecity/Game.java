package battlecity;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;

import battlecity.Handler;

public class Game extends Canvas implements Runnable{

    private Boolean isRunning = false;
    private Thread thread;
//    private Handler handler;

    public Game () {
        start();

//        handler = new Handler();
//        handler.addObject(new Box(100, 100));
//        handler.addObject(new Box(200, 100));
    }

    public void start() {
        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        isRunning = false;
        try {
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() { // Game Loop
        this.requestFocus();
        long lastTime = System.nanoTime();
        double amountofTicks = 60.0;
        double ns = 1000000000 / amountofTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;
        while(isRunning) {
            if(!this.isDisplayable()){
                System.out.println("continue");
                continue;
            }
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while(delta >= 1) {
                tick();
                //updates++;
                delta--;
            }
            render();
            frames++;

            if(System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                frames = 0;
                //updates = 0;
            }
        }
        stop();
    }

    public void tick() {
        // Updates everything in the game
        // Gets updated 60 times a second
//        handler.tick();
    }

    public void render() {
        // Runs everything in the game
        // Gets updated a couple thousand times a second
        BufferStrategy bs = this.getBufferStrategy();
        if(bs == null) {
            // There are 3 frames being loaded behind the current frame
            this.createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();
        // This is where we start to draw

        g.setColor(Color.RED);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Renders objects above background
//        handler.render(g);

        // This is where we end our drawing
        g.dispose();
        bs.show();	// May error kasi parang wala siyang nalo-load na buffer mula dun sa this.createBufferStrategy(3);
    }
}