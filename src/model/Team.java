package model;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public enum Team {
	RED, BLUE, GREEN, YELLOW;

	public Paint getColor() {
		switch (this) {
			case RED :
				return Color.RED;
			case BLUE :
				return Color.BLUE;
			case GREEN :
				return Color.GREEN;
			case YELLOW :
				return Color.YELLOW;
			default :
				return null;
		}
	}
}
