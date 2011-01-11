package org.openstreetmap.aiotm.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;

import org.openstreetmap.aiotm.data.Bounds;
import org.openstreetmap.aiotm.data.GarminLayer;
import org.openstreetmap.aiotm.data.GarminTile;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;
import org.openstreetmap.gui.jmapviewer.OsmTileSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapRectangle;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

public class SlippyMap extends JMapViewer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static private TileSource[] TILE_SOURCES = { new OsmTileSource.Mapnik(),
		new OsmTileSource.TilesAtHome(), new OsmTileSource.CycleMap() };

	private GarminLayer gLayer;

	private final SourceButton layerswitcher = new SourceButton();

	// upper left and lower right corners of the selection rectangle
	Point iSelectionRectStart;
	Point iSelectionRectEnd;

	private boolean isSelecting = false;

	public SlippyMap() {
		super();
		setTileLoader(new OsmFileCacheTileLoader(this));
		setZoomContolsVisible(false);
		new TileSelectionController(this,layerswitcher);
	}

	public void setGarminTiles(List<GarminTile> tiles) {
		getMapRectangleList().clear();
		for (GarminTile t : tiles) {
			addMapRectangle(t);
		}
	}

	public void showGarminLayer(GarminLayer l) {
		setGarminTiles(l.getTiles());
		gLayer = l;
	}

	public void setIsSelecting(boolean isSelecting) {
		this.isSelecting = isSelecting;
	}

	protected Point getTopLeftCoordinates() {
		return new Point(center.x - (getWidth() / 2), center.y - (getHeight() / 2));
	}

	/**
	 * Draw the map.
	 */
	@Override
	public void paint(Graphics g) {
		try {
			super.paint(g);

			// draw selection rectangle
			if (isSelecting && iSelectionRectStart != null && iSelectionRectEnd != null) {

				int zoomDiff = MAX_ZOOM - zoom;
				Point tlc = getTopLeftCoordinates();
				int x_min = (iSelectionRectStart.x >> zoomDiff) - tlc.x;
				int y_min = (iSelectionRectStart.y >> zoomDiff) - tlc.y;
				int x_max = (iSelectionRectEnd.x >> zoomDiff) - tlc.x;
				int y_max = (iSelectionRectEnd.y >> zoomDiff) - tlc.y;

				int w = x_max - x_min;
				int h = y_max - y_min;
				g.setColor(new Color(0.9f, 0.7f, 0.7f, 0.6f));
				g.fillRect(x_min, y_min, w, h);

				g.setColor(Color.BLACK);
				g.drawRect(x_min, y_min, w, h);
			}
			g.setClip(this.getVisibleRect());
			layerswitcher.paint(g);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void toggleMapSource(int mapSource) {
		this.tileController.setTileCache(new MemoryTileCache());
		if (mapSource == SourceButton.MAPNIK) {
			this.setTileSource(TILE_SOURCES[0]);
		} else if (mapSource == SourceButton.CYCLEMAP) {
			this.setTileSource(TILE_SOURCES[2]);
		} else {
			this.setTileSource(TILE_SOURCES[1]);
		}
	}

	/**
	 * Callback for the OsmMapControl. (Re-)Sets the start and end point of the
	 * selection rectangle.
	 *
	 * @param aStart
	 * @param aEnd
	 */
	public void setSelection(Point aStart, Point aEnd, boolean isAdditional) {
		if (aStart == null || aEnd == null )
			return;

		Point p_max = new Point(Math.max(aEnd.x, aStart.x), Math.max(aEnd.y, aStart.y));
		Point p_min = new Point(Math.min(aEnd.x, aStart.x), Math.min(aEnd.y, aStart.y));

		Point tlc = getTopLeftCoordinates();
		int zoomDiff = MAX_ZOOM - zoom;
		Point pEnd = new Point(p_max.x + tlc.x, p_max.y + tlc.y);
		Point pStart = new Point(p_min.x + tlc.x, p_min.y + tlc.y);

		pEnd.x <<= zoomDiff;
		pEnd.y <<= zoomDiff;
		pStart.x <<= zoomDiff;
		pStart.y <<= zoomDiff;

		iSelectionRectStart = pStart;
		iSelectionRectEnd = pEnd;

		Coordinate l1 = getPosition(p_max);
		Coordinate l2 = getPosition(p_min);
		if (gLayer != null)
			selectTiles(new Bounds(
					Math.min(l2.getLat(), l1.getLat()),
					Math.min(l1.getLon(), l2.getLon()),
					Math.max(l2.getLat(), l1.getLat()),
					Math.max(l1.getLon(), l2.getLon())
			),
			isAdditional
			);

		repaint();
	}

	public void selectTiles(Bounds b, boolean isAdditional) {
		List<MapRectangle> rects = getMapRectangleList();
		GarminTile t;
		for (MapRectangle r : rects) {
			t = (GarminTile)r;
			t.setIsSelected( isAdditional && t.isSelected() ||t.getBounds().intersects(b));
		}
		gLayer.notifyLayerChanged();
	}


}
