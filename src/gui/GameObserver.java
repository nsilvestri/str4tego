package gui;

import java.util.Observable;
import java.util.Observer;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import model.Square;
import model.StrategoGame;

public class GameObserver extends BorderPane implements Observer {

	private StrategoGame game;

	private Canvas canvas;
	private GraphicsContext gc;

	public GameObserver(StrategoGame game) {
		this.game = game;
		canvas = new Canvas(500, 500);
		this.setCenter(canvas);
		gc = canvas.getGraphicsContext2D();
	}

	@Override
	public void update(Observable o, Object arg1) {
		this.game = (StrategoGame) o;

		Square[][] board = game.getBoard();

		int sqSize = 40;

		for (int r = 0; r < board.length; r++) {
			for (int c = 0; c < board[r].length; c++) {

				Square thisSquare = board[r][c];

				gc.setFill(Color.BLACK);
				gc.setStroke(Color.BLACK);
				if (!thisSquare.isMoveable()) {
					gc.fillRect(c * sqSize, r * sqSize, sqSize, sqSize);
				} else {
					gc.strokeRect(c * sqSize, r * sqSize, sqSize, sqSize);
				}

				if (thisSquare.isOccupied()) {
					gc.setFill(Color.RED);
					int margin = 3;
					gc.fillRect(c * sqSize + margin, r * sqSize + margin, sqSize - (margin * 2), sqSize - (margin * 2));
					gc.setFill(Color.BLUE);
					gc.setFont(new Font("Garamond", 32));
					gc.fillText(thisSquare.getOccupied().getRank().getSymbol(), c * sqSize + 13,
							(r + 1) * sqSize - 10);
				}
			}
		}
	}

}
