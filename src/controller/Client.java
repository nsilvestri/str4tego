package controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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

		primaryStage.setTitle("Str4tego");
		window = new BorderPane();
		Scene scene = new Scene(window, 600, 600);
		primaryStage.setScene(scene);
		primaryStage.show();

		Label connectionStatus = new Label("Connecting to server...");
		window.setCenter(connectionStatus);

		clientSocket = new Socket();
		clientSocket.connect(new InetSocketAddress(serverAddress, serverPort));
		outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
		inFromServer = new ObjectInputStream(clientSocket.getInputStream());

		connectionStatus.setText("Connected to server. Waiting for game to start.");

		// start a thread that reads packets from the socket and puts them in the buffer
		Thread packetReader = new Thread(() -> {
			try {
				while (true) {
					Packet p = (Packet) inFromServer.readObject();
					System.out.println(p);
					packetBuffer.add(p);
				}
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		});
		packetReader.start();

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

		
		game = new StrategoGame();
		gameObserver = new GameObserver(game);
		window.setLeft((Node) gameObserver);
	}
	
	private void parsePacket(Packet p) {
		
	}
}
