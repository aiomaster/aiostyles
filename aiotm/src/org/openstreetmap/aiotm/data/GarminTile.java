package org.openstreetmap.aiotm.data;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapRectangle;

public class GarminTile implements MapRectangle {

	private String filePath;
	private final Bounds b;

	private final int number;

	private boolean isSelected = false;

	public GarminTile(int number, Bounds b) {
		super();
		this.b = b;
		this.number = number;
	}

	public int getNumber() {
		return number;
	}

	@Override
	public String toString() {
		return String.valueOf(number);
	}

	public Bounds getBounds() {
		return b;
	}

	@Override
	public Coordinate getBottomRight() {
		return b.getBottomRight();
	}

	@Override
	public Coordinate getTopLeft() {
		return b.getTopLeft();
	}

	public void setIsSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public boolean isSelected() {
		return isSelected;
	}

	@Override
	public void paint(Graphics g, Point topLeft, Point bottomRight) {

		int w = bottomRight.x - topLeft.x;
		int h = bottomRight.y - topLeft.y;

		if (isSelected)
			g.setColor(new Color(0.8f, 0.5f, 0.3f, 0.6f));
		else
			g.setColor(new Color(0.9f, 0.5f, 0.3f, 0.2f));

		g.fillRect(topLeft.x, topLeft.y, w, h);

		g.setColor(Color.BLACK);
		g.drawRect(topLeft.x, topLeft.y, w, h);

	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String path) {
		filePath = path;
	}

	public boolean equals(GarminTile t) {
		return number == t.getNumber() &&
		b.equals(t.getBounds());
	}

}
