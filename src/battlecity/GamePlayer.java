package battlecity;

import java.io.InputStream;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GamePlayer extends GameObject {
    public GamePlayer() {
        super(new Rectangle(20, 20, Color.BLUE));
        InputStream is = getClass().getClassLoader().getResourceAsStream("tank_ico.png");
        Image img = new Image(is);
        super.setImageView(img, 20, 20);
    }
}