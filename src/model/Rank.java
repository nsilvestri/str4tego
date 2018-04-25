package model;

public enum Rank {
	FLAG, EIGHT, SEVEN, SIX, FIVE, FOUR, THREE, TWO, BOMB, UNKNOWN;

	public String getSymbol() {
		switch (this) {
			case FLAG :
				return "F";
			case EIGHT :
				return "8";
			case SEVEN :
				return "7";
			case SIX :
				return "6";
			case FIVE :
				return "5";
			case FOUR :
				return "4";
			case THREE :
				return "3";
			case TWO :
				return "2";
			case BOMB :
				return "B";
			case UNKNOWN :
				return "";
			default :
				return "?";
		}
	}
}
