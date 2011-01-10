package org.openstreetmap.aiotm;


//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import org.openstreetmap.aiotm.gui.MainFrame;
import org.openstreetmap.gui.jmapviewer.JMapViewer;

/**
 *
 * Demonstrates the usage of {@link JMapViewer}
 *
 * @author Jan Peter Stotz
 *
 */
public class Aiotm {

	private static final long serialVersionUID = 1L;

	public static MainFrame main;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// java.util.Properties systemProperties = System.getProperties();
		// systemProperties.setProperty("http.proxyHost", "localhost");
		// systemProperties.setProperty("http.proxyPort", "8008");
		main = new MainFrame();
		main.setVisible(true);
		//Aiotm.map.setGarminTiles(Aiotm.glh.getLayerTiles());
		main.addWindowListener(new WindowListener() {

			@Override
			public void windowActivated(WindowEvent arg0) {
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				Aiotm.main.pref.saveProperties();
				System.exit(0);
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
			}

			@Override
			public void windowIconified(WindowEvent arg0) {
			}

			@Override
			public void windowOpened(WindowEvent arg0) {
			}

		});

	}

}
