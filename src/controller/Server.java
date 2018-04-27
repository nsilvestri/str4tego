package controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.InitializePacket;
import model.MovePacket;
import model.Packet;
import model.PacketType;
import model.Piece;
import model.AllClientsReadyPacket;
import model.ClientReadyPacket;
import model.Direction;
import model.Square;
import model.StrategoGame;
import model.Team;

public class Server extends Application
{

	private static Square[][] serverBoard;
	private static Canvas canvas;
	private static GraphicsContext gc;
	private static VBox statusLabels;

	private final static int serverPort = 9998;
	private static ServerSocket serverSocket;
	private static Socket[] connectedClients;
	private static ObjectOutputStream[] clientOutputStreams;
	private static ObjectInputStream[] clientInputStreams;
	private ArrayList<Packet> packetBuffer;

	private static boolean gameInProgress = false;
	private static boolean redReady = false;
	private static boolean greenReady = false;
	private static boolean blueReady = false;
	private static boolean yellowReady = false;

	private static Team turn;

	public static void main(String[] args) throws UnknownHostException, IOException
	{

		serverSocket = new ServerSocket(serverPort);
		connectedClients = new Socket[4];
		clientOutputStreams = new ObjectOutputStream[4];
		clientInputStreams = new ObjectInputStream[4];

		initializeBoard();

		launch(args);
	}

	private BorderPane window;
	private Label[] connectionStatuses;

	@Override
	public void start(Stage primaryStage) throws Exception
	{

		window = new BorderPane();

		canvas = new Canvas(480, 480);
		gc = canvas.getGraphicsContext2D();
		window.setCenter(canvas);
		drawBoard();

		primaryStage.setTitle("Str4tego Server");
		Scene scene = new Scene(window, 500, 600);
		primaryStage.setScene(scene);
		primaryStage.show();

		statusLabels = new VBox();
		connectionStatuses = new Label[4];
		for (int i = 0; i < 4; i++)
		{
			connectionStatuses[i] = new Label("Client " + (i + 1) + " not connected.");
			statusLabels.getChildren().add(connectionStatuses[i]);
		}
		window.setTop(statusLabels);

		packetBuffer = new ArrayList<Packet>();
		connectClients();

		// start a timer that will parse packets 60 times per second
		AnimationTimer at = new AnimationTimer()
		{
			@Override
			public void handle(long now)
			{
				while (!packetBuffer.isEmpty())
				{
					parseNextPacket();
				}
			}
		};
		at.start();
	}

	private void connectClients() throws IOException
	{
		ClientConnector cc = new ClientConnector();
		Thread thread = new Thread(cc);
		thread.start();
	}

	private class ClientConnector implements Runnable
	{

		@Override
		public void run()
		{
			for (int i = 0; i < 4; i++)
			{
				try
				{
					connectedClients[i] = serverSocket.accept();
					clientOutputStreams[i] = new ObjectOutputStream(connectedClients[i].getOutputStream());
					clientInputStreams[i] = new ObjectInputStream(connectedClients[i].getInputStream());
					Platform.runLater(() ->
					{
						for (int j = 0; j < 4; j++)
						{
							if (connectedClients[j] != null)
							{
								connectionStatuses[j].setText("Client " + (j + 1) + " connected.");
							}
						}
					});
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			initializeClientReaders();

			/* All clients connected; tell them their team */
			try
			{
				clientOutputStreams[0].writeObject(new InitializePacket(Team.RED));
				clientOutputStreams[1].writeObject(new InitializePacket(Team.GREEN));
				clientOutputStreams[2].writeObject(new InitializePacket(Team.BLUE));
				clientOutputStreams[3].writeObject(new InitializePacket(Team.YELLOW));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void initializeClientReaders()
	{
		// client 1
		Thread packetReader1 = new Thread(() ->
		{
			try
			{
				while (true)
				{
					Packet p = (Packet) clientInputStreams[0].readObject();
					addPacketToBuffer(p);
				}
			}
			catch (ClassNotFoundException | IOException e)
			{
				e.printStackTrace();
			}
		});
		packetReader1.start();

		// client 2
		Thread packetReader2 = new Thread(() ->
		{
			try
			{
				while (true)
				{
					Packet p = (Packet) clientInputStreams[1].readObject();
					addPacketToBuffer(p);
				}
			}
			catch (ClassNotFoundException | IOException e)
			{
				e.printStackTrace();
			}
		});
		packetReader2.start();

		// client 3
		Thread packetReader3 = new Thread(() ->
		{
			try
			{
				while (true)
				{
					Packet p = (Packet) clientInputStreams[2].readObject();
					addPacketToBuffer(p);
				}
			}
			catch (ClassNotFoundException | IOException e)
			{
				e.printStackTrace();
			}
		});
		packetReader3.start();

		// client 4
		Thread packetReader4 = new Thread(() ->
		{
			try
			{
				while (true)
				{
					Packet p = (Packet) clientInputStreams[3].readObject();
					addPacketToBuffer(p);
				}
			}
			catch (ClassNotFoundException | IOException e)
			{
				e.printStackTrace();
			}
		});
		packetReader4.start();
	}

	private synchronized void addPacketToBuffer(Packet p)
	{
		packetBuffer.add(p);
	}

	private void parseNextPacket()
	{
		Packet p = packetBuffer.remove(0);
		System.out.println("Received packet of type " + p.getPacketType() + " from " + p.getSource());
		// special check if a game is being readied up
		if (!gameInProgress)
		{
			// trash any packet that isn't a READY packet
			if (p.getPacketType() != PacketType.READY)
			{
				return;
			}

			ClientReadyPacket crp = (ClientReadyPacket) p;
			Square[][] packetBoard = crp.getBoard();

			// copy the given board to the server's board
			switch (crp.getSource())
			{
			case RED:
				for (int r = 9; r < 12; r++)
				{
					for (int c = 3; c < 9; c++)
					{
						Piece clientPiece = packetBoard[r][c].getOccupied();
						Piece newPiece = new Piece(clientPiece.getRank(), clientPiece.getTeam());
						serverBoard[r][c].setOccupied(newPiece);
					}
				}

				redReady = true;
				break;

			case GREEN:

				for (int r = 3; r < 9; r++)
				{
					for (int c = 0; c < 3; c++)
					{
						Piece clientPiece = packetBoard[r][c].getOccupied();
						Piece newPiece = new Piece(clientPiece.getRank(), clientPiece.getTeam());
						serverBoard[r][c].setOccupied(newPiece);
					}
				}

				greenReady = true;
				break;

			case BLUE:

				for (int r = 0; r < 3; r++)
				{
					for (int c = 3; c < 9; c++)
					{
						Piece clientPiece = packetBoard[r][c].getOccupied();
						Piece newPiece = new Piece(clientPiece.getRank(), clientPiece.getTeam());
						serverBoard[r][c].setOccupied(newPiece);
					}
				}

				blueReady = true;
				break;

			case YELLOW:

				for (int r = 3; r < 9; r++)
				{
					for (int c = 9; c < 12; c++)
					{
						Piece clientPiece = packetBoard[r][c].getOccupied();
						Piece newPiece = new Piece(clientPiece.getRank(), clientPiece.getTeam());
						serverBoard[r][c].setOccupied(newPiece);
					}

				}
				yellowReady = true;
				break;

			default:
				System.err.println("Unknown team type");
				break;
			}

			// check if all clients are ready and send the clients the ready
			// sign
			if (redReady && greenReady && blueReady && yellowReady)
			{
				System.out.println("All clients ready.");
				gameInProgress = true;
				Packet acrp = new AllClientsReadyPacket();

				for (int i = 0; i < 4; i++)
				{
					try
					{
						clientOutputStreams[i].writeObject(acrp);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}

				turn = Team.RED;
			}
		}

		// parse move packets
		else if (p.getPacketType() == PacketType.MOVE)
		{
			// trash any packet from a client whose not waiting its turn
			if (p.getSource() != turn)
			{
				return;
			}

			MovePacket mp = (MovePacket) p;

			int r = mp.getCoords().getLeft();
			int c = mp.getCoords().getRight();
			Piece movedPiece = serverBoard[r][c].getOccupied();
			Direction dir = mp.getDirection();

			switch (dir)
			{
			case UP:
				if (!serverBoard[r - 1][c].isOccupied())
				{
					serverBoard[r - 1][c].setOccupied(movedPiece);
					serverBoard[r][c].setOccupied(null);
					mp.setSuccessful(true);
					sendPacketToAll(mp);
				}
				else
				{
					if (movedPiece.getRank().getValue() >= serverBoard[r - 1][c].getOccupied().getRank().getValue())
					{
						serverBoard[r - 1][c].setOccupied(movedPiece);
						mp.setSource(Team.SERVER);
						mp.setSuccessful(true);
						sendPacketToAll(mp);
					}
					else
					{
						serverBoard[r][c].setOccupied(null);
					}
				}
				break;
			case DOWN:
				System.out.println("square: " + serverBoard[r][c]);
				if (!serverBoard[r + 1][c].isOccupied())
				{
					serverBoard[r][c].setOccupied(null);
					serverBoard[r + 1][c].setOccupied(movedPiece);
					mp.setSuccessful(true);
					sendPacketToAll(mp);
				}
				else
				{
					if (movedPiece.getRank().getValue() >= serverBoard[r + 1][c].getOccupied().getRank().getValue())
					{
						serverBoard[r + 1][c].setOccupied(movedPiece);
						mp.setSuccessful(true);
						sendPacketToAll(mp);

					}
					else
					{
						serverBoard[r][c].setOccupied(null);
					}
				}
				break;
			case LEFT:
				if (!serverBoard[r][c - 1].isOccupied())
				{
					serverBoard[r][c].setOccupied(null);
					serverBoard[r][c - 1].setOccupied(movedPiece);
					mp.setSuccessful(true);
					sendPacketToAll(mp);
				}
				else
				{
					if (movedPiece.getRank().getValue() >= serverBoard[r][c - 1].getOccupied().getRank().getValue())
					{
						serverBoard[r][c - 1].setOccupied(movedPiece);
						mp.setSuccessful(true);
						sendPacketToAll(mp);
					}
					else
					{
						serverBoard[r][c].setOccupied(null);
					}
				}
				break;
			case RIGHT:
				if (!serverBoard[r][c + 1].isOccupied())
				{
					serverBoard[r][c].setOccupied(null);
					serverBoard[r][c + 1].setOccupied(movedPiece);
					mp.setSuccessful(true);
					sendPacketToAll(mp);
				}
				else
				{
					if (movedPiece.getRank().getValue() >= serverBoard[r][c + 1].getOccupied().getRank().getValue())
					{
						serverBoard[r][c + 1].setOccupied(movedPiece);
						mp.setSuccessful(true);
						sendPacketToAll(mp);
					}
					else
					{
						serverBoard[r][c].setOccupied(null);
					}
				}
				break;
			default:
				System.out.println("something wrong happened in the move parsing");
			}
			
			turn = Team.whoseTurnNext(turn);
		}
		
		drawBoard();
	}

	/* draws the board on the GraphicsContext */
	private void drawBoard()
	{
		// reset to a white canvas
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		int sqSize = 40;

		for (int r = 0; r < serverBoard.length; r++)
		{
			for (int c = 0; c < serverBoard[r].length; c++)
			{

				Square thisSquare = serverBoard[r][c];

				gc.setFill(Color.BLACK);
				gc.setStroke(Color.BLACK);
				if (!thisSquare.isMoveable())
				{
					gc.fillRect(c * sqSize, r * sqSize, sqSize, sqSize);
				}
				else
				{
					gc.strokeRect(c * sqSize, r * sqSize, sqSize, sqSize);
				}

				if (thisSquare.isOccupied())
				{
					gc.setFill(thisSquare.getOccupied().getTeam().getColor());
					int margin = 3;
					gc.fillRect(c * sqSize + margin, r * sqSize + margin, sqSize - (margin * 2), sqSize - (margin * 2));
					gc.setFill(Color.WHITE);
					gc.setFont(new Font("Garamond", 32));
					gc.fillText(thisSquare.getOccupied().getRank().getSymbol(), c * sqSize + 13, (r + 1) * sqSize - 10);
				}
			}
		}
	}

	/**
	 * Initializes the board instance and declares which squares are unmovable.
	 */
	private static void initializeBoard()
	{

		serverBoard = new Square[12][12];

		for (int r = 0; r < 12; r++)
		{
			for (int c = 0; c < 12; c++)
			{
				serverBoard[r][c] = new Square();
			}
		}

		/* Make certain Squares of the board unmovable */

		// top left 3x3 grid
		for (int r = 0; r < 3; r++)
		{
			for (int c = 0; c < 3; c++)
			{
				serverBoard[r][c].setMovable(false);
			}
		}

		// top right 3x3 grid
		for (int r = 0; r < 3; r++)
		{
			for (int c = 9; c < 12; c++)
			{
				serverBoard[r][c].setMovable(false);
			}
		}

		// bottom left 3x3 grid
		for (int r = 9; r < 12; r++)
		{
			for (int c = 0; c < 3; c++)
			{
				serverBoard[r][c].setMovable(false);
			}
		}

		// bottom right 3x3 grid
		for (int r = 9; r < 12; r++)
		{
			for (int c = 9; c < 12; c++)
			{
				serverBoard[r][c].setMovable(false);
			}
		}

		// center 2x2 grid
		for (int r = 5; r < 7; r++)
		{
			for (int c = 5; c < 7; c++)
			{
				serverBoard[r][c].setMovable(false);
			}
		}
	}

	/**
	 * Sends the given packet to all connected Clients.
	 * 
	 * @param p - the packet to be sent.
	 */
	private void sendPacket(Packet p)
	{
		for (int i = 0; i < 4; i++)
		{
			try
			{
				clientOutputStreams[i].writeObject(p);
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Sends the given packet to all connected Clients.
	 * 
	 * @param p - the packet to be sent.
	 */
	private void sendPacketToAll(Packet p) {
		for (int i = 0; i < 4; i++) {
			try
			{
				clientOutputStreams[i].writeObject(p);
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Sends the given packet to all connected Clients except the client whose turn it is.
	 * 
	 * @param p - the packet to be sent.
	 */
	private void sendPacketToAllButCurrentTurn(Packet p)
	{
		switch (turn)
		{
		case RED:

			try
			{
				clientOutputStreams[1].writeObject(p);
				clientOutputStreams[2].writeObject(p);
				clientOutputStreams[3].writeObject(p);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		case GREEN:

			try
			{
				clientOutputStreams[0].writeObject(p);
				clientOutputStreams[2].writeObject(p);
				clientOutputStreams[3].writeObject(p);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		case BLUE:

			try
			{
				clientOutputStreams[0].writeObject(p);
				clientOutputStreams[1].writeObject(p);
				clientOutputStreams[3].writeObject(p);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		case YELLOW:

			try
			{
				clientOutputStreams[0].writeObject(p);
				clientOutputStreams[1].writeObject(p);
				clientOutputStreams[2].writeObject(p);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		default:
			System.out.println("AllButCurrent: unknown team");
		}
	}

	/**
	 * Sends the given packet to all connected Clients.
	 * 
	 * @param p - the packet to be sent.
	 */
	private void sendPacketToCurrentTurn(Packet p)
	{
		switch (turn)
		{
		case RED:

			try
			{
				clientOutputStreams[0].writeObject(p);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		case GREEN:

			try
			{
				clientOutputStreams[1].writeObject(p);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		case BLUE:

			try
			{
				clientOutputStreams[2].writeObject(p);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		case YELLOW:

			try
			{
				clientOutputStreams[3].writeObject(p);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		default:
			System.out.println("Current: unknown team");
		}
	}
}
