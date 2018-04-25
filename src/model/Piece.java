package model;

public class Piece {

	private Rank rank;
	private Team team;

	public Piece(Rank rank, Team team) {
		this.rank = rank;
		this.team = team;
	}

	public Rank getRank() {
		return rank;
	}
	
	public Team getTeam() {
		return team;
	}
}
