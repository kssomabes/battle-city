package battlecity;

import java.io.InputStream;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PowerUp extends GameObject {
    public PowerUp() {
        super(new Rectangle(20, 20, Color.GREEN));
        super.setTimeToLive(360);   // 360 kasi 60 fps yung game
        InputStream is = getClass().getClassLoader().getResourceAsStream("helmet1.png");
        Image img = new Image(is);
        super.setImageView(img, 20, 20);
    }
}