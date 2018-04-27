package model;

public class EliminationPacket extends Packet
{
	private static final long serialVersionUID = 1L;
	
	private Team eliminated;
	public EliminationPacket(Team eliminated) 
	{
		super(PacketType.ELIMINATION, Team.SERVER);
		this.eliminated = eliminated;
	}
	
	public Team getEliminated() {
		return eliminated;
	}
}
