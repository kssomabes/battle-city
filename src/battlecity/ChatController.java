package battlecity;

import java.io.IOException;

import battlecity.Main;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import proto.TcpPacketProtos.TcpPacket.ChatPacket;
import proto.TcpPacketProtos.TcpPacket.PlayerListPacket;
import java.util.ResourceBundle;
import java.net.URL;

public class ChatController extends Pane implements Initializable{
	@FXML private TextArea messageBox;
    @FXML private Label usernameLabel;
    @FXML private Label onlineCountLabel;
    @FXML private ListView userList;
    @FXML private Label lobbyLabel;
    
    @FXML ListView chatPane;
//    @FXML BorderPane borderPane;
    
    Main application;
    Tcp tcp; 
    
    String username = "";
    String lobbyId = "";
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {}
    
    public void setApp(Main application) {
    	this.application = application;
    	this.username = application.username; 
    	this.lobbyId = application.lobbyId;
    	this.tcp = new Tcp(username, application.getConnection(), 
				application.getOutputStream(), 
				application.getInputStream(), 
				application.getPlayer(), 
				this);
    	setUsernameLabel(this.username);
    	setLobby(this.lobbyId);
    }
    public void setUsernameLabel(String username) {
        this.usernameLabel.setText(username);
    }
    
    public void setUserList(PlayerListPacket msg) {
        Platform.runLater(() -> {
        	java.util.List<proto.PlayerProtos.Player> players = msg.getPlayerListList();
            userList.setItems(FXCollections.observableList(players));
            userList.setCellFactory(new CellRenderer());
            setOnlineLabel(String.valueOf(msg.getPlayerListCount()));
        });
    }
    
    public void setOnlineLabel(String usercount) {
        Platform.runLater(() -> onlineCountLabel.setText(usercount));
    }
    
    public void setLobby(String lobbyid) {
    	Platform.runLater(() -> lobbyLabel.setText("Lobby ID: " + lobbyid));
    }
    
    public void sendButtonAction() throws IOException {
        String msg = messageBox.getText();
        if (messageBox.getText().trim().length() > 0) {
            Tcp.send(msg.trim());
        }
        messageBox.clear();
    }
    
    public void sendMethod(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            sendButtonAction();
        }
    }
    
    @FXML
    public void closeApplication() {
    	try {
    		application.getInputStream().close();
    		application.getOutputStream().close();
    		application.getConnection().close();
    		tcp.closeStream();
            Platform.exit();
            System.exit(0);
    	}catch(IOException e) {
    		e.printStackTrace();
    	}
		
    }
    
    public synchronized void addToChat(ChatPacket msg) {
        Task<HBox> othersMessages = new Task<HBox>() {
            @Override
            public HBox call() throws Exception {
                BubbledLabel bl6 = new BubbledLabel(); 
                bl6.setText(msg.getPlayer().getName() + ": " + msg.getMessage());
                bl6.setBackground(new Background(new BackgroundFill(Color.WHITE,null, null)));
                HBox x = new HBox();
                bl6.setBubbleSpec(BubbleSpec.FACE_LEFT_CENTER);
                x.getChildren().addAll(bl6);
                return x;
            }
        };

        othersMessages.setOnSucceeded(event -> {
            chatPane.getItems().add(othersMessages.getValue());
        });

        Task<HBox> yourMessages = new Task<HBox>() {
            @Override
            public HBox call() throws Exception {
                BubbledLabel bl6 = new BubbledLabel();
                bl6.setText(msg.getMessage());
                bl6.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN,
                        null, null)));
                HBox x = new HBox();
                x.setMaxWidth(chatPane.getWidth() - 20);
                x.setAlignment(Pos.BOTTOM_RIGHT);
                bl6.setBubbleSpec(BubbleSpec.FACE_RIGHT_CENTER);
                x.getChildren().addAll(bl6);
                return x;
            }
        };
        yourMessages.setOnSucceeded(event -> chatPane.getItems().add(yourMessages.getValue()));

        if (msg.getPlayer().getName().equals(usernameLabel.getText())) {
            Thread t2 = new Thread(yourMessages);
            t2.setDaemon(true);
            t2.start();
        } else {
            Thread t = new Thread(othersMessages);
            t.setDaemon(true);
            t.start();
        }
        chatPane.scrollTo(chatPane.getItems().size());
    }
    
    /* Method to display server messages */
    public synchronized void addAsServer(String msg) {
        Task<HBox> task = new Task<HBox>() {
            @Override
            public HBox call() throws Exception {
                BubbledLabel bl6 = new BubbledLabel();
                bl6.setText(msg);
                bl6.setBackground(new Background(new BackgroundFill(Color.ALICEBLUE,
                        null, null)));
                HBox x = new HBox();
                bl6.setBubbleSpec(BubbleSpec.FACE_BOTTOM);
                x.setAlignment(Pos.CENTER);
                x.getChildren().addAll(bl6);
                return x;
            }
        };
        task.setOnSucceeded(event -> {
            chatPane.getItems().add(task.getValue());
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }
 
}
