package controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observer;

import gui.GameObserver;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.StrategoGame;

public class Client extends Application {

	private final static String serverAddress = "localhost";
	private final static int serverPort = 9998;
	private static Socket clientSocket;
	private static ObjectOutputStream outToServer;
	private static ObjectInputStream inFromServer;

	private static ArrayList<Packet> packetBuffer = new ArrayList<Packet>();

	public static void main(String[] args) throws UnknownHostException, IOException {
		launch(args);
	}

	private StrategoGame game;
	private Observer gameObserver;

	private BorderPane window;

	@Override
	public void start(Stage primaryStage) throws Exception {

		window = new BorderPane();
		
		game = new StrategoGame();
		gameObserver = new GameObserver(game);
		game.addObserver(gameObserver);
		game.setChangedAndNotifyObservers();
		window.setCenter((Node) gameObserver);

		primaryStage.setTitle("Str4tego");
		Scene scene = new Scene(window, 600, 600);
		primaryStage.setScene(scene);
		primaryStage.show();

		Label connectionStatus = new Label("Connecting to server...");
		window.setBottom(connectionStatus);

		// start a thread that continuously attempts to connect to the server
		Thread serverConnector = new Thread(() -> {

			while (clientSocket == null || !clientSocket.isConnected()) {
				try {
					clientSocket = new Socket();
					clientSocket.connect(new InetSocketAddress(serverAddress, serverPort));
				} catch (IOException ioe) {
					// TODO: Empty Catch
				}

				// hopefully stops the server from thinking it's getting DOS'd
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// initialize streams
			try {
				outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
				inFromServer = new ObjectInputStream(clientSocket.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}

			// start a thread that reads packets from the socket and puts them
			// in the buffer
			Thread packetReader = new Thread(() -> {
				try {
					while (true) {
						Packet p = (Packet) inFromServer.readObject();
						packetBuffer.add(p);
					}
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
			});
			packetReader.start();

			// Server is connected, update status label
			Platform.runLater(() -> {
				connectionStatus.setText("Connected to Server.");
			});
		});
		serverConnector.start();

		// start a timer that will parse packets 60 times per second
		AnimationTimer at = new AnimationTimer() {
			@Override
			public void handle(long now) {
				while (!packetBuffer.isEmpty()) {
					parsePacket(packetBuffer.remove(0));
				}
			}
		};
		at.start();
	}

	private void parsePacket(Packet p) {

	}
}
