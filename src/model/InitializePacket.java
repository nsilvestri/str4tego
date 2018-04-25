package model;

public class InitializePacket extends Packet {

	private static final long serialVersionUID = 1L;
	private int clientNum;

	public InitializePacket(int clientNum) {
		super(PacketType.INITIALIZE_GAME);
		this.clientNum = clientNum;
	}

	public int getClientNum() {
		return clientNum;
	}
}
