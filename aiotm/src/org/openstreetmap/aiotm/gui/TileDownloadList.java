package org.openstreetmap.aiotm.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.aiotm.Aiotm;
import org.openstreetmap.aiotm.data.GarminLayer;
import org.openstreetmap.aiotm.data.GarminLayerListener;
import org.openstreetmap.aiotm.data.GarminTile;
import org.openstreetmap.aiotm.util.GBC;

public class TileDownloadList implements GarminLayerListener {

	private final JPanel panel;
	private final Map<GarminLayer, JCollapsiblePanel> layerPanels;
	private final Map<GarminLayer, JList> tileList;

	private boolean selectByMap = false;

	public TileDownloadList() {
		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		layerPanels = new HashMap<GarminLayer, JCollapsiblePanel>();
		tileList = new HashMap<GarminLayer, JList>();
	}

	public JComponent getComponent() {
		JPanel hor = new JPanel(new BorderLayout());
		hor.add(panel,BorderLayout.LINE_START);
		hor.add(Box.createHorizontalGlue(),BorderLayout.LINE_END);
		JPanel ver = new JPanel(new BorderLayout());
		ver.add(hor,BorderLayout.PAGE_START);
		ver.add(Box.createVerticalGlue(),BorderLayout.PAGE_END);
		return new JScrollPane(ver);
	}

	public void refreshLayer(GarminLayer l) {
		JList list;
		if ((list = tileList.get(l)) != null) {
			selectByMap = true;
			list.setSelectedIndices(l.getSelectedIndices());
			selectByMap = false;
		}
	}

	public void selectLayer(GarminLayer l) {
		for (GarminLayer gl : layerPanels.keySet()) {
			JCollapsiblePanel p = layerPanels.get(gl);
			if (gl.equals(l)) {
				p.expand();
			} else {
				p.collapse();
			}
		}
	}

	private void deleteLayer(GarminLayer l) {
		JCollapsiblePanel p;
		if ((p = layerPanels.get(l)) != null) {
			panel.remove(p);
		}
		layerPanels.remove(l);
	}


	private class ExtendedListModel extends DefaultListModel {
		public void wakeMeUp(Object o) {
			int i = this.indexOf(o);
			this.fireContentsChanged(this, i, i);
		}
	}

	private JList createTileList(GarminLayer l) {
		final ExtendedListModel listModel = new ExtendedListModel();

		for (GarminTile t : l.getTiles()) {
			TileProgressBar tpb = new TileProgressBar(t);
			listModel.addElement(tpb);
			tpb.addChangeListener(new ChangeListener(){

				@Override
				public void stateChanged(ChangeEvent evt) {
					listModel.wakeMeUp(evt.getSource());
				}

			});
		}

		JList list = new JList(listModel);

		list.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!selectByMap) {
					JList list = (JList)e.getSource();
					for (int i = 0; i< list.getModel().getSize(); i++) {
						GarminTile t = ((TileProgressBar)list.getModel().getElementAt(i)).getTile();
						t.setIsSelected(list.isSelectedIndex(i));
					}
					Aiotm.main.map.repaint();
				}
			}

		});


		list.setCellRenderer(new ListCellRenderer(){
			@Override
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
				JProgressBar bar = (JProgressBar) value;
				bar.setBorderPainted(isSelected);
				return bar;
			}

		});

		tileList.put(l, list);
		return list;
	}

	private void addLayer(GarminLayer l) {
		if (l.getName().equals(GarminLayer.makeDummy().getName())) return;

		JPanel content = new JPanel();
		content.add(createTileList(l));
		JCollapsiblePanel p = new JCollapsiblePanel(l.getName(),content);
		layerPanels.put(l, p);
		panel.add(p,GBC.eol());
	}

	@Override
	public void afterLayerChange(GarminLayer l) {
		refreshLayer(l);
	}

	@Override
	public void afterLayerlistChange(Vector<GarminLayer> layers) {
		// remove all deleted layers
		for (GarminLayer l : layerPanels.keySet()) {
			if (!layers.contains(l))
				deleteLayer(l);
		}

		// add missing layers
		Collection<GarminLayer> retainedLayers = layerPanels.keySet();
		for (GarminLayer l : layers) {
			if (!retainedLayers.contains(l))
				addLayer(l);
		}

	}

	public void downloadSelected() {
		for (GarminLayer l : tileList.keySet()) {
			File layerPath = new File(Aiotm.main.pref.get("cachedir"),"local/"+l.getName());
			String layerPathServer = Aiotm.main.pref.get("serverpath")+"/"+l.getName();
			for (Object o : tileList.get(l).getSelectedValues()) {
				TileProgressBar tpb = (TileProgressBar) o;
				String filename = tpb.getTile().getNumber()+".img.gz";
				Aiotm.main.dm.downloadFile(layerPathServer+"/"+filename, new File(layerPath,filename), null, tpb);
			}
		}
	}

	public class TileProgressBar extends JProgressBar {
		private final GarminTile t;
		public TileProgressBar(GarminTile t) {
			super();
			setString(t.toString());
			setStringPainted(true);
			this.t = t;
		}

		public GarminTile getTile() {
			return t;
		}

	}

}
