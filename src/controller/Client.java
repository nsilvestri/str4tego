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

	}
}
