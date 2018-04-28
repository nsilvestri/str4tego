package model;

public class MovePacket extends Packet
{

	private static final long serialVersionUID = 1L;

	private Pair<Integer, Integer> coords;
	private Direction direction;
	private boolean wonSuccessful;

	public MovePacket(Pair<Integer, Integer> coords, Direction direction, Team source)
	{
		super(PacketType.MOVE, source);
		this.coords = coords;
		this.direction = direction;
	}

	public Pair<Integer, Integer> getCoords()
	{
		return coords;
	}

	public Direction getDirection()
	{
		return direction;
	}

	public void setSuccessful(boolean b)
	{
		wonSuccessful = b;
	}

	public boolean isSuccessful()
	{
		return wonSuccessful;
	}
}
