package controller;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Observer;

import gui.GameObserver;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.StrategoGame;

public class Client extends Application
{

	public static void main(String[] args) throws UnknownHostException, IOException
	{
		launch(args);
	}

	private StrategoGame game;
	private Observer gameObserver;

	private BorderPane window;

	@Override
	public void start(Stage primaryStage) throws Exception
	{

		window = new BorderPane();

		game = new StrategoGame();
		gameObserver = new GameObserver(game);
		game.addObserver(gameObserver);
		game.setChangedAndNotifyObservers();
		window.setLeft((Node) gameObserver);

		primaryStage.setTitle("Str4tego");
		Scene scene = new Scene(window, 700, 500);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
