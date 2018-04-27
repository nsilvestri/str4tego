package model;

public enum Rank
{
	FLAG(0), EIGHT(8), SEVEN(7), SIX(6), FIVE(5), FOUR(4), THREE(3), TWO(2), BOMB(10), UNKNOWN(-1);

	private int value;

	private Rank(int value)
	{
		this.value = value;
	}

	public String getSymbol()
	{
		switch (this)
		{
		case FLAG:
			return "F";
		case EIGHT:
			return "8";
		case SEVEN:
			return "7";
		case SIX:
			return "6";
		case FIVE:
			return "5";
		case FOUR:
			return "4";
		case THREE:
			return "3";
		case TWO:
			return "2";
		case BOMB:
			return "B";
		case UNKNOWN:
			return "";
		default:
			return "?";
		}
	}

	public int getValue()
	{
		return value;
	}
}
