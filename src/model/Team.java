package model;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public enum Team {
	RED, BLUE, GREEN, YELLOW, NONE;

	public Paint getColor() {
		switch (this) {
			case RED :
				return Color.RED;
			case BLUE :
				return Color.BLUE;
			case GREEN :
				return Color.GREEN;
			case YELLOW :
				return Color.GOLD;
			case NONE:
				return Color.BLACK;
			default :
				return Color.PURPLE;
		}
	}
}
