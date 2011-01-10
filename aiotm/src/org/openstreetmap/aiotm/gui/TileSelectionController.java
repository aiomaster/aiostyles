package org.openstreetmap.aiotm.gui;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class TileSelectionController extends MouseAdapter implements MouseMotionListener, MouseListener {

	private final SourceButton layerswitcher;
	private final SlippyMap map;

	// start and end point of selection rectangle
	private Point start;
	private Point end;

	public TileSelectionController(SlippyMap slippymap, SourceButton sourceButton) {
		layerswitcher = sourceButton;
		map = slippymap;
		map.addMouseListener(this);
		map.addMouseMotionListener(this);
	}

	/**
	 * Start drawing the selection rectangle if it was the 1st button (left
	 * button)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			start = e.getPoint();
			end = e.getPoint();
			map.setIsSelecting(true);
		}

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
			if (start != null) {
				end = e.getPoint();
				map.setSelection(start, end, e.isControlDown() || e.isShiftDown());
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			map.setIsSelecting(false);

			int sourceButton = layerswitcher.hit(e.getPoint());

			if (sourceButton == SourceButton.HIDE_OR_SHOW) {
				layerswitcher.toggle();
				map.repaint();

			} else if (sourceButton == SourceButton.MAPNIK || sourceButton == SourceButton.OSMARENDER
					|| sourceButton == SourceButton.CYCLEMAP) {
				map.toggleMapSource(sourceButton);
			} else {
				map.setSelection(start, e.getPoint(), e.isControlDown() || e.isShiftDown());
			}

		}
	}

}
