package controller;

import java.util.Observer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.StrategoGame;

public class Main extends Application {

	public static void main(String[] args) {
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
