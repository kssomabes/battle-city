package battlecity;

import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

public class CellRenderer implements Callback<ListView<proto.PlayerProtos.Player>,ListCell<proto.PlayerProtos.Player>> {
	public ListCell<proto.PlayerProtos.Player> call(ListView<proto.PlayerProtos.Player> p) {

        ListCell<proto.PlayerProtos.Player> cell = new ListCell<proto.PlayerProtos.Player>(){

            @Override
            protected void updateItem(proto.PlayerProtos.Player user, boolean bln) {
                super.updateItem(user, bln);
                setGraphic(null);
                setText(null);
                if (user != null) {
                    HBox hBox = new HBox();

                    Text name = new Text(user.getName());

                    hBox.getChildren().addAll(name);
                    hBox.setAlignment(Pos.CENTER_LEFT);

                    setGraphic(hBox);
                }
            }
        };
        return cell;
    }
}
