package model;

public class InitializePacket extends Packet {

	private static final long serialVersionUID = 1L;

	public InitializePacket(Team source) {
		super(PacketType.INITIALIZE_GAME, source);
	}
}
