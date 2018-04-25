package gui;

import java.util.Observable;
import java.util.Observer;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import model.Piece;
import model.Rank;
import model.Square;
import model.StrategoGame;

public class GameObserver extends BorderPane implements Observer {

	private StrategoGame game;

	private Canvas canvas;
	private GraphicsContext gc;
	private int sqSize = 40;

	private VBox rightMenu;
	private Label teamColorLabel;
	private GridPane pieceButtons;
	private Label connectionStatus;
	
	private Piece placingPiece;
	

	public GameObserver(StrategoGame game) {
		this.game = game;
		canvas = new Canvas(480, 480);
		BorderPane.setMargin(canvas, new Insets(10));
		this.setCenter(canvas);
		canvas.setOnMouseClicked(new CanvasClickHandler());
		gc = canvas.getGraphicsContext2D();
		
		rightMenu = new VBox();
		teamColorLabel = new Label("Your Team: " + game.getTeam().toString());
		rightMenu.getChildren().add(teamColorLabel);
		
		initializePieceButtons();
		
		rightMenu.getChildren().add(pieceButtons);

		connectionStatus = new Label("Connecting to server...");
		rightMenu.getChildren().add(connectionStatus);
		this.setRight(rightMenu);
	}
	
	private void initializePieceButtons() {
		pieceButtons = new GridPane();
		
		Button eightButton = new Button("8");
		Button sevenButton = new Button("7");
		Button sixButton = new Button("6");
		Button fiveButton = new Button("5");
		Button fourButton = new Button("4");
		Button threeButton = new Button("3");
		Button twoButton = new Button("2");
		Button bombButton = new Button("B");
		Button flagButton = new Button("F");
		
		PieceButtonHandler pbh = new PieceButtonHandler();
		eightButton.setOnAction(pbh);
		sevenButton.setOnAction(pbh);
		sixButton.setOnAction(pbh);
		fiveButton.setOnAction(pbh);
		fourButton.setOnAction(pbh);
		threeButton.setOnAction(pbh);
		twoButton.setOnAction(pbh);
		bombButton.setOnAction(pbh);
		flagButton.setOnAction(pbh);
		
		pieceButtons.add(eightButton, 0, 0);
		pieceButtons.add(sevenButton, 1, 0);
		pieceButtons.add(sixButton, 0, 1);
		pieceButtons.add(fiveButton, 1, 1);
		pieceButtons.add(fourButton, 0, 2);
		pieceButtons.add(threeButton, 1, 2);
		pieceButtons.add(twoButton, 0, 3);
		pieceButtons.add(bombButton, 1, 3);
		pieceButtons.add(flagButton, 0, 4);
	}

	@Override
	public void update(Observable o, Object arg1) {
		this.game = (StrategoGame) o;

		/*
		 * not sure why this needs a Platform.runLater() but otherwise there are
		 * NotOnFXApplicationThread exceptions.
		 */
		if (game.isConnectedToServer()) {
			Platform.runLater(() -> {
				connectionStatus.setText("Connected to server.");
			});
		}
		
		teamColorLabel.setText("Team: " + game.getTeam().toString());

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
					gc.setFill(thisSquare.getOccupied().getTeam().getColor());
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
			
			if (placingPiece == null) {
				return;
			}

			int r = (int) (mouse.getY() / sqSize);
			int c = (int) (mouse.getX() / sqSize);
			
			System.out.println(placingPiece);
			game.setPiece(placingPiece, r, c);
			
			placingPiece = null;

			System.out.printf("Mouse click at [%d, %d]\n", r, c);
		}
	}
	
	private class PieceButtonHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent ae) {
			Button b = (Button) ae.getSource();
			
			placingPiece = new Piece(Rank.EIGHT, game.getTeam());
		}
		
	}
}
