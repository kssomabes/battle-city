package battlecity;

import java.io.InputStream;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Bullet extends GameObject {
    public Bullet() {
        super(new Rectangle(5, 5, Color.BROWN));
        InputStream is = getClass().getClassLoader().getResourceAsStream("tank_ico.png");
        Image img = new Image(is);
        super.setImageView(img, 5, 5);
    }
}