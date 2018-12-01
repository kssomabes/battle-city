package battlecity;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
//import battlecity.Game;
import javafx.scene.Group;
import javafx.scene.Parent;

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
import javafx.geometry.Point2D;
import proto.TcpPacketProtos.TcpPacket.*;
import proto.PlayerProtos.Player;
import java.net.Socket;
public class Main extends Application	 {
    private static Stage primaryStageObj;
    
    private HBox root = new HBox(2);

    Pane gameBoard = new Pane(); // holds the whole pane for board
    Group board = new Group();
//    Game game;
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
	
	
	private List<GameObject> bullets = new ArrayList<>();
    private List<GameObject> blocks = new ArrayList<>();
    private List<GameObject> powerUps = new ArrayList<>();

    private GameObject player;
	
	
	private void createContent() {
//        root = new Pane();
//        root.setPrefSize(750, 750);

		gameBoard = new Pane();

        player = new GamePlayer();
        player.setPosition(new Point2D(0, 0));

        // Load Map
        loadMap();

        // Adds Player
        addGameObject(player, 50, 50);

        AnimationTimer timer = new AnimationTimer(){

            @Override
            public void handle(long now) {
                onUpdate();
            }
        };
        timer.start();

//        return gameBoard;
    }

    public void loadMap() {
    	String path = "src/battlecity/Map.txt";
//    	FileReader a = new FileReader(path);
//        try (BufferedReader br = new BufferedReader(new FileReader(getClass().getClassLoader().getResourceAsStream("Map.txt")))) {
    	try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String sCurrentLine;
            int y = 0;
			while ((sCurrentLine = br.readLine()) != null) {
                for (int i = 0; i < sCurrentLine.length(); i++) {
                    if(sCurrentLine.charAt(i) == 'B') {
                        addBlock(new Block(), i*25, y);
                    }
                }
                y += 25;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public void addBullet(GameObject bullet, double x, double y) {
        bullets.add(bullet);
        addGameObject(bullet, x, y);
    }

    public void addBlock(GameObject block, double x, double y) {
        blocks.add(block);
        addGameObject(block, x, y);
    }

    public void addPowerUp(GameObject powerUp, double x, double y) {
        powerUps.add(powerUp);
        addGameObject(powerUp, x, y);
    }

    private void addGameObject(GameObject object, double x, double y) {
        object.getView().setTranslateX(x);
        object.getView().setTranslateY(y);
        gameBoard.getChildren().add(object.getView());
    }

    private void onUpdate() {   // Collision detection
        for (GameObject bullet : bullets) { // Hindi ko alam ahh, baka isang loop lang ang kailangan nilang lahat???
            for (GameObject block : blocks) {
                if (bullet.isColliding(block)) {
                    bullet.setAlive(false);

                    gameBoard.getChildren().removeAll(bullet.getView());
                }
            }
        }

        for (GameObject block : blocks) {   // May bug pa ito di ko alam kung pano huhu :(
            if (player.isColliding(block)) {	// Yung bug pala ay kapag nag-collide yung player sa dalawang blocks na magkasabay
                player.bounce(1);
            }
        }

        for (GameObject powerUp : powerUps) {
            for (GameObject block : blocks) {
                if (powerUp.isColliding(block)) {   // Para hindi mag-spawn yung powerup sa loob ng isang block/wall/terrain
                    powerUp.setAlive(false);

                    gameBoard.getChildren().removeAll(powerUp.getView());
                }
                if(player.isColliding(powerUp)) {   // Player gets powerup  // Okay, may bug din ito, idk
                    player.setPower();
                    powerUp.setAlive(false);

                    gameBoard.getChildren().removeAll(powerUp.getView());
                }
            }
            powerUp.updateTimeToLive();
            if(powerUp.getTimeToLive() <= 0) {
                powerUp.setAlive(false);

                gameBoard.getChildren().removeAll(powerUp.getView());
            }
        }

        bullets.removeIf(GameObject::isDead);
        blocks.removeIf(GameObject::isDead);
        powerUps.removeIf(GameObject::isDead);

        bullets.forEach(GameObject::update);
        blocks.forEach(GameObject::update);
        powerUps.forEach(GameObject::update);

        player.diminishPower();
        player.update();
        if(Math.random() < 0.001) { // powerup randomly spawn
            addPowerUp(new PowerUp(), Math.random()*750, Math.random()*750);
        }
    }
	
	
	
	
	
	 @Override
	    public void start(Stage primaryStage) throws Exception {
	        primaryStageObj = primaryStage;
	        
	        createContent();
	        primaryStage.setScene(new Scene(gameBoard));
	        gameBoard.setOnKeyPressed(e -> {
	            if(e.getCode() == KeyCode.UP) {
	                player.goUp(1);
	            } else if(e.getCode() == KeyCode.DOWN) {
	                player.goDown(1);
	            } else if(e.getCode() == KeyCode.LEFT) {
	                player.goLeft(1);
	            } else if(e.getCode() == KeyCode.RIGHT) {
	                player.goRight(1);
	            } else if(e.getCode() == KeyCode.SPACE) {
	                Bullet bullet = new Bullet();
	                int UP = 1, DOWN = 2, LEFT = 3, RIGHT = 4;
	                // bullet.setPosition(player.getPosition().normalize().multiply(2)); // Speed of bullet
	                bullet.setPosition(player.getPosition());
	                if(player.getLastDirection() == UP) {
	                    bullet.goUp(5);
	                } else if(player.getLastDirection() == DOWN) {
	                    bullet.goDown(5);
	                } else if(player.getLastDirection() == LEFT) {
	                    bullet.goLeft(5);
	                } else if(player.getLastDirection() == RIGHT) {
	                    bullet.goRight(5);
	                }
	                addBullet(bullet, player.getView().getTranslateX()+5, player.getView().getTranslateY()+5);  // Bullet spawns at the center of the player
	            }
	        });
	        gameBoard.setFocusTraversable(true);
//	        gameBoard.requestFocus();	// lol para saan ito?
//	        stage.show();
	        
	        
	        
	        
	        
	        
	        
	        
//	        GameController gameController = new GameController();
//			SwingNode swingNode = new SwingNode();
//
//	        SwingUtilities.invokeLater(new Runnable() {
//	            @Override
//	            public void run() {
//	            	gameController.setLayout(null);
//	            	gameController.setSize(750, 750);
//	                swingNode.setContent(gameController);
//	            }
//	        });
//	        
//	        gameBoard.getChildren().add(swingNode);
//	        gameController.init();
	        
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
