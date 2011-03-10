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

	private final long date;

	private final String md5Hash;

	private boolean isSelected = false;

	private boolean isCached = false;

	public GarminTile(int number, Bounds b, String hash, long date) {
		super();
		this.b = b;
		this.number = number;
		this.md5Hash = hash;
		this.date = date;
	}

	public int getNumber() {
		return number;
	}

	public String getHash() {
		return md5Hash;
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

	public void setCached(boolean cached) {
		isCached = cached;
	}

	public boolean isCached() {
		return isCached;
	}

	/*
	public void setLocalDate(long time) {
		this.localDate = time;
	}

	public long getLocalDate() {
		return localDate;
	}

	public void setServerDate(long time) {
		this.serverDate = time;
	}

	public long getServerDate() {
		return serverDate;
	}

	public boolean isOutdated() {
		return serverDate>localDate;
	}
	 */

	public long getDate() {
		return date;
	}

	@Override
	public void paint(Graphics g, Point topLeft, Point bottomRight) {

		int w = bottomRight.x - topLeft.x;
		int h = bottomRight.y - topLeft.y;

		if (isCached) {
			if (isSelected)
				g.setColor(new Color(0.4f, 1.0f, 0.2f, 0.6f));
			else
				g.setColor(new Color(0.6f, 1.0f, 0.2f, 0.4f));
		} else {
			if (isSelected)
				g.setColor(new Color(0.8f, 0.5f, 0.3f, 0.6f));
			else
				g.setColor(new Color(0.9f, 0.5f, 0.3f, 0.2f));
		}
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
