package model;

public class Square {
	
	private Piece occupied;
	private boolean movable;
	
	
	
	public boolean isEmpty() {
		return (occupied == null);
	}
	
	public boolean isMoveable() {
		return movable;
	}
	
	public void setMovable(boolean movable) {
		this.movable = movable;
	}
}
