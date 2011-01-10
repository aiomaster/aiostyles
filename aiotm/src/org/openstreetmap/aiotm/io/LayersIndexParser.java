package org.openstreetmap.aiotm.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.openstreetmap.aiotm.data.GarminLayer;

public class LayersIndexParser {

	private final BufferedReader input;

	public LayersIndexParser(InputStream is) {
		InputStreamReader in;
		try {
			in = new InputStreamReader(is, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			in = new InputStreamReader(is);
			System.err.println("Sorry, your system doesn't support UTF8. WTF? Uninstall the Windows crap!");
		}
		this.input = new BufferedReader(in);
	}

	public Vector<GarminLayer> parseLayers() {
		Vector<GarminLayer> layers = new Vector<GarminLayer>();
		String line = null;

		try {
			while (( line = input.readLine()) != null){
				if (!(line.charAt(0) == '#')) {
					String[] s = line.split("::");
					GarminLayer l = new GarminLayer(s[0]);
					l.setDescription(s[1]);
					layers.add(l);
				}
			}
		} catch (IOException e) {
			System.err.println("Error while parsing layers Index File");
		}
		return layers;
	}

}
