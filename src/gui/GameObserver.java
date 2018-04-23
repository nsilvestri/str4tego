package gui;

import java.util.Observable;
import java.util.Observer;

import model.StrategoGame;

public class GameObserver implements Observer {

	private StrategoGame game;
	
	@Override
	public void update(Observable game, Object arg1) {
		this.game = (StrategoGame) game;
		
		
	}

}
