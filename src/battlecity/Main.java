package battlecity;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
//import battlecity.Game;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileReader;
import java.io.IOException;

import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import proto.TcpPacketProtos.TcpPacket.*;
import proto.PlayerProtos.Player;
import java.net.Socket;

public class Main extends Application	 {
    private static Stage primaryStageObj;
    
    private HBox root = new HBox(2);

    Pane gameBoard = new Pane(); // holds the whole pane for board
    Group board = new Group();
	boolean isAuth = false;
	
	ConnectPacket established;
	String username = "";
	String lobbyId = "";
	String ipAdd = "";
	int port; 
	Player loggedIn;
	Socket connection;
	DataInputStream inputStream; 
	OutputStream outputStream;
	ChatController chatCont;
	GameController gameController; 

	 @Override
	    public void start(Stage primaryStage) throws Exception {
	        primaryStageObj = primaryStage;

	        primaryStage.initStyle(StageStyle.UNDECORATED);
	        primaryStage.setTitle("Battle City");
	        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResource("tank_ico.png").toString()));
	        
	        Scene mainScene = new Scene(parentContent(), 1250, 750);
//	        mainScene.setRoot(root);
	        primaryStage.setResizable(false);
	        primaryStage.setScene(mainScene);
	        primaryStage.show();
	        primaryStage.setOnCloseRequest(e -> Platform.exit());
	    }
	
	public HBox parentContent() {
		login(); // login will be loaded first
		return root; 
	}

	public void login() {
		try {
			UserController loginCont = (UserController)replaceSceneContent("LoginView.fxml");
			loginCont.setApp(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
	public void openChat(ConnectPacket packet,  String username, Socket socket, OutputStream outputStream,
			DataInputStream inputStream, Player loggedIn, String lobbyId, String ipAdd, int port) {
		this.established = packet;
		this.connection = socket;
		this.username = username; 
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.loggedIn = loggedIn;
		this.lobbyId = lobbyId;
		this.ipAdd = ipAdd;
		this.port = port;
		chat();
	}
	
	public void chat() {
		try {
			chatCont = (ChatController) replaceSceneContent("ChatView.fxml");
			chatCont.setApp(this);
//			This will initialize Tcp Chat
//			Also initialize game server
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void endGame(String id) {
		Main mainapplication = this;
		
		Label lbl = new Label("WINNER: " + id);
		lbl.setFont(new Font(50));
		VBox vbEndgame = new VBox();
		Button btn = new Button();
		btn.setText("Play Again");
		vbEndgame.getChildren().add(lbl);
		vbEndgame.getChildren().add(btn);
		vbEndgame.setPadding(new Insets(300, 200, 300, 200));
		
		btn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
            public void handle(ActionEvent event) {
				root.getChildren().remove(vbEndgame);
				try {
					gameController = new GameController(username, ipAdd, port);
					gameController.ResetPlayer();
		        	gameController.setOnMouseClicked(new EventHandler<MouseEvent>(){

		                @Override
		                public void handle(MouseEvent arg0) {
		                	gameController.requestFocus();
		                }

		            });

			        System.out.println("The lobby ID is " + lobbyId);
			        
			        gameController.setApp(mainapplication);
			        root.getChildren().add(gameController);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
		});
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				root.getChildren().remove(gameController);
				root.getChildren().add(vbEndgame);
			}
		});
	}

//	For replacing FXML
	private Initializable replaceSceneContent(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        InputStream in = getClass().getClassLoader().getResourceAsStream(fxml);
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(Main.class.getResource(fxml));
        Pane page;

        try {
            page = (Pane) loader.load(in);
        } finally {
            in.close();
        }
        root.getChildren().clear(); // getChildren().removeAll() does not work in HBox
        
        if (fxml.equals("ChatView.fxml")) {        	
//        	Load board when the number of players is met 
//        	game controller also connects to UDP
        	this.gameController = new GameController(this.username, this.ipAdd, this.port);
        	gameController.setOnMouseClicked(new EventHandler<MouseEvent>(){

                @Override
                public void handle(MouseEvent arg0) {
                	gameController.requestFocus();
                }

            });

	        System.out.println("The lobby ID is " + this.lobbyId);
	        root.getChildren().add(page);

	        gameController.setApp(this);
        }else {
        	root.getChildren().add(page);
        }
        return (Initializable)loader.getController();
    }
	
	public void addGameController(GameController gamecontroller) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					root.getChildren().add(gamecontroller);
				}
				catch(Exception e) {
					
				}
			}
		});
	}
	
//	Setters
	public void setIsAuth(boolean val) {
		isAuth = val; 
	}
	
//	Getters
	public HBox getRoot() {
		return this.root;
	}
	
	public boolean getIsAuth() {
		return isAuth;
	}

	public OutputStream getOutputStream() {
		return this.outputStream;
	}
	
	public DataInputStream getInputStream() {
		return this.inputStream;
	}
	
	public Player getPlayer() {
		return this.loggedIn;
	}
	
	public Socket getConnection() {
		return this.connection;
	}
	
	public static Stage getPrimaryStage() {
        return primaryStageObj;
    }
	
	public static void main(String[] args) {
		launch(args);
	}
}
