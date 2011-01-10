package org.openstreetmap.aiotm.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

public class GarminLayer {

	private final String name;
	private String description = "";


	public static GarminLayer makeDummy() {
		GarminLayer l = new GarminLayer("<none>");
		l.setDescription("Show nothing on the map");
		return l;
	}

	private final Vector<GarminTile> tiles = new Vector<GarminTile>();
	private final Vector<GarminLayerListener> listener = new Vector<GarminLayerListener>();


	public GarminLayer(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setDescription(String desc) {
		description = desc;
	}

	public String getDescription() {
		return description;
	}

	public void addTile(GarminTile tile) {
		tiles.add(tile);
		notifyLayerChanged();
	}

	public void addAllTiles(Collection<GarminTile> c) {
		tiles.addAll(c);
		notifyLayerChanged();
	}

	public void removeAllTiles() {
		tiles.clear();
		notifyLayerChanged();
	}

	public List<GarminTile> getTiles() {
		return tiles;
	}

	public Vector<GarminTile> getSelected() {
		Vector<GarminTile> selected = new Vector<GarminTile>();
		for (GarminTile t : getTiles()) {
			if (t.isSelected()) selected.add(t);
		}
		return selected;
	}

	public int[] getSelectedIndices() {
		List<Integer> l = new ArrayList<Integer>();
		for (int i=0 ; i<tiles.size() ; i++) {
			if (tiles.get(i).isSelected())
				l.add(new Integer(i));
		}
		int[] indices = new int[l.size()];
		for (int i=0; i<l.size(); i++) {
			indices[i] = l.get(i);
		}
		return indices;
	}

	public void addGarminLayerListener(GarminLayerListener gll) {
		listener.add(gll);
	}

	public void removeGarminLayerListener(GarminLayerListener gll) {
		listener.remove(gll);
	}

	public void notifyLayerChanged() {
		for (GarminLayerListener gll :  listener) {
			gll.afterLayerChange(this);
		}
	}

	@Override
	public String toString() {
		return name;
	}

}
