package model;

public class InitializePacket extends Packet {

	private static final long serialVersionUID = 1L;
	private Team team;

	public InitializePacket(Team team) {
		super(PacketType.INITIALIZE_GAME);
		this.team = team;
	}

	public Team getTeam() {
		return team;
	}
}
