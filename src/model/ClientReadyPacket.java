package model;

public class ClientReadyPacket extends Packet {
	private static final long serialVersionUID = 1L;
	private Team team;
	private Square[][] board;
	
	public ClientReadyPacket(Team team, Square[][] board) {
		super(PacketType.READY);
		this.team = team;
		this.board = board;
	}
	
	public Team getTeam() {
		return team;
	}
	
	public Square[][] getBoard() {
		return board;
	}
}
