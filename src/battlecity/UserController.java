package battlecity;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import proto.TcpPacketProtos.TcpPacket.CreateLobbyPacket;
import proto.TcpPacketProtos.TcpPacket.PacketType;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.ResourceBundle;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import proto.PlayerProtos.Player;
import proto.TcpPacketProtos.TcpPacket;
import proto.TcpPacketProtos.TcpPacket.ChatPacket;
import proto.TcpPacketProtos.TcpPacket.ConnectPacket;
import proto.TcpPacketProtos.TcpPacket.CreateLobbyPacket;
import proto.TcpPacketProtos.TcpPacket.DisconnectPacket;
import proto.TcpPacketProtos.TcpPacket.ErrLdnePacket;
import proto.TcpPacketProtos.TcpPacket.PacketType;
import proto.TcpPacketProtos.TcpPacket.PlayerListPacket;

public class UserController implements Initializable {
    @FXML private ImageView Defaultview;
    @FXML private TextField usernameTextfield;
    @FXML private Label lobbyIdLabelField;
    @FXML private TextField lobbyIdTextField;
    @FXML private Label lobbyIdLabelFielderr1;
    @FXML private Label lobbyIdLabelFielderr2;
    @FXML private Label lobbyIdLabelFielderr3;
    @FXML private BorderPane borderPane;
    private double xOffset;
    private double yOffset;
    private Scene scene;
    public static ChatController con;
    
    private static Socket clientSocket = null;
	private static OutputStream outputStream = null;
	private static DataInputStream inputStream = null;
	private static BufferedReader inputLine = null;

    private static UserController instance;

    public UserController() {
        instance = this;
    }

    public static UserController getInstance() {
        return instance;
    }
    public void loginButtonAction() throws IOException {
    	int serverOutputLength = 0; 
		byte[] serverOutput = null;
		if (usernameTextfield.getText().equals("")) {
			lobbyIdLabelFielderr1.setVisible(false);
			lobbyIdLabelFielderr2.setVisible(false);
			lobbyIdLabelFielderr3.setVisible(true);
		}
		else {
			try {
				clientSocket = new Socket("202.92.144.45", 80);
				outputStream = clientSocket.getOutputStream();
				inputStream = new DataInputStream(clientSocket.getInputStream());
				int createLobby = 0;
				String lobbyId = lobbyIdTextField.getText();
				CreateLobbyPacket receivedCL = null;
				ConnectPacket connectPacket = null;
				ConnectPacket receivedC = null;
				ErrLdnePacket lobbyNotFound = null;
				if (!lobbyIdTextField.isVisible()) {
					createLobby = 1;
						
					CreateLobbyPacket createLobbyInit = CreateLobbyPacket.newBuilder()
							.setType(PacketType.CREATE_LOBBY)
							.setMaxPlayers(4)
							.build();
					outputStream.write(createLobbyInit.toByteArray());
						
					serverOutput = new byte[0];
					while(serverOutputLength == 0 ) {
						serverOutputLength = inputStream.available();
						serverOutput = new byte[serverOutputLength];
						inputStream.readFully(serverOutput);
					}
						
					receivedCL = TcpPacket.CreateLobbyPacket.parseFrom(serverOutput);
					lobbyId = receivedCL.getLobbyId();
				}
				
		        String username = usernameTextfield.getText();
	
		        Player newPlayer = Player.newBuilder()
						.setName(username)
						.build();
				
				connectPacket = ConnectPacket.newBuilder()
						.setType(PacketType.CONNECT)
						.setLobbyId(lobbyId)
						.setPlayer(newPlayer)
						.build();
	
				outputStream.write(connectPacket.toByteArray());
				
				serverOutputLength = 0; // reset to 0
				serverOutput = new byte[0]; // reset byte [] 
				while(serverOutputLength == 0 ) {
					serverOutputLength = inputStream.available();
					serverOutput = new byte[serverOutputLength];
					inputStream.readFully(serverOutput);
				}
				TcpPacket receivedPacket = TcpPacket.parseFrom(serverOutput);
				
				if (receivedPacket.getType() == PacketType.CONNECT){
					receivedC = TcpPacket.ConnectPacket.parseFrom(serverOutput);
					if (receivedC.isInitialized()){
						FXMLLoader fmxlLoader = new FXMLLoader(getClass().getClassLoader().getResource("ChatView.fxml"));
				        Parent window = (Pane) fmxlLoader.load();
				        con = fmxlLoader.<ChatController>getController();
				        con.setLobby(lobbyId);
				        this.scene = new Scene(window);
				        this.showScene();
				        Tcp listener = new Tcp(username, clientSocket, outputStream, inputStream, newPlayer, con);
					}else{
						lobbyIdLabelFielderr1.setVisible(false);
						lobbyIdLabelFielderr2.setVisible(true);
						lobbyIdLabelFielderr3.setVisible(false);
						clientSocket.close();
					}
				}else if(receivedPacket.getType() == PacketType.ERR_LDNE || receivedPacket.getType() == PacketType.ERR_LFULL || receivedPacket.getType() == PacketType.ERR){
					lobbyIdLabelFielderr1.setVisible(false);
					lobbyIdLabelFielderr2.setVisible(true);
					lobbyIdLabelFielderr3.setVisible(false);
					clientSocket.close();
				}
			}
			catch (UnknownHostException e) {
				lobbyIdLabelFielderr1.setVisible(true);
				lobbyIdLabelFielderr2.setVisible(false);
				lobbyIdLabelFielderr3.setVisible(false);
				clientSocket.close();
			} catch(IOException e){
				lobbyIdLabelFielderr1.setVisible(true);
				lobbyIdLabelFielderr2.setVisible(false);
				lobbyIdLabelFielderr3.setVisible(false);
				clientSocket.close();
			}
		}
		
    }
    
    public void showLobby() throws IOException {
    	lobbyIdLabelField.setVisible(true);
    	lobbyIdTextField.setVisible(true);
    }
    
    public void hideLobby() throws IOException {
    	lobbyIdLabelField.setVisible(false);
    	lobbyIdTextField.setVisible(false);       
    }

    public void showScene() throws IOException {
        Platform.runLater(() -> {
            Stage stage = (Stage) usernameTextfield.getScene().getWindow();
            stage.setResizable(true);
            stage.setWidth(600);
            stage.setHeight(620);

            stage.setOnCloseRequest((WindowEvent e) -> {
                Platform.exit();
                System.exit(0);
            });
            stage.setScene(this.scene);
            stage.setMinWidth(600);
            stage.setMinHeight(620);
            stage.centerOnScreen();
            con.setUsernameLabel(usernameTextfield.getText());
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        /* Drag and Drop */
        borderPane.setOnMousePressed(event -> {
            xOffset = Main.getPrimaryStage().getX() - event.getScreenX();
            yOffset = Main.getPrimaryStage().getY() - event.getScreenY();
            borderPane.setCursor(Cursor.CLOSED_HAND);
        });

        borderPane.setOnMouseDragged(event -> {
        	Main.getPrimaryStage().setX(event.getScreenX() + xOffset);
        	Main.getPrimaryStage().setY(event.getScreenY() + yOffset);

        });

        borderPane.setOnMouseReleased(event -> {
            borderPane.setCursor(Cursor.DEFAULT);
        });

    }

    /* Terminates Application */
    public void closeSystem(){
        Platform.exit();
        System.exit(0);
    }

    /* This displays an alert message to the user */
    public void showErrorDialog(String message) {
        Platform.runLater(()-> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning!");
            alert.setHeaderText(message);
            alert.setContentText("Please check for firewall issues and check if the server is running.");
            alert.showAndWait();
        });

    }
}