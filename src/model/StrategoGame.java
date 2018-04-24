package model;

import java.util.Observable;
import java.util.Observer;

public class StrategoGame extends Observable {

	private Square[][] board;

	public StrategoGame() {
		initializeBoard();
		setChangedAndNotifyObservers();
	}

	/**
	 * Initializes the board instance and declares which squares are unmovable.
	 */
	private void initializeBoard() {

		board = new Square[12][12];
		
		for (int r = 0; r < 12; r++) {
			for (int c = 0; c < 12; c++) {
				board[r][c] = new Square();
			}
		}

		/* Make certain Squares of the board unmovable */

		// top left 3x3 grid
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 3; c++) {
				board[r][c].setMovable(false);
			}
		}

		// top right 3x3 grid
		for (int r = 0; r < 3; r++) {
			for (int c = 9; c < 12; c++) {
				board[r][c].setMovable(false);
			}
		}

		// bottom left 3x3 grid
		for (int r = 9; r < 12; r++) {
			for (int c = 0; c < 3; c++) {
				board[r][c].setMovable(false);
			}
		}

		// bottom right 3x3 grid
		for (int r = 9; r < 12; r++) {
			for (int c = 9; c < 12; c++) {
				board[r][c].setMovable(false);
			}
		}

		// center 2x2 grid
		for (int r = 5; r < 7; r++) {
			for (int c = 5; c < 7; c++) {
				board[r][c].setMovable(false);
			}
		}
	}
	
	public Square[][] getBoard() {
		return board;
	}
	
	private void setChangedAndNotifyObservers() {
		setChanged();
		notifyObservers();
	}
}
