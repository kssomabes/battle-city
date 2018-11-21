package battlecity;
import battlecity.Tcp; 
import battlecity.Game;
import javafx.application.Application; 
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.Group;

public class Main extends Application {
	Terrain[][] cells;
	
	public static void main(String[] args) {
		launch(args);
	} 
	
	/* (non-Javadoc)
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Battle City");
        Game game = new Game();
        cells = game.getMap().getTerrain();
//        Button btn = new Button();
//        btn.setText("Say 'Hello World'");
//        btn.setOnAction(new EventHandler<ActionEvent>() {
// 
//            @Override
//            public void handle(ActionEvent event) {
//                System.out.println("Hello World!");
//            }
//        });
        
        Group root = new Group();
        
        for (int i=0; i < cells.length; i++) {
        	for (int j=0; j < cells.length; j++) {
            	root.getChildren().add(cells[i][j].imgview);
        	}
        }

        //        root.getChildren().add(btn);
        final Scene scene = new Scene(root, 1250, 750);
        scene.setFill(Color.BLUE);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
