package battlecity;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import proto.TcpPacketProtos.TcpPacket.CreateLobbyPacket;
import proto.TcpPacketProtos.TcpPacket.PacketType;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import java.io.OutputStream;

import proto.PlayerProtos.Player;
import proto.TcpPacketProtos.TcpPacket;
import proto.TcpPacketProtos.TcpPacket.ConnectPacket;
import proto.TcpPacketProtos.TcpPacket.ErrLdnePacket;

public class UserController extends Pane implements Initializable {
    @FXML private ImageView Defaultview;
    @FXML private TextField usernameTextfield;
    @FXML private Label lobbyIdLabelField;
    @FXML private TextField lobbyIdTextField;
    @FXML private Label playerNumberLabelField; 		// Player number
    @FXML private TextField playerNumberTextField; 	// Player number
    @FXML private Label ipAddTextLabel; 	// IP Add for UDP
    @FXML private TextField ipAddTextField; 	// IP Add for UDP
    @FXML private Label portTextLabel; 	// IP Add for UDP
    @FXML private TextField portTextField; 	// Port number for UDP
    @FXML private Label lobbyIdLabelFielderr1;
    @FXML private Label lobbyIdLabelFielderr2;
    @FXML private Label lobbyIdLabelFielderr3;
    @FXML private Label lobbyIdLabelFielderr4; // Player number
    @FXML private Label ipMissingWarning;
    @FXML private Label portMissingWarning;
    @FXML private BorderPane borderPane;
    
    @FXML ToggleGroup tgCommand; 
    
    private double xOffset;
    private double yOffset;
    private Scene scene;
    public static ChatController con;
    
    private static Socket clientSocket = null;
	private static OutputStream outputStream = null;
	private static DataInputStream inputStream = null;
	private static BufferedReader inputLine = null;

    private static UserController instance;
    private Main application;
 
    public UserController() {
        instance = this;
    }

    public static UserController getInstance() {
        return instance;
    }
    
    void setApp(Main application) {
    	this.application = application;
    }

    public void loginButtonAction() throws IOException {
    	int serverOutputLength = 0; 
		byte[] serverOutput = null;
		RadioButton selected = (RadioButton) tgCommand.getSelectedToggle();
		String toggleGroupValue = selected.getId();
		
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
				CreateLobbyPacket receivedCL = null;
				ConnectPacket connectPacket = null;
				ConnectPacket receivedC = null;
				ErrLdnePacket lobbyNotFound = null;
				String lobbyId = "";
				
//				Check radio button: create or join
				if (toggleGroupValue.equals("createLobbyRb")) {
					int numPlayers = Integer.parseInt(playerNumberTextField.getText());
//					Since game specs limited max number of players to 4
					if (numPlayers > 4) {
						throw new Exception("MORE_THAN");
					}
					CreateLobbyPacket createLobbyInit = CreateLobbyPacket.newBuilder()
							.setType(PacketType.CREATE_LOBBY)
							.setMaxPlayers(numPlayers)
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
				}else if (toggleGroupValue.equals("joinLobbyRb")) {
					lobbyId = lobbyIdTextField.getText();
				}
				
				if (ipAddTextField.getText().length() == 0) {
					throw new Exception("MISSING_IP_ADD");
				}
				
				if (portTextField.getText().length() == 0) {
					throw new Exception("MISSING_PORT_NUM");
				}
				
				String ipAdd = ipAddTextField.getText();
				int port = Integer.parseInt(portTextField.getText());

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
//						main application will be modifying the scene in openChat
				        application.setIsAuth(true);
				        application.openChat(receivedC, username, clientSocket, outputStream, inputStream, newPlayer, lobbyId, ipAdd, port);				        
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
				lobbyIdLabelFielderr4.setVisible(false);
				ipMissingWarning.setVisible(false);
				portMissingWarning.setVisible(false);
				clientSocket.close();
			} catch(IOException e){
				lobbyIdLabelFielderr1.setVisible(true);
				lobbyIdLabelFielderr2.setVisible(false);
				lobbyIdLabelFielderr3.setVisible(false);
				lobbyIdLabelFielderr4.setVisible(false);
				ipMissingWarning.setVisible(false);
				portMissingWarning.setVisible(false);
				clientSocket.close();
			} catch (Exception e) {
				if (e.getMessage().equals("MORE_THAN")) {
					lobbyIdLabelFielderr1.setVisible(false);
					lobbyIdLabelFielderr2.setVisible(false);
					lobbyIdLabelFielderr3.setVisible(false);
					lobbyIdLabelFielderr4.setVisible(true);
					ipMissingWarning.setVisible(false);
					portMissingWarning.setVisible(false);
				}else if (e.getMessage().equals("MISSING_IP_ADD")) {
					lobbyIdLabelFielderr1.setVisible(false);
					lobbyIdLabelFielderr2.setVisible(false);
					lobbyIdLabelFielderr3.setVisible(false);
					lobbyIdLabelFielderr4.setVisible(false);
					ipMissingWarning.setVisible(true);
					portMissingWarning.setVisible(false);
				}else if (e.getMessage().equals("MISSING_PORT_NUM")) {
					lobbyIdLabelFielderr1.setVisible(false);
					lobbyIdLabelFielderr2.setVisible(false);
					lobbyIdLabelFielderr3.setVisible(false);
					lobbyIdLabelFielderr4.setVisible(false);
					ipMissingWarning.setVisible(false);
					portMissingWarning.setVisible(true);

				}
			}
		}
		
    }
    
    public void showLobby() throws IOException {
    	lobbyIdLabelField.setVisible(true);
    	lobbyIdTextField.setVisible(true);
    	playerNumberLabelField.setVisible(false);
    	playerNumberTextField.setVisible(false);
    }
    
    public void hideLobby() throws IOException {
    	lobbyIdLabelField.setVisible(false);
    	lobbyIdTextField.setVisible(false);
    	playerNumberLabelField.setVisible(true);
    	playerNumberTextField.setVisible(true);       
    }

    public void showScene() throws IOException {
        Platform.runLater(() -> {
            Stage stage = (Stage) usernameTextfield.getScene().getWindow();
            stage.setResizable(true);
            stage.setWidth(1200);
            stage.setHeight(620);

            stage.setOnCloseRequest((WindowEvent e) -> {
                Platform.exit();
                System.exit(0);
            });
            stage.setScene(this.scene);
            stage.setMinWidth(1000);
            stage.setMinHeight(620);
            stage.centerOnScreen();
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	BackgroundSize bSize = new BackgroundSize(750, 750, false, false, false, false);
    	Background background2 = new Background(new BackgroundImage(new Image((InputStream) getClass().getClassLoader().getResourceAsStream("instructions.png")),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                bSize));
    	borderPane.setBackground(background2);

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