package battlecity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JPanel;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class GameController extends JPanel implements Constants, Runnable, KeyListener{

	Terrain[][] cells;
	int x, y;
	String serverData;
	String playerName;
	DatagramSocket socket = new DatagramSocket();
	boolean connected = false; 
	Thread thread;
	int gameStage = WAITING_FOR_PLAYERS; 
	
//	Add throws Exception to handle socket exception
	public GameController(String name) throws Exception{
//		Add the GUI stuff here
		this.playerName = name;
        this.addKeyListener(this);
        this.setFocusable(true);
        x = 100;
        y = 100;
		socket.setSoTimeout(4000);
        thread = new Thread(this);
        thread.start();
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
			
			if (!connected && serverData.equals("CONNECTED " + this.playerName)) {
				this.connected = true; // you have successfully connected
				System.out.println("You have been connected");
			} else if (!this.connected) {
				System.out.println("You are trying to connect...");
				send("CONNECT " + this.playerName);
			} else if (this.connected) {
//				Do something if complete/incomplete or receiving other players' data
				if (serverData.equals("START")) {
					System.out.println("Game will start now");
					gameStage = GAME_START; 
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
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    	
    }

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.fillOval(x, y, 50, 50);
		g.setColor(Color.BLACK);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean isConnected() {
		return this.connected;
	}
	
	public boolean isStart() {
		if (gameStage == GAME_START) return true;
		return false;
	}
}