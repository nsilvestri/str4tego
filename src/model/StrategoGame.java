package model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;

import javafx.animation.AnimationTimer;

public class StrategoGame extends Observable
{

	private Square[][] board;
	private Team team;

	private final static String serverAddress = "localhost";
	private final static int serverPort = 9998;
	private static Socket clientSocket;
	private static ObjectOutputStream outToServer;
	private static ObjectInputStream inFromServer;
	private static ArrayList<Packet> packetBuffer = new ArrayList<Packet>();

	private boolean setup = true;
	private static ArrayList<Team> eliminated = new ArrayList<>();

	private Team turn;

	public StrategoGame()
	{
		initializeBoard();
		team = Team.NONE;
		initializeServerConnection();
		setChangedAndNotifyObservers();
	}

	/**
	 * Initializes the board instance and declares which squares are unmovable.
	 */
	private void initializeBoard()
	{

		board = new Square[12][12];

		for (int r = 0; r < 12; r++)
		{
			for (int c = 0; c < 12; c++)
			{
				board[r][c] = new Square();
			}
		}

		/* Make certain Squares of the board unmovable */

		// top left 3x3 grid
		for (int r = 0; r < 3; r++)
		{
			for (int c = 0; c < 3; c++)
			{
				board[r][c].setMovable(false);
			}
		}

		// top right 3x3 grid
		for (int r = 0; r < 3; r++)
		{
			for (int c = 9; c < 12; c++)
			{
				board[r][c].setMovable(false);
			}
		}

		// bottom left 3x3 grid
		for (int r = 9; r < 12; r++)
		{
			for (int c = 0; c < 3; c++)
			{
				board[r][c].setMovable(false);
			}
		}

		// bottom right 3x3 grid
		for (int r = 9; r < 12; r++)
		{
			for (int c = 9; c < 12; c++)
			{
				board[r][c].setMovable(false);
			}
		}

		// center 2x2 grid
		for (int r = 5; r < 7; r++)
		{
			for (int c = 5; c < 7; c++)
			{
				board[r][c].setMovable(false);
			}
		}
	}

	private void initializeServerConnection()
	{
		// start a thread that continuously attempts to connect to the server
		Thread serverConnector = new Thread(() ->
		{

			while (clientSocket == null || !clientSocket.isConnected())
			{
				try
				{
					clientSocket = new Socket();
					clientSocket.connect(new InetSocketAddress(serverAddress, serverPort));
				}
				catch (IOException ioe)
				{
					// TODO: Empty Catch
				}

				// hopefully stops the server from thinking it's getting DOS'd
				try
				{
					Thread.sleep(200);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}

			// initialize streams
			try
			{
				outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
				inFromServer = new ObjectInputStream(clientSocket.getInputStream());
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			// start a thread that reads packets from the socket and puts them
			// in the buffer
			Thread packetReader = new Thread(() ->
			{
				try
				{
					while (true)
					{
						Packet p = (Packet) inFromServer.readObject();
						packetBuffer.add(p);
					}
				}
				catch (ClassNotFoundException | IOException e)
				{
					e.printStackTrace();
				}
			});
			packetReader.setDaemon(true);
			packetReader.start();

			setChangedAndNotifyObservers();
		});
		serverConnector.setDaemon(true);
		serverConnector.start();

		// start a timer that will parse packets 60 times per second
		AnimationTimer at = new AnimationTimer()
		{
			@Override
			public void handle(long now)
			{
				while (!packetBuffer.isEmpty())
				{
					parsePacket(packetBuffer.remove(0));
				}
			}
		};
		at.start();
	}

	public Square[][] getBoard()
	{
		return board;
	}

	public void setPiece(Piece p, int r, int c)
	{
		board[r][c].setOccupied(p);
		setChangedAndNotifyObservers();
	}

	public void setChangedAndNotifyObservers()
	{
		setChanged();
		notifyObservers();
	}

	public boolean isConnectedToServer()
	{
		return clientSocket != null && clientSocket.isConnected();
	}

	private void parsePacket(Packet p)
	{
		System.out.println("Received packet of type: " + p.getPacketType());
		if (p.getPacketType() == PacketType.INITIALIZE_GAME)
		{
			InitializePacket ip = (InitializePacket) p;
			team = ip.getSource();

			// Initialize red pieces
			for (int r = 9; r < 12; r++)
			{
				for (int c = 3; c < 9; c++)
				{
					board[r][c].setOccupied(new Piece(Rank.UNKNOWN, Team.RED));
				}
			}

			// Initialize GREEN pieces
			for (int r = 3; r < 9; r++)
			{
				for (int c = 0; c < 3; c++)
				{
					board[r][c].setOccupied(new Piece(Rank.UNKNOWN, Team.GREEN));
				}
			}

			// Initialize blue pieces
			for (int r = 0; r < 3; r++)
			{
				for (int c = 3; c < 9; c++)
				{
					board[r][c].setOccupied(new Piece(Rank.UNKNOWN, Team.BLUE));
				}
			}

			// Initialize yellow pieces
			for (int r = 3; r < 9; r++)
			{
				for (int c = 9; c < 12; c++)
				{
					board[r][c].setOccupied(new Piece(Rank.UNKNOWN, Team.YELLOW));
				}
			}

			fillDummyDataAndSendReadyPacket();

		}
		else if (p.getPacketType() == PacketType.ALL_CLIENTS_READY)
		{
			turn = Team.RED;
		}
		else if (p.getPacketType() == PacketType.MOVE)
		{
			MovePacket mp = (MovePacket) p;

			int r = mp.getCoords().getLeft();
			int c = mp.getCoords().getRight();
			Direction dir = mp.getDirection();
			boolean success = mp.isSuccessful();

			// an unsuccessful move is just removal of the piece
			if (!success)
			{
				System.out.println("unsucessful move");
				board[r][c].setOccupied(null);
				setChangedAndNotifyObservers();
				return;
			}

			// sucessful move
			Piece movedPiece = board[r][c].getOccupied();

			switch (dir)
			{
			case UP:
				board[r][c].setOccupied(null);
				board[r - 1][c].setOccupied(movedPiece);

				break;
			case DOWN:
				board[r][c].setOccupied(null);
				board[r + 1][c].setOccupied(movedPiece);

				break;
			case LEFT:
				board[r][c].setOccupied(null);
				board[r][c - 1].setOccupied(movedPiece);

				break;
			case RIGHT:
				board[r][c].setOccupied(null);
				board[r][c + 1].setOccupied(movedPiece);

				break;
			default:
				System.out.println("something wrong happened in the move parsing");
			}

			// change turns, but skip them if they are eliminated
			do
			{
				turn = Team.whoseTurnNext(turn);
			}
			while (eliminated.contains(turn));
		}
		else if (p.getPacketType() == PacketType.ELIMINATION)
		{
			EliminationPacket ep = (EliminationPacket) p;
			eliminated.add(ep.getEliminated());
		}

		setChangedAndNotifyObservers();
	}

	public Team whoseTurn()
	{
		return turn;
	}

	public Team getTeam()
	{
		return team;
	}

	/**
	 * Writes the given Packet to the connected Server.
	 * 
	 * @param p - the Packet to write to the connected Server.
	 */
	public void sendPacket(Packet p)
	{
		try
		{
			System.out.println("Sending packet: " + p.getPacketType());
			outToServer.writeObject(p);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void fillDummyDataAndSendReadyPacket()
	{
		switch (team)
		{
		case RED:
			board[9][3].setOccupied(new Piece(Rank.EIGHT, Team.RED));
			board[10][3].setOccupied(new Piece(Rank.TWO, Team.RED));
			board[11][3].setOccupied(new Piece(Rank.TWO, Team.RED));
			board[9][4].setOccupied(new Piece(Rank.THREE, Team.RED));
			board[10][4].setOccupied(new Piece(Rank.BOMB, Team.RED));
			board[11][4].setOccupied(new Piece(Rank.FOUR, Team.RED));
			board[9][5].setOccupied(new Piece(Rank.SEVEN, Team.RED));
			board[10][5].setOccupied(new Piece(Rank.SIX, Team.RED));
			board[11][5].setOccupied(new Piece(Rank.FIVE, Team.RED));
			board[9][6].setOccupied(new Piece(Rank.SIX, Team.RED));
			board[10][6].setOccupied(new Piece(Rank.BOMB, Team.RED));
			board[11][6].setOccupied(new Piece(Rank.BOMB, Team.RED));
			board[9][7].setOccupied(new Piece(Rank.FOUR, Team.RED));
			board[10][7].setOccupied(new Piece(Rank.THREE, Team.RED));
			board[11][7].setOccupied(new Piece(Rank.TWO, Team.RED));
			board[9][8].setOccupied(new Piece(Rank.FIVE, Team.RED));
			board[10][8].setOccupied(new Piece(Rank.FOUR, Team.RED));
			board[11][8].setOccupied(new Piece(Rank.FLAG, Team.RED));
			break;
		case GREEN:
			board[3][0].setOccupied(new Piece(Rank.EIGHT, Team.GREEN));
			board[3][1].setOccupied(new Piece(Rank.TWO, Team.GREEN));
			board[3][2].setOccupied(new Piece(Rank.TWO, Team.GREEN));
			board[4][0].setOccupied(new Piece(Rank.THREE, Team.GREEN));
			board[4][1].setOccupied(new Piece(Rank.BOMB, Team.GREEN));
			board[4][2].setOccupied(new Piece(Rank.FOUR, Team.GREEN));
			board[5][0].setOccupied(new Piece(Rank.SEVEN, Team.GREEN));
			board[5][1].setOccupied(new Piece(Rank.SIX, Team.GREEN));
			board[5][2].setOccupied(new Piece(Rank.FIVE, Team.GREEN));
			board[6][0].setOccupied(new Piece(Rank.SIX, Team.GREEN));
			board[6][1].setOccupied(new Piece(Rank.BOMB, Team.GREEN));
			board[6][2].setOccupied(new Piece(Rank.BOMB, Team.GREEN));
			board[7][0].setOccupied(new Piece(Rank.FOUR, Team.GREEN));
			board[7][1].setOccupied(new Piece(Rank.THREE, Team.GREEN));
			board[7][2].setOccupied(new Piece(Rank.TWO, Team.GREEN));
			board[8][0].setOccupied(new Piece(Rank.FIVE, Team.GREEN));
			board[8][1].setOccupied(new Piece(Rank.FOUR, Team.GREEN));
			board[8][2].setOccupied(new Piece(Rank.FLAG, Team.GREEN));
			break;

		case BLUE:

			board[0][3].setOccupied(new Piece(Rank.EIGHT, Team.BLUE));
			board[1][3].setOccupied(new Piece(Rank.TWO, Team.BLUE));
			board[2][3].setOccupied(new Piece(Rank.TWO, Team.BLUE));
			board[0][4].setOccupied(new Piece(Rank.THREE, Team.BLUE));
			board[1][4].setOccupied(new Piece(Rank.BOMB, Team.BLUE));
			board[2][4].setOccupied(new Piece(Rank.FOUR, Team.BLUE));
			board[0][5].setOccupied(new Piece(Rank.SEVEN, Team.BLUE));
			board[1][5].setOccupied(new Piece(Rank.SIX, Team.BLUE));
			board[2][5].setOccupied(new Piece(Rank.FIVE, Team.BLUE));
			board[0][6].setOccupied(new Piece(Rank.SIX, Team.BLUE));
			board[1][6].setOccupied(new Piece(Rank.BOMB, Team.BLUE));
			board[2][6].setOccupied(new Piece(Rank.BOMB, Team.BLUE));
			board[0][7].setOccupied(new Piece(Rank.FOUR, Team.BLUE));
			board[1][7].setOccupied(new Piece(Rank.THREE, Team.BLUE));
			board[2][7].setOccupied(new Piece(Rank.TWO, Team.BLUE));
			board[0][8].setOccupied(new Piece(Rank.FIVE, Team.BLUE));
			board[1][8].setOccupied(new Piece(Rank.FOUR, Team.BLUE));
			board[2][8].setOccupied(new Piece(Rank.FLAG, Team.BLUE));

			break;

		case YELLOW:

			board[3][9].setOccupied(new Piece(Rank.EIGHT, Team.YELLOW));
			board[3][10].setOccupied(new Piece(Rank.TWO, Team.YELLOW));
			board[3][11].setOccupied(new Piece(Rank.TWO, Team.YELLOW));
			board[4][9].setOccupied(new Piece(Rank.THREE, Team.YELLOW));
			board[4][10].setOccupied(new Piece(Rank.BOMB, Team.YELLOW));
			board[4][11].setOccupied(new Piece(Rank.FOUR, Team.YELLOW));
			board[5][9].setOccupied(new Piece(Rank.SEVEN, Team.YELLOW));
			board[5][10].setOccupied(new Piece(Rank.SIX, Team.YELLOW));
			board[5][11].setOccupied(new Piece(Rank.FIVE, Team.YELLOW));
			board[6][9].setOccupied(new Piece(Rank.SIX, Team.YELLOW));
			board[6][10].setOccupied(new Piece(Rank.BOMB, Team.YELLOW));
			board[6][11].setOccupied(new Piece(Rank.BOMB, Team.YELLOW));
			board[7][9].setOccupied(new Piece(Rank.FOUR, Team.YELLOW));
			board[7][10].setOccupied(new Piece(Rank.THREE, Team.YELLOW));
			board[7][11].setOccupied(new Piece(Rank.TWO, Team.YELLOW));
			board[8][9].setOccupied(new Piece(Rank.FIVE, Team.YELLOW));
			board[8][10].setOccupied(new Piece(Rank.FOUR, Team.YELLOW));
			board[8][11].setOccupied(new Piece(Rank.FLAG, Team.YELLOW));
			break;
		default:
			System.err.println("Wrong team type received: " + team);
			break;
		}

		setup = false;
		sendPacket(new ClientReadyPacket(team, board));
	}

	public boolean getSetup()
	{
		return setup;
	}

	public void setSetup(boolean b)
	{
		setup = b;
	}
}
