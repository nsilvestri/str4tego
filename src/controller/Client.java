package controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Observer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.StrategoGame;

public class Client extends Application {

	private final static String serverAddress = "localhost";
	private final static int serverPort = 9998;
	private static Socket clientSocket;
	private static ObjectOutputStream out;
	private static ObjectInputStream in;
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		clientSocket = new Socket(serverAddress, serverPort);
		
		out = new ObjectOutputStream(clientSocket.getOutputStream());
		in = new ObjectInputStream(clientSocket.getInputStream());
		
		launch(args);
	}

	private StrategoGame game;
	private Observer gameObserver;

	private BorderPane window;

	@Override
	public void start(Stage primaryStage) throws Exception {

		window = new BorderPane();

		primaryStage.setTitle("Str4tego");
		Scene scene = new Scene(window, 600, 600);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

}
