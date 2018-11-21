package battlecity;

import java.io.IOException;

import battlecity.Main;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Duration;

import proto.PlayerProtos.Player;
import proto.TcpPacketProtos.TcpPacket;
import proto.TcpPacketProtos.TcpPacket.ChatPacket;
import proto.TcpPacketProtos.TcpPacket.ConnectPacket;
import proto.TcpPacketProtos.TcpPacket.CreateLobbyPacket;
import proto.TcpPacketProtos.TcpPacket.DisconnectPacket;
import proto.TcpPacketProtos.TcpPacket.ErrLdnePacket;
import proto.TcpPacketProtos.TcpPacket.PacketType;
import proto.TcpPacketProtos.TcpPacket.PlayerListPacket;

public class ChatController {
	@FXML private TextArea messageBox;
    @FXML private Label usernameLabel;
    @FXML private Label onlineCountLabel;
    @FXML private ListView userList;
    @FXML private Label lobbyLabel;
    
    @FXML ListView chatPane;
    @FXML BorderPane borderPane;
    
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
        Platform.exit();
        System.exit(0);
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
                x.setAlignment(Pos.BOTTOM_RIGHT);
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
