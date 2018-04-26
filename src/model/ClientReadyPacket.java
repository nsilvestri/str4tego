package model;

public class ClientReadyPacket extends Packet {
	private static final long serialVersionUID = 1L;
	private Square[][] board;

	public ClientReadyPacket(Team source, Square[][] board) {
		super(PacketType.READY, source);
		this.board = board;
	}

	public Square[][] getBoard() {
		return board;
	}
}
