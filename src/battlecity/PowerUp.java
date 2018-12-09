package battlecity;

import java.io.InputStream;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PowerUp extends GameObject {
    public PowerUp() {
        super("helmet1.png", 20, 20);
        super.setTimeToLive(360);   // 360 kasi 60 fps yung game
    }
}