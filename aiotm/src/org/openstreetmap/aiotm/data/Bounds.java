package org.openstreetmap.aiotm.data;

import org.openstreetmap.gui.jmapviewer.Coordinate;

public class Bounds {
	/**
	 * The minimum and maximum coordinates.
	 */
	private final double minLat, minLon, maxLat, maxLon;

	public Coordinate getMin() {
		return new Coordinate(minLat, minLon);
	}

	public Coordinate getMax() {
		return new Coordinate(maxLat, maxLon);
	}

	public Coordinate getBottomRight() {
		return new Coordinate(minLat, maxLon);
	}

	public Coordinate getTopLeft() {
		return new Coordinate(maxLat, minLon);
	}

	public Bounds(double minlat, double minlon, double maxlat, double maxlon) {
		this.minLat = minlat;
		this.minLon = minlon;
		this.maxLat = maxlat;
		this.maxLon = maxlon;
	}

	/**
	 * The two bounds intersect? Compared to java Shape.intersects, if does not use
	 * the interior but the closure. (">=" instead of ">")
	 */
	public boolean intersects(Bounds b) {
		return b.getMax().getLat() >= minLat &&
		b.getMax().getLon() >= minLon &&
		b.getMin().getLat() <= maxLat &&
		b.getMin().getLon() <= maxLon;
	}

	public boolean equals(Bounds b) {
		Coordinate max = b.getMax();
		Coordinate min = b.getMin();
		return minLat == min.getLat() &&
		minLon == min.getLon() &&
		maxLat == max.getLat() &&
		maxLon == max.getLon();
	}

}
