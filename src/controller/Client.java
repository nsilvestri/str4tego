package controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Observer;

import gui.GameObserver;
import javafx.application.Application;
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
		 
		clientSocket = new Socket(serverAddress, serverPort);
		System.out.println("connected to server");
		outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
		System.out.println("connected to server");
		inFromServer = new ObjectInputStream(clientSocket.getInputStream());
		System.out.println("connected to server");
		
		
		connectionStatus.setText("Connected to server. Waiting for game to start.");
		
		gameObserver = new GameObserver();
		// window.setCenter((Node) gameObserver);
	}
}
