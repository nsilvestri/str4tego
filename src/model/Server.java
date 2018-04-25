package model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Server extends Application {

	private final static int serverPort = 9998;
	private static ServerSocket serverSocket;
	private static Socket[] connectedClients;
	private static ObjectOutputStream[] clientOutputStreams;
	private static ObjectInputStream[] clientInputStreams;

	public static void main(String[] args) throws UnknownHostException, IOException {

		serverSocket = new ServerSocket(serverPort);

		connectedClients = new Socket[4];
		clientOutputStreams = new ObjectOutputStream[4];
		clientInputStreams = new ObjectInputStream[4];

		launch(args);
	}

	private BorderPane window;
	private Label[] connectionStatuses;

	@Override
	public void start(Stage primaryStage) throws Exception {

		window = new BorderPane();

		primaryStage.setTitle("Str4tego Server");
		Scene scene = new Scene(window, 300, 200);
		primaryStage.setScene(scene);
		primaryStage.show();

		VBox statusLabels = new VBox();
		connectionStatuses = new Label[4];
		for (int i = 0; i < 4; i++) {
			connectionStatuses[i] = new Label("Client " + (i + 1) + " not connected.");
			statusLabels.getChildren().add(connectionStatuses[i]);
		}

		window.setCenter(statusLabels);

		connectClients();
	}

	private void connectClients() throws IOException {
		ClientConnector cc = new ClientConnector();
		Thread thread = new Thread(cc);
		thread.start();
	}

	private class ClientConnector implements Runnable {

		@Override
		public void run() {
			for (int i = 0; i < 4; i++) {
				try {
					connectedClients[i] = serverSocket.accept();
					clientOutputStreams[i] = new ObjectOutputStream(connectedClients[i].getOutputStream());
					clientInputStreams[i] = new ObjectInputStream(connectedClients[i].getInputStream());
					Platform.runLater(() -> {
						for (int j = 0; j < 4; j++) {
							if (connectedClients[j] != null) {
								connectionStatuses[j].setText("Client " + (j + 1) + " connected.");
							}
						}
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			/* All clients connected; tell them their team */
			try {
				clientOutputStreams[0].writeObject(new InitializePacket(Team.RED));
				clientOutputStreams[1].writeObject(new InitializePacket(Team.GREEN));
				clientOutputStreams[2].writeObject(new InitializePacket(Team.BLUE));
				clientOutputStreams[3].writeObject(new InitializePacket(Team.YELLOW));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
