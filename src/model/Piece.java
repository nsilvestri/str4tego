package model;

import java.io.Serializable;

public class Piece implements Serializable {

	private static final long serialVersionUID = 1L;

	private Rank rank;
	private Team team;

	public Piece(Rank rank, Team team) {
		this.rank = rank;
		this.team = team;
	}

	public Rank getRank() {
		return rank;
	}

	public void setRank(Rank rank) {
		this.rank = rank;
	}

	public Team getTeam() {
		return team;
	}
}
