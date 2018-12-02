package battlecity;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PowerUp extends GameObject {
    public PowerUp() {
        super(new Rectangle(20, 20, Color.GREEN));
        super.setTimeToLive(360);   // 360 kasi 60 fps yung game
    }
}