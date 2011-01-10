package org.openstreetmap.aiotm.data;

import java.util.Vector;

public interface GarminLayerListener {
	/**
	 * called after a Layer has changed for example tiles get selected or added or removed
	 * @param l
	 */
	public void afterLayerChange(GarminLayer l);

	public void afterLayerlistChange(Vector<GarminLayer> layers);
}
