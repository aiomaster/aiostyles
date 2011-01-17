package org.openstreetmap.aiotm.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openstreetmap.aiotm.data.Bounds;
import org.openstreetmap.aiotm.data.GarminTile;

public class AreasListParser {

	private final BufferedReader input;

	public AreasListParser(InputStream is) {
		InputStreamReader in;
		try {
			in = new InputStreamReader(is, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			in = new InputStreamReader(is);
			System.err.println("Sorry, your system doesn't support UTF8. WTF? Uninstall the Windows crap!");
		}
		this.input = new BufferedReader(in);
	}

	public List<GarminTile> parseAreas() {
		List<GarminTile> tiles = new ArrayList<GarminTile>();
		String line = null;
		String nextline = null;
		/*
		 * 2 example lines look:
		 * 
	70013001: 1492992,-1488896 to 2258944,-509952
	#       : 32.036133,-31.948242 to 48.471680,-10.942383

		 */
		Pattern l = Pattern.compile("^(\\d*)\\s*:\\s*-?\\d*,-?\\d*\\s*to\\s*-?\\d*,-?\\d*");
		String value = "-?\\d*\\.?\\d*";
		Pattern ll = Pattern.compile("^#\\s*:\\s*("+value+")\\s*,\\s*("+value+")\\s*to\\s*("+value+")\\s*,\\s*("+value+")");
		Matcher m;

		int id;
		try {
			while (( line = input.readLine()) != null){
				m = l.matcher(line);
				if (m.matches()) {
					id = Integer.parseInt(m.group(1));
					if (( nextline = input.readLine()) != null){
						m = ll.matcher(nextline);
						if (m.matches()) {
							Bounds b = new Bounds(Double.parseDouble(m.group(1)),Double.parseDouble(m.group(2)),Double.parseDouble(m.group(3)),Double.parseDouble(m.group(4)));
							tiles.add(new GarminTile(id,b,"",0));
						}
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Error while parsing areas.list");
		}
		return tiles;

	}

}
