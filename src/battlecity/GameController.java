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
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class GameController extends Pane implements Constants, Runnable{

	private final int UP = 1;
	private final int DOWN = 2;
	private final int LEFT = 3;
	private final int RIGHT = 4;
	private int reset = 0;
	private int reset2 = 0;
	private Main application;
	
	void setApp(Main application) {
    	this.application = application;
    }
	void ResetPlayer() {
    	reset = 1;
    }
	
	Terrain[][] cells;
	double x, y;
	String serverData;
	String playerName;
	DatagramSocket socket = new DatagramSocket();
	boolean connected = false;
	Thread thread;
	int gameStage = WAITING_FOR_PLAYERS; // initial game stage
	int bulletsfired = 0;
	String ipAdd = "";
	int port; 
	private List<GameObject> otherPlayers = new ArrayList<>();
	private List<GameObject> bullets = new ArrayList<>();
	private List<GameObject> blocks = new ArrayList<>();
	private List<GameObject> powerUps = new ArrayList<>();
	public AnimationTimer timer;
	private GameObject player;

	// Add throws Exception to handle socket exception
	public GameController(String name, String ipAdd, int port) throws Exception {
		// Add the GUI stuff here
		addKeyListeners();
		this.setFocusTraversable(true);
		this.playerName = name;
		this.setFocusTraversable(true);
		this.ipAdd = ipAdd;
		this.port = port;
		socket.setSoTimeout(4000);
		thread = new Thread(this);
		thread.start();
	}

	public void ResetPanel() {
		final ObservableList<Node> children = this.getChildren();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				children.removeAll(children);
			}
		});
	}
	
	private void createContent() {

		player = new GamePlayer();
		player.setPosition(new Point2D(0, 0));
		player.setLife(5);

		// Load Map
		loadMap();

		// Adds Player
		addGameObject(player, this.x, this.y);

		timer = new AnimationTimer() {

			@Override
			public void handle(long now) {
				onUpdate();
			}
		};
		timer.start();
	}

	public void loadMap() {
		String path = "src/battlecity/Map.txt";
		// FileReader a = new FileReader(path);
		// try (BufferedReader br = new BufferedReader(new
		// FileReader(getClass().getClassLoader().getResourceAsStream("Map.txt")))) {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String sCurrentLine;
			int y = 0;
			while ((sCurrentLine = br.readLine()) != null) {
				for (int i = 0; i < sCurrentLine.length(); i++) {
					if (sCurrentLine.charAt(i) == 'B') {
						addBlock(new Block(), i * 25, y);
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
			if (e.getCode() == KeyCode.UP) {
				player.goUp(1);
			} else if (e.getCode() == KeyCode.DOWN) {
				player.goDown(1);
			} else if (e.getCode() == KeyCode.LEFT) {
				player.goLeft(1);
			} else if (e.getCode() == KeyCode.RIGHT) {
				player.goRight(1);
			} else if (e.getCode() == KeyCode.SPACE) {
				if (player.isAlive()) {
					Bullet bullet = new Bullet();
					int UP = 1, DOWN = 2, LEFT = 3, RIGHT = 4;
					// bullet.setPosition(player.getPosition().normalize().multiply(2)); // Speed of
					// bullet
					bullet.setPosition(player.getPosition());
					if (player.getLastDirection() == UP) {
						bullet.goUp(5);
					} else if (player.getLastDirection() == DOWN) {
						bullet.goDown(5);
					} else if (player.getLastDirection() == LEFT) {
						bullet.goLeft(5);
					} else if (player.getLastDirection() == RIGHT) {
						bullet.goRight(5);
					}
					if (player.getCooldown() <= 0) {
						// get direction para sa harap ng player magi-ispawn yung bullet??? 20x20 yung
						// size ng player
						int x = 5, y = 5; // center of player
						if (player.getLastDirection() == UP) {
							y = -11;
						} else if (player.getLastDirection() == DOWN) {
							y = 21;
						} else if (player.getLastDirection() == LEFT) {
							x = -11;
						} else if (player.getLastDirection() == RIGHT) {
							x = 21;
						}
						addBullet(bullet, player.getView().getTranslateX() + x, player.getView().getTranslateY() + y,
								playerName + '_' + bulletsfired); // Bullet spawns at the center of the player
						double xbullet = player.getView().getTranslateX() + x;
						double ybullet = player.getView().getTranslateY() + y;
						send("CREATEBULLET:" + playerName + "_" + bulletsfired + ":" + xbullet + ":" + ybullet + ":" + player.getLastDirection());
						player.setCooldown(60);
						bulletsfired++;
					}
				}
			}
			e.consume();
			this.requestFocus();
		});
		this.setOnKeyReleased(e -> {
			if (e.getCode() == KeyCode.UP && player.getDirection() == UP) {
				player.stopTank();
			} else if (e.getCode() == KeyCode.DOWN && player.getDirection() == DOWN) {
				player.stopTank();
			} else if (e.getCode() == KeyCode.LEFT && player.getDirection() == LEFT) {
				player.stopTank();
			} else if (e.getCode() == KeyCode.RIGHT && player.getDirection() == RIGHT) {
				player.stopTank();
			}
		});
	}

	public void addBullet(GameObject bullet, double x, double y, String ID) {
		addGameObjectBullet(bullet, x, y, ID);
	}

	public void addBlock(GameObject block, double x, double y) {
		blocks.add(block);
		addGameObject(block, x, y);
	}

	public void addPowerUp(GameObject powerUp, double x, double y, String ID) {
		powerUps.add(powerUp);
		addGameObject(powerUp, x, y, ID);
	}

	private void addGameObject(GameObject object, double x, double y) {
		final ObservableList<Node> children = this.getChildren();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				object.getView().setTranslateX(x);
				object.getView().setTranslateY(y);
				children.add(object.getView());
			}
		});
	}

	private void addGameObjectBullet(GameObject object, double x, double y, String ID) {
		final ObservableList<Node> children = this.getChildren();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				object.getView().setTranslateX(x);
				object.getView().setTranslateY(y);
				object.getView().setId(ID);
				children.add(object.getView());
				bullets.add(object);
			}
		});
	}
	
	private void addGameObject(GameObject object, double x, double y, String ID) {
		final ObservableList<Node> children = this.getChildren();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				object.getView().setTranslateX(x);
				object.getView().setTranslateY(y);
				object.getView().setId(ID);
				children.add(object.getView());
			}
		});
	}

	private void setGameObject(double x, double y, String ID) {
		final ObservableList<Node> children = this.getChildren();
		final String id = new String(ID);
		try {
			for (Node node : children) {
				if (node.getId() != null) {
					if (node.getId().equals(id)) {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								node.setTranslateX(x);
								node.setTranslateY(y);
							}
						});
						break;
					}
				}
			}
		}
		catch(Exception e) {
			System.out.println("Delay. Retrying");
			setGameObject(x, y, ID);
		}

	}

	private void onUpdate() { // Collision detection
		for (GameObject bullet : bullets) { // Hindi ko alam ahh, baka isang loop lang ang kailangan nilang lahat???
			for (GameObject block : blocks) {
				if (bullet.isColliding(block)) {
					bullet.setAlive(false);

					this.getChildren().removeAll(bullet.getView());
					this.requestFocus();
				}
			}
			if (bullet.isColliding(player)) {
				bullet.setAlive(false);
				player.hit();
				if (player.getLife() <= 0) {
					player.setAlive(false);
					this.getChildren().removeAll(player.getView());
				}

				this.getChildren().removeAll(bullet.getView());
				this.requestFocus();
			}
			for (GameObject playerz : otherPlayers) {
				if (playerz.isAlive() && bullet.isColliding(playerz)) {
					bullet.setAlive(false);
					playerz.hit();
					if (playerz.getLife() <= 0) {
						playerz.setAlive(false);
						this.getChildren().removeAll(playerz.getView());
					}
	
					this.getChildren().removeAll(bullet.getView());
					this.requestFocus();
				}
			}
		}

		for (GameObject powerUp : powerUps) {
			for (GameObject block : blocks) {
				if (powerUp.isColliding(block)) { // Para hindi mag-spawn yung powerup sa loob ng isang
													// block/wall/terrain
					powerUp.setAlive(false);

					this.getChildren().removeAll(powerUp.getView());
				}
				
			}
			for (GameObject playerz : otherPlayers) {
				if (playerz.isColliding(powerUp)) {
					playerz.setPower();
					powerUp.setAlive(false);

					this.getChildren().removeAll(powerUp.getView());
				}
			}
			if (player.isColliding(powerUp)) {
				player.setPower();
				powerUp.setAlive(false);

				this.getChildren().removeAll(powerUp.getView());
			}
			powerUp.updateTimeToLive();
			if (powerUp.getTimeToLive() <= 0) {
				powerUp.setAlive(false);

				this.getChildren().removeAll(powerUp.getView());
			}
		}

		bullets.removeIf(GameObject::isDead);
		blocks.removeIf(GameObject::isDead);
		powerUps.removeIf(GameObject::isDead);

		for (GameObject bullet : bullets) {
			bullet.update();
		}

		blocks.forEach(GameObject::update);
		powerUps.forEach(GameObject::update);

		for (GameObject powerup : powerUps) {
			powerup.update();
		}

		player.diminishPower();
		player.updateCooldown();

		send("PLAYER " + this.playerName + " " + player.getView().getTranslateX() + " "
				+ player.getView().getTranslateY());
		player.updateplayer(blocks);
		
		int winnerfound = 1;
		for (GameObject playerz : otherPlayers) {
			if (playerz.isAlive()) {
				winnerfound = 0;
				break;
			}
		}
		if (winnerfound == 1) {
			timer.stop();
			send("WINNER:" + this.playerName);
		}
	}

	@Override
	public void run() {
		// Sending of data
		while (thread.isAlive()) {
			try {
				Thread.sleep(1);
			} catch (Exception e) {
			}

			byte[] receivedData = new byte[1024];
			DatagramPacket packet = new DatagramPacket(receivedData, receivedData.length);

			try {
				socket.receive(packet);
			} catch (SocketTimeoutException e) {
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Pre-process received data
			serverData = new String(receivedData);
			serverData = serverData.trim();

			if (serverData.length() > 0) {
				// System.out.println("serverData " + serverData);
			}

			if (!connected && serverData.startsWith("CONNECTED " + this.playerName)) {
				// FORMAT: CONNECTED <name> <initialX> <initialY>
				this.connected = true; // you have successfully connected
				System.out.println("You have been connected");
				String[] playerInfo = serverData.split(" ");
				this.x = Double.parseDouble(playerInfo[2]);
				this.y = Double.parseDouble(playerInfo[3]);
			} else if (!this.connected) {
				if (this.reset == 1) {
					gameStage = WAITING_FOR_PLAYERS;
					send("RESET");
					reset = 0;
					ResetPanel();
				}
				System.out.println("You are trying to connect...");
				send("CONNECT " + this.playerName);
			} else if (this.connected) {

				if (gameStage == WAITING_FOR_PLAYERS) {
					if (serverData.equals("LOADING")) {
						gameStage = LOADING;
					}
				} else if (gameStage == LOADING) {
					// START message or IMITIAL PLAYERS LIST
					if (serverData.startsWith("PLAYER")) {
						System.out.println("Loading initial player list");
						String[] playersInfo = serverData.split(":");
						otherPlayers.clear();
						for (int i = 0; i < playersInfo.length; i++) {
							// PLAYER name x y
							String[] playerInfo = playersInfo[i].split(" ");
							String pname = playerInfo[1];

							if (pname.equals(this.playerName)) {
								System.out.println("Current player here ");
								continue;
							}
							System.out.println("Player name: " + pname);
							double x = Double.parseDouble(playerInfo[2]);
							double y = Double.parseDouble(playerInfo[3]);

							GameObject playerZ = new GamePlayer();
							playerZ.setPosition(new Point2D(x, y));
							playerZ.setLife(5);
							addGameObject(playerZ, x, y, pname);
							otherPlayers.add(playerZ);
						}
					} else if (serverData.equals("START")) {
						gameStage = GAME_START;
						createContent();
					}
				} else if (gameStage == GAME_START) {
					System.out.println("The game is about to start");
					application.addGameController(this);
					gameStage = IN_PROGRESS;
				} else if (gameStage == IN_PROGRESS) {
					if (serverData.startsWith("PLAYER")) {
						// System.out.println("IN PROGRESS: PLAYER");
						// Player info received from server
						// As of now, catches the movement of the other players
						String[] playersInfo = serverData.split(":");
						//this.getChildren().removeAll(otherPlayers);

						for (int i = 0; i < playersInfo.length; i++) {
							// PLAYER name x y
							String[] playerInfo = playersInfo[i].split(" ");
							String pname = playerInfo[1];

							if (pname.equals(this.playerName))
								continue;

							// System.out.println(pname);

							double x = Double.parseDouble(playerInfo[2]);
							double y = Double.parseDouble(playerInfo[3]);

							// otherPlayers.clear();

							// addGameObject(playerZ, x, y);
							setGameObject(x, y, pname);
						}
					} else if (serverData.startsWith("CREATEBULLET")) {
						String[] objectinfos = serverData.split(":");
						String id = objectinfos[1];
						double x = Double.parseDouble(objectinfos[2]);
						double y = Double.parseDouble(objectinfos[3]);
						int position = Integer.parseInt(objectinfos[4]);
						if (!id.split("_")[0].equals(playerName)) {
							Bullet bullet = new Bullet();
							int UP = 1, DOWN = 2, LEFT = 3, RIGHT = 4;
							// bullet.setPosition(player.getPosition().normalize().multiply(2)); // Speed of
							// bullet
							//bullet.setPosition(player.getPosition());
							if (position == UP) {
								bullet.goUp(5);
							} else if (position == DOWN) {
								bullet.goDown(5);
							} else if (position == LEFT) {
								bullet.goLeft(5);
							} else if (position == RIGHT) {
								bullet.goRight(5);
							}
							
							addBullet(bullet, x, y, id);
						}
					} else if (serverData.startsWith("DISCONNECTED")) {
						// A player disconnected in-game
					} else if (serverData.startsWith("POWERUPS")) {
						String[] objectinfos = serverData.split(":");
						String id = objectinfos[1];
						double x = Double.parseDouble(objectinfos[2]);
						double y = Double.parseDouble(objectinfos[3]);
						addPowerUp(new PowerUp(), x, y, id);
					} else if (serverData.startsWith("WINNER")) {
							String[] objectinfos = serverData.split(":");
							String id = objectinfos[1];
							application.endGame(id);
							timer.stop();
							thread.stop();
					}
				} else {
					// Other stages not catched yet
					// For player movement repainting?
					// Do something
					System.out.println("Something else");
				}
			}
			// Do something if complete/incomplete or receiving other players' data
		}

		if (gameStage != GAME_START) {
			send("DISCONNECT " + this.playerName);
		}

		thread.stop();
	}

	// Send data to server
	public void send(String msg) {
		try {
			byte[] buf = msg.getBytes();
			InetAddress address = InetAddress.getByName(this.ipAdd);
			DatagramPacket packet = new DatagramPacket(buf, buf.length, address, this.port);
			socket.send(packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean udpIsConnected() {
		return this.connected;
	}

	public boolean isStart() {
		if (gameStage == GAME_START)
			return true;
		return false;
	}
}