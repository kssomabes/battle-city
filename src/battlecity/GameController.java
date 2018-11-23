package battlecity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

public class GameController extends JPanel implements KeyListener{

	Terrain[][] cells;
	int x, y;
	
	public GameController() {
        this.addKeyListener(this);
        this.setFocusable(true);
        x = 100;
        y = 100;
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
}
