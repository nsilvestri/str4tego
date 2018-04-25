package gui;

import java.util.Observable;
import java.util.Observer;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import model.Square;
import model.StrategoGame;

public class GameObserver extends BorderPane implements Observer {

	private StrategoGame game;

	private Canvas canvas;
	private GraphicsContext gc;
	private int sqSize = 40;

	private Label connectionStatus;

	public GameObserver(StrategoGame game) {
		this.game = game;
		canvas = new Canvas(480, 480);
		this.setCenter(canvas);
		canvas.setOnMouseClicked(new CanvasClickHandler());
		gc = canvas.getGraphicsContext2D();

		connectionStatus = new Label("Connecting to server...");
		this.setBottom(connectionStatus);
	}

	@Override
	public void update(Observable o, Object arg1) {
		this.game = (StrategoGame) o;

		/*
		 * not sure why this needs a Platform.runLater() but otherwise there
		 * are NotOnFXApplicationThread exceptions.
		 */
		if (game.isConnectedToServer()) {
			Platform.runLater(() -> {
				connectionStatus.setText("Connected to server.");
			});
		}

		Square[][] board = game.getBoard();

		/* draw the board */
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
					gc.setFill(Color.WHITE);
					gc.setFont(new Font("Garamond", 32));
					gc.fillText(thisSquare.getOccupied().getRank().getSymbol(), c * sqSize + 13, (r + 1) * sqSize - 10);
				}
			}
		}
	}

	private class CanvasClickHandler implements EventHandler<MouseEvent> {

		@Override
		public void handle(MouseEvent mouse) {
			if (mouse.getEventType() != MouseEvent.MOUSE_CLICKED) {
				return;
			}

			int r = (int) (mouse.getY() / sqSize);
			int c = (int) (mouse.getX() / sqSize);

			System.out.printf("Mouse click at [%d, %d]\n", r, c);
		}

	}
}
