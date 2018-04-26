package model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;

import javafx.animation.AnimationTimer;

public class StrategoGame extends Observable {

	private Square[][] board;
	private Team team;

	private final static String serverAddress = "localhost";
	private final static int serverPort = 9998;
	private static Socket clientSocket;
	private static ObjectOutputStream outToServer;
	private static ObjectInputStream inFromServer;
	private static ArrayList<Packet> packetBuffer = new ArrayList<Packet>();

	private Team turn;

	public StrategoGame() {
		initializeBoard();
		team = Team.NONE;
		initializeServerConnection();
		setChangedAndNotifyObservers();
	}

	/**
	 * Initializes the board instance and declares which squares are unmovable.
	 */
	private void initializeBoard() {

		board = new Square[12][12];

		for (int r = 0; r < 12; r++) {
			for (int c = 0; c < 12; c++) {
				board[r][c] = new Square();
			}
		}

		/* Make certain Squares of the board unmovable */

		// top left 3x3 grid
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 3; c++) {
				board[r][c].setMovable(false);
			}
		}

		// top right 3x3 grid
		for (int r = 0; r < 3; r++) {
			for (int c = 9; c < 12; c++) {
				board[r][c].setMovable(false);
			}
		}

		// bottom left 3x3 grid
		for (int r = 9; r < 12; r++) {
			for (int c = 0; c < 3; c++) {
				board[r][c].setMovable(false);
			}
		}

		// bottom right 3x3 grid
		for (int r = 9; r < 12; r++) {
			for (int c = 9; c < 12; c++) {
				board[r][c].setMovable(false);
			}
		}

		// center 2x2 grid
		for (int r = 5; r < 7; r++) {
			for (int c = 5; c < 7; c++) {
				board[r][c].setMovable(false);
			}
		}
	}

	private void initializeServerConnection() {
		// start a thread that continuously attempts to connect to the server
		Thread serverConnector = new Thread(() -> {

			while (clientSocket == null || !clientSocket.isConnected()) {
				try {
					clientSocket = new Socket();
					clientSocket.connect(new InetSocketAddress(serverAddress, serverPort));
				} catch (IOException ioe) {
					// TODO: Empty Catch
				}

				// hopefully stops the server from thinking it's getting DOS'd
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// initialize streams
			try {
				outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
				inFromServer = new ObjectInputStream(clientSocket.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}

			// start a thread that reads packets from the socket and puts them
			// in the buffer
			Thread packetReader = new Thread(() -> {
				try {
					while (true) {
						Packet p = (Packet) inFromServer.readObject();
						packetBuffer.add(p);
					}
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
			});
			packetReader.start();

			setChangedAndNotifyObservers();
		});
		serverConnector.start();

		// start a timer that will parse packets 60 times per second
		AnimationTimer at = new AnimationTimer() {
			@Override
			public void handle(long now) {
				while (!packetBuffer.isEmpty()) {
					parsePacket(packetBuffer.remove(0));
				}
			}
		};
		at.start();
	}

	public Square[][] getBoard() {
		return board;
	}

	public void setPiece(Piece p, int r, int c) {
		board[r][c].setOccupied(p);
		setChangedAndNotifyObservers();
	}

	public void setChangedAndNotifyObservers() {
		setChanged();
		notifyObservers();
	}

	public boolean isConnectedToServer() {
		return clientSocket != null && clientSocket.isConnected();
	}

	private void parsePacket(Packet p) {
		System.out.println("Received packet of type: " + p.getPacketType());
		if (p.getPacketType() == PacketType.INITIALIZE_GAME) {
			InitializePacket ip = (InitializePacket) p;
			team = ip.getSource();

			// Initialize red pieces
			for (int r = 9; r < 12; r++) {
				for (int c = 3; c < 9; c++) {
					board[r][c].setOccupied(new Piece(Rank.UNKNOWN, Team.RED));
				}
			}

			// Initialize GREEN pieces
			for (int r = 3; r < 9; r++) {
				for (int c = 0; c < 3; c++) {
					board[r][c].setOccupied(new Piece(Rank.UNKNOWN, Team.GREEN));
				}
			}

			// Initialize blue pieces
			for (int r = 0; r < 3; r++) {
				for (int c = 3; c < 9; c++) {
					board[r][c].setOccupied(new Piece(Rank.UNKNOWN, Team.BLUE));
				}
			}

			// Initialize yellow pieces
			for (int r = 3; r < 9; r++) {
				for (int c = 9; c < 12; c++) {
					board[r][c].setOccupied(new Piece(Rank.UNKNOWN, Team.YELLOW));
				}
			}

			fillDummyDataAndSendReadyPacket();

		} else if (p.getPacketType() == PacketType.ALL_CLIENTS_READY) {
			turn = Team.RED;
		}

		setChangedAndNotifyObservers();
	}

	public Team whoseTurn() {
		return turn;
	}

	public Team getTeam() {
		return team;
	}

	/**
	 * Writes the given Packet to the connected Server.
	 * 
	 * @param p
	 *            - the Packet to write to the connected Server.
	 */
	public void sendPacket(Packet p) {
		try {
			outToServer.writeObject(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void fillDummyDataAndSendReadyPacket() {
		switch (team) {
			case RED :
				for (int r = 9; r < 12; r++) {
					for (int c = 3; c < 9; c++) {
						board[r][c].setOccupied(new Piece(Rank.BOMB, Team.RED));
					}
				}
				break;
			case GREEN :
				// Initialize GREEN pieces
				for (int r = 3; r < 9; r++) {
					for (int c = 0; c < 3; c++) {
						board[r][c].setOccupied(new Piece(Rank.BOMB, Team.GREEN));
					}
				}
				break;

			case BLUE :
				// Initialize blue pieces
				for (int r = 0; r < 3; r++) {
					for (int c = 3; c < 9; c++) {
						board[r][c].setOccupied(new Piece(Rank.BOMB, Team.BLUE));
					}
				}
				break;

			case YELLOW :
				// Initialize yellow pieces
				for (int r = 3; r < 9; r++) {
					for (int c = 9; c < 12; c++) {
						board[r][c].setOccupied(new Piece(Rank.BOMB, Team.YELLOW));
					}
				}
				break;
			default :
				System.err.println("Wrong team type received: " + team);
				break;
		}

		sendPacket(new ClientReadyPacket(team, board));
	}

}
