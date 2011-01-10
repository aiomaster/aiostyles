package org.openstreetmap.aiotm.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openstreetmap.aiotm.data.GarminLayer;
import org.openstreetmap.aiotm.data.GarminLayerHandler;
import org.openstreetmap.aiotm.data.Preferences;
import org.openstreetmap.aiotm.io.DownloadManager;

public class MainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final Preferences pref = new Preferences();

	public final SlippyMap map = new SlippyMap();

	public final DownloadManager dm = new DownloadManager();

	public final GarminLayerHandler glh = new GarminLayerHandler();

	public final TileDownloadList tdl = new TileDownloadList();

	public MainFrame() {
		super("All-in-One-TileManager");
		setSize(800, 600);

		setLayout(new BorderLayout());
		//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);

		add(map, BorderLayout.CENTER);
		add(makeLeftMenu(), BorderLayout.WEST);
		add(makeTopMenu(), BorderLayout.NORTH);
		add(makeStatusPanel(), BorderLayout.SOUTH);

		//dm.downloadFile("http://dev.openstreetmap.de/aio/garmintiles/basemap/70003001.img.gz", "/home/master/workspace/aiotm/data/70003001.img.gz");
	}

	private JPanel makeStatusPanel() {
		JPanel statusPanel = new JPanel();
		statusPanel.add(new JLabel("Use right mouse button to move,\n "
				+ "left double click or mouse wheel to zoom."));
		statusPanel.add(dm.getProgressBar());
		return statusPanel;
	}

	private JPanel makeLeftMenu() {
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));

		glh.addGarminLayerListener(tdl);

		leftPanel.add(tdl.getComponent());

		JButton downloadButton = new JButton(new AbstractAction("Download Tiles"){
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				tdl.downloadSelected();
			}

		});
		leftPanel.add(downloadButton);
		leftPanel.add(dm.getSpeedLabel());
		return leftPanel;
	}

	private String chooseCacheDir() {
		final JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = chooser.showOpenDialog(this);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile().getAbsolutePath();
		}
		return null;
	}

	private JPanel makeTopMenu() {
		JPanel menu = new JPanel();
		menu.setLayout(new BoxLayout(menu, BoxLayout.LINE_AXIS));


		JComboBox layerBox = new JComboBox(glh.getLayers());
		layerBox.setPreferredSize(new Dimension(200, layerBox.getPreferredSize().height));
		layerBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				GarminLayer l = (GarminLayer) e.getItem();
				map.showGarminLayer(l);
				tdl.selectLayer(l);
			}
		});
		layerBox.setRenderer(new DefaultListCellRenderer(){
			@Override
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {

				JComponent comp = (JComponent) super.getListCellRendererComponent(list,
						value, index, isSelected, cellHasFocus);

				list.setToolTipText(((GarminLayer)value).getDescription());

				return comp;
			}

		});

		menu.add(layerBox);
		menu.add(Box.createRigidArea(new Dimension(10, 0)));

		JButton layerButton = new JButton(new AbstractAction("Lookup Layers"){
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				glh.lookupAvailableLayers();
			}

		});

		menu.add(layerButton);

		menu.add(Box.createRigidArea(new Dimension(20, 0)));


		menu.add(new JLabel("Local Garmin Tile Cache:"));
		menu.add(Box.createRigidArea(new Dimension(5, 0)));

		final JTextField cachefield = new JTextField(20);
		cachefield.setText(pref.get("cachedir"));
		cachefield.setInputVerifier(new InputVerifier() {

			@Override
			public boolean verify(JComponent field) {
				JTextField tf = (JTextField)field;
				File f = new File(tf.getText());
				return f.exists();
			}

		});
		menu.add(cachefield);
		menu.add(Box.createRigidArea(new Dimension(5, 0)));

		JButton cacheButton = new JButton(new AbstractAction("Choose"){
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String dir = chooseCacheDir();
				if (dir != null)
					cachefield.setText(dir);
			}

		});

		menu.add(cacheButton);

		return menu;
	}
}
