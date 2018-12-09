package battlecity;

import java.io.InputStream;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Block extends GameObject {
    public Block() {
        super(new Rectangle(23, 23, Color.BLACK));
        InputStream is = getClass().getClassLoader().getResourceAsStream("brick.png");
        Image img = new Image(is);
        super.setImageView(img, 23, 23);
    }
}