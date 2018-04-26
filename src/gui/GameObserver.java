package gui;

import java.util.HashMap;
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
import model.Packet;
import model.PacketType;
import model.Piece;
import model.Rank;
import model.ClientReadyPacket;
import model.Square;
import model.StrategoGame;
import model.Team;

public class GameObserver extends BorderPane implements Observer {

	private StrategoGame game;

	private Canvas canvas;
	private GraphicsContext gc;
	private int sqSize = 40;

	private VBox rightMenu;
	private Label teamColorLabel;
	private GridPane pieceButtons;
	private Label connectionStatus;

	private Button eightButton;
	private Button sevenButton;
	private Button sixButton;
	private Button fiveButton;
	private Button fourButton;
	private Button threeButton;
	private Button twoButton;
	private Button bombButton;
	private Button flagButton;

	private Label whoseTurn;

	private Piece placingPiece;

	private static HashMap<Rank, Integer> maxPieces;
	private HashMap<Rank, Integer> canPlacePieces;
	private HashMap<Rank, Button> rankButtonMap;

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

		initializeMaps();

		connectionStatus = new Label("Connecting to server...");
		rightMenu.getChildren().add(connectionStatus);

		whoseTurn = new Label();
		rightMenu.getChildren().add(whoseTurn);

		this.setRight(rightMenu);
	}

	private void initializeMaps() {
		maxPieces = new HashMap<Rank, Integer>();
		maxPieces.put(Rank.EIGHT, 1);
		maxPieces.put(Rank.SEVEN, 1);
		maxPieces.put(Rank.SIX, 2);
		maxPieces.put(Rank.FIVE, 2);
		maxPieces.put(Rank.FOUR, 3);
		maxPieces.put(Rank.THREE, 2);
		maxPieces.put(Rank.TWO, 3);
		maxPieces.put(Rank.BOMB, 3);
		maxPieces.put(Rank.FLAG, 1);

		canPlacePieces = new HashMap<Rank, Integer>(maxPieces);

		rankButtonMap = new HashMap<Rank, Button>();
		rankButtonMap.put(Rank.EIGHT, eightButton);
		rankButtonMap.put(Rank.SEVEN, sevenButton);
		rankButtonMap.put(Rank.SIX, sixButton);
		rankButtonMap.put(Rank.FIVE, fiveButton);
		rankButtonMap.put(Rank.FOUR, fourButton);
		rankButtonMap.put(Rank.THREE, threeButton);
		rankButtonMap.put(Rank.TWO, twoButton);
		rankButtonMap.put(Rank.BOMB, bombButton);
		rankButtonMap.put(Rank.FLAG, flagButton);
	}

	private void initializePieceButtons() {
		pieceButtons = new GridPane();

		eightButton = new Button("8");
		sevenButton = new Button("7");
		sixButton = new Button("6");
		fiveButton = new Button("5");
		fourButton = new Button("4");
		threeButton = new Button("3");
		twoButton = new Button("2");
		bombButton = new Button("B");
		flagButton = new Button("F");

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

		System.out.println("Whose turn: " + game.whoseTurn());
		if (game.whoseTurn() != null) {
			whoseTurn.setText(game.whoseTurn() + "'s turn.");
		}
	}

	/*
	 * Parses clicks on the game board canvas.
	 */
	private class CanvasClickHandler implements EventHandler<MouseEvent> {

		@Override
		public void handle(MouseEvent mouse) {
			if (mouse.getEventType() != MouseEvent.MOUSE_CLICKED) {
				return;
			}

			// ignore clicks if a piece isn't being placed
			if (placingPiece == null) {
				return;
			}

			int r = (int) (mouse.getY() / sqSize);
			int c = (int) (mouse.getX() / sqSize);

			// return if the click is out of the bounds
			if (game.getTeam() == Team.RED) {
				if (c < 3 || c > 8 || r < 9 || r > 11) {
					return;
				}
			} else if (game.getTeam() == Team.BLUE) {
				if (c < 3 || c > 8 || r < 0 || r > 2) {
					return;
				}
			} else if (game.getTeam() == Team.GREEN) {
				if (c < 0 || c > 2 || r < 3 || r > 8) {
					return;
				}
			} else if (game.getTeam() == Team.YELLOW) {
				if (c < 9 || c > 11 || r < 3 || r > 8) {
					return;
				}
			}
			game.setPiece(placingPiece, r, c);

			// decrement the number of available pieces
			canPlacePieces.put(placingPiece.getRank(), canPlacePieces.get(placingPiece.getRank()) - 1);

			// disable buttons if the piece limit is reached
			if (canPlacePieces.get(placingPiece.getRank()) == 0) {
				rankButtonMap.get(placingPiece.getRank()).setDisable(true);
			}
			placingPiece = null;

			// check if that was the last piece placed, and send a ready packet
			// if so
			int piecesRemaining = 0;
			for (int i : canPlacePieces.values()) {
				piecesRemaining += i;
			}
			if (piecesRemaining == 0) {
				game.sendPacket(new ClientReadyPacket(game.getTeam(), game.getBoard()));
			}

		}
	}

	private class PieceButtonHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent ae) {
			Button b = (Button) ae.getSource();

			if (b.getText().contains("8")) {
				placingPiece = new Piece(Rank.EIGHT, game.getTeam());
			} else if (b.getText().contains("7")) {
				placingPiece = new Piece(Rank.SEVEN, game.getTeam());
			} else if (b.getText().contains("6")) {
				placingPiece = new Piece(Rank.SIX, game.getTeam());
			} else if (b.getText().contains("5")) {
				placingPiece = new Piece(Rank.FIVE, game.getTeam());
			} else if (b.getText().contains("4")) {
				placingPiece = new Piece(Rank.FOUR, game.getTeam());
			} else if (b.getText().contains("3")) {
				placingPiece = new Piece(Rank.THREE, game.getTeam());
			} else if (b.getText().contains("2")) {
				placingPiece = new Piece(Rank.TWO, game.getTeam());
			} else if (b.getText().contains("B")) {
				placingPiece = new Piece(Rank.BOMB, game.getTeam());
			} else if (b.getText().contains("F")) {
				placingPiece = new Piece(Rank.FLAG, game.getTeam());
			}
		}
	}
}
