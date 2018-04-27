package model;

import java.io.Serializable;

public class Packet implements Serializable
{

	private static final long serialVersionUID = 1L;

	PacketType type;
	Team source;

	public Packet(PacketType type, Team source)
	{
		this.type = type;
		this.source = source;
	}

	public PacketType getPacketType()
	{
		return type;
	}

	public Team getSource()
	{
		return source;
	}

	public void setSource(Team t)
	{
		source = t;
	}
}
