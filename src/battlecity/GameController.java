package battlecity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JPanel;

import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class GameController extends Pane implements Constants, Runnable, KeyListener{

	Terrain[][] cells;
	double x, y;
	String serverData;
	String playerName;
	DatagramSocket socket = new DatagramSocket();
	boolean connected = false; 
	Thread thread;
	int gameStage = WAITING_FOR_PLAYERS; // initial game stage

	private List<GameObject> bullets = new ArrayList<>();
    private List<GameObject> blocks = new ArrayList<>();
    private List<GameObject> powerUps = new ArrayList<>();

    private GameObject player;
	
//	Add throws Exception to handle socket exception
	public GameController(String name) throws Exception{
//		Add the GUI stuff here
		addKeyListeners(); 
        this.setFocusTraversable(true);
		this.playerName = name;
        this.setFocusTraversable(true);
		socket.setSoTimeout(4000);
        thread = new Thread(this);
        thread.start();
	}
	
	private void createContent() {

		player = new GamePlayer();
        player.setPosition(new Point2D(0, 0));

        // Load Map
        loadMap();

        // Adds Player
        addGameObject(player, this.x, this.y);

        AnimationTimer timer = new AnimationTimer(){

            @Override
            public void handle(long now) {
                onUpdate();
            }
        };
        timer.start();
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
    
    public void addKeyListeners() {
    	this.setOnKeyPressed(e -> {
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
            e.consume();
            this.requestFocus();
        });
    	this.setOnKeyReleased(e -> {
    		player.stopTank();
    	});
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
        this.getChildren().add(object.getView());
    }

    private void onUpdate() {   // Collision detection
        for (GameObject bullet : bullets) { // Hindi ko alam ahh, baka isang loop lang ang kailangan nilang lahat???
            for (GameObject block : blocks) {
                if (bullet.isColliding(block)) {
                    bullet.setAlive(false);

                    this.getChildren().removeAll(bullet.getView());
                    this.requestFocus();
                }
            }
        }

        for (GameObject block : blocks) {   // May bug pa ito di ko alam kung pano huhu :(
            if (player.isColliding(block)) {	// Yung bug pala ay kapag nag-collide yung player sa dalawang blocks na magkasabay
            	player.bounce(1);
            	//player.stopTank();
            }
        }

        for (GameObject powerUp : powerUps) {
            for (GameObject block : blocks) {
                if (powerUp.isColliding(block)) {   // Para hindi mag-spawn yung powerup sa loob ng isang block/wall/terrain
                    powerUp.setAlive(false);

                    this.getChildren().removeAll(powerUp.getView());
                }
                if(player.isColliding(powerUp)) {   // Player gets powerup  // Okay, may bug din ito, idk
                    player.setPower();
                    powerUp.setAlive(false);

                    this.getChildren().removeAll(powerUp.getView());
                }
            }
            powerUp.updateTimeToLive();
            if(powerUp.getTimeToLive() <= 0) {
                powerUp.setAlive(false);

                this.getChildren().removeAll(powerUp.getView());
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
	public void run(){
//		Sending of data 
		while (thread.isAlive()) {
			try {
				Thread.sleep(1);
			} catch (Exception e) {}

			byte [] receivedData = new byte[1024];
			DatagramPacket packet = new DatagramPacket(receivedData, receivedData.length);

			try {
				socket.receive(packet);
			}catch (SocketTimeoutException e){
			}catch (Exception e) {
				e.printStackTrace();
			}

//			Pre-process received data	
			serverData = new String(receivedData);
			serverData = serverData.trim();

			if (serverData.length() > 0) {
				System.out.println("serverData " + serverData);
			}
			
			if (!connected && serverData.startsWith("CONNECTED " + this.playerName)) {
//				FORMAT: CONNECTED <name> <initialX> <initialY>
				this.connected = true; // you have successfully connected
				System.out.println("You have been connected");
				String [] playerInfo = serverData.split(" ");
				this.x = Double.parseDouble(playerInfo[2]);
				this.y = Double.parseDouble(playerInfo[3]);
			} else if (!this.connected) {
				System.out.println("You are trying to connect...");
				send("CONNECT " + this.playerName);
			} else if (this.connected) {
//				Do something if complete/incomplete or receiving other players' data
				if (serverData.equals("START")) {
					System.out.println("Game will start now");
					createContent();
					gameStage = GAME_START; 
				} else if (serverData.startsWith("PLAYER")) {
//					Player info received from server
//					As of now, catches the movement of the other players
					String [] playerInfo = serverData.split(" ");
					System.out.println("Received a player packet while in-game");
					System.out.println(serverData);
				} else if (serverData.startsWith("DISCONNECTED")) {
//					A player disconnected in-game 
				}
			}else {
//				For player movement repainting?
//				Do something
				System.out.println("Something else");
			} 
		}

		thread.stop();
	}
	
//	Send data to server
	public void send(String msg) {
		try{
			byte[] buf = msg.getBytes();
			InetAddress address = InetAddress.getByName(IPADD);
			DatagramPacket packet = new DatagramPacket(buf, buf.length, address, PORT);
			socket.send(packet);
		} catch (Exception e) {
			e.printStackTrace(); 
		}
	}
	
// Send the keyEvent to the server to inform other players
//	Also send the initial coordinates of the user
	@Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            x += 10;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            x -= 10;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            y -= 10;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            y += 10;
        }
        send("PLAYER " + playerName + " " + this .x + " " + this.y);
    }

    @Override
    public void keyReleased(KeyEvent e) {
    	
    }

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean udpIsConnected() {
		return this.connected;
	}
	
	public boolean isStart() {
		if (gameStage == GAME_START) return true;
		return false;
	}
}