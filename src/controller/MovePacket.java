package controller;

public class MovePacket extends Packet {

	private static final long serialVersionUID = 1L;
	
	private Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> move;
	
	public MovePacket(PacketType type, Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> move) {
		super(type);
		this.move = move;
	}
	
	public Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> getMove() {
		return move;
	}
}
