package battlecity;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Block extends GameObject {
    public Block() {
        super(new Rectangle(25, 25, Color.BLACK));
    }
}