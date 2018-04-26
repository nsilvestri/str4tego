package model;

public class AllClientsReadyPacket extends Packet {

	private static final long serialVersionUID = 1L;

	public AllClientsReadyPacket() {
		super(PacketType.ALL_CLIENTS_READY, Team.SERVER);
	}
}
