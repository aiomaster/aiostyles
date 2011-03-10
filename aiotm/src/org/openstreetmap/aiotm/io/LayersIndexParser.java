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
		GarminLayer l = null;
		int lineNumber = 0;
		try {
			while (( line = input.readLine()) != null){
				lineNumber++;
				// remove comments
				int i;
				if ((i = line.indexOf('#')) != -1)
					line = line.substring(0, i);

				// remove whitespaces
				line = line.trim();

				// continue loop if it was an empty line
				if (line.equals("")) continue;

				String[] kv = new String[2];
				// read key word and value
				kv = line.split("\\s", 2);
				if (kv[1] == null) {
					System.err.println("Error in Line:"+lineNumber);
					continue;
				}

				String key = kv[0];
				String value = kv[1].trim();

				if (kv[0].equals("Layer") && value.matches("\\w*")) {
					l = new GarminLayer(value);
					layers.add(l);
					continue;
				}

				if (l != null) {
					if (!l.setValue(key, value)) {
						System.err.println("Key-Value-Pair in Index File sucks. Line number:"+lineNumber);
					}
				} else {
					System.err.println("Found text without Layer context. Line number:"+lineNumber);
				}


			}
		} catch (IOException e) {
			System.err.println("Error while parsing layers Index File");
		}
		return layers;
	}

}
