package battlecity;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import battlecity.Game;
import javafx.scene.Group;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.DataInputStream;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import proto.TcpPacketProtos.TcpPacket.*;
import proto.PlayerProtos.Player;
import java.net.Socket;
public class Main extends Application	 {
    private static Stage primaryStageObj;
    
    private HBox root = new HBox(2);

    Pane gameBoard = new Pane(); // holds the whole pane for board
    Group board = new Group();
    Game game;
//	Terrain[][] cells = game.getMap().getTerrain();
	boolean isAuth = false;
	
	ConnectPacket established;
	String username = "";
	String lobbyId = "";
	Player loggedIn;
	Socket connection;
	DataInputStream inputStream; 
	OutputStream outputStream;
	ChatController chatCont;
	
	 @Override
	    public void start(Stage primaryStage) throws Exception {
	        primaryStageObj = primaryStage;
	        
	        GameController gameController = new GameController();
			SwingNode swingNode = new SwingNode();

	        SwingUtilities.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	            	gameController.setLayout(null);
	            	gameController.setSize(750, 750);
	                swingNode.setContent(gameController);
	            }
	        });
	        
	        gameBoard.getChildren().add(swingNode);
	        gameController.init();
	        
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
	
	public void openChat(ConnectPacket packet,  String username, Socket socket, OutputStream outputStream, DataInputStream inputStream, Player loggedIn, String lobbyId) {
		this.established = packet;
		this.connection = socket;
		this.username = username; 
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.loggedIn = loggedIn;
		this.lobbyId = lobbyId;
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
	
//	public void addCells() {
//		for (int i=0; i < cells.length; i++) {
//        	for (int j=0; j < cells.length; j++) {
//            	board.getChildren().add(cells[i][j].imgview);
//        	}
//        }
//	}

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
        	root.getChildren().add(page);
//        	Load board when the number of players is met 
        	root.getChildren().add(gameBoard);
        }else {
        	root.getChildren().add(page);
        }
        return (Initializable)loader.getController();
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
