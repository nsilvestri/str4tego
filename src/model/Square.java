package model;

public class Square {

	private Piece occupied;
	private boolean movable;

	public Square() {
		occupied = null;
		movable = true;
	}

	public boolean isOccupied() {
		return (occupied != null);
	}

	public void setOccupied(Piece p) {
		occupied = p;
	}

	public Piece getOccupied() {
		return occupied;
	}

	public boolean isMoveable() {
		return movable;
	}

	public void setMovable(boolean movable) {
		this.movable = movable;
	}
}
