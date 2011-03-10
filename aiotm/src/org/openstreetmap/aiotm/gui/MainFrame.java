package org.openstreetmap.aiotm.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
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
import org.openstreetmap.aiotm.util.GBC;
import org.openstreetmap.aiotm.util.MkgmapController;

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
		leftPanel.setLayout(new GridBagLayout());
		leftPanel.setPreferredSize(new Dimension(200,leftPanel.getPreferredSize().height));

		glh.addGarminLayerListener(tdl);

		leftPanel.add(tdl.getComponent(),GBC.eol().fill());

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
		JButton gmapsuppButton = new JButton(new AbstractAction("Create gmapsupp.img"){
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dm.getProgressBar().setIndeterminate(true);
				String outDir = chooseDir();
				MkgmapController.createGmapsupp(glh.getLayers(),outDir);
				dm.getProgressBar().setIndeterminate(false);
			}

		});
		leftPanel.add(downloadButton,GBC.eol());
		leftPanel.add(dm.getSpeedLabel(),GBC.eol());
		leftPanel.add(gmapsuppButton,GBC.eol());
		return leftPanel;
	}

	private String chooseDir() {
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
				if (f.exists())
					pref.put("cachedir", f.getAbsolutePath());
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
				String dir = chooseDir();
				if (dir != null) {
					cachefield.setText(dir);
					pref.put("cachedir", dir);
				}
			}

		});

		menu.add(cacheButton);

		return menu;
	}
}
