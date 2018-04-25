package model;

import java.io.Serializable;

public abstract class Packet implements Serializable {

	private static final long serialVersionUID = 1L;

	PacketType type;

	public Packet(PacketType type) {
		this.type = type;
	}

	public PacketType getPacketType() {
		return type;
	}
}
