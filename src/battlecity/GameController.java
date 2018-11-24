package battlecity;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

// Eto yung Window
public class GameController extends JPanel{

	public GameController() {
        this.setLayout(new GridLayout());
    }

    public void init() {
        Game game = new Game();
        System.out.println("Hi");
        this.add(game);
    }

}
