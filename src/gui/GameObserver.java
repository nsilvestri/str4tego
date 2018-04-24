package gui;

import java.util.Observable;
import java.util.Observer;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import model.Square;
import model.StrategoGame;

public class GameObserver extends BorderPane implements Observer {

	private StrategoGame game;
	
	private Canvas canvas;
	private GraphicsContext gc;
	
	public GameObserver(StrategoGame game) {
		this.game = game;
		canvas = new Canvas(500, 500);
		gc = canvas.getGraphicsContext2D();
	}
	
	@Override
	public void update(Observable o, Object arg1) {
		this.game = (StrategoGame) o;
	
		Square[][] board = game.getBoard();
		
		int sqSize = 20;
		
		for (int r = 0; r < board.length; r++) {
			for (int c = 0; c < board[r].length; c++) {
				System.out.println("redrawing canvas");
				gc.setStroke(Color.BLACK);
				gc.strokeRect(c * sqSize, r * sqSize, sqSize, sqSize);
			}
		}
	}

}
