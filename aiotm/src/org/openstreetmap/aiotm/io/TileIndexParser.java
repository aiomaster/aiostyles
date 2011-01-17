package org.openstreetmap.aiotm.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Vector;

import org.openstreetmap.aiotm.data.Bounds;
import org.openstreetmap.aiotm.data.GarminTile;

public class TileIndexParser {
	private final BufferedReader input;

	public TileIndexParser(InputStream is) {
		InputStreamReader in;
		try {
			in = new InputStreamReader(is, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			in = new InputStreamReader(is);
			System.err.println("Sorry, your system doesn't support UTF8. WTF? Uninstall the Windows crap!");
		}
		this.input = new BufferedReader(in);
	}

	public List<GarminTile> parseTiles() {
		List<GarminTile> tiles = new Vector<GarminTile>();
		String line = null;

		try {
			while (( line = input.readLine()) != null){
				// comments begin with #
				if (line.startsWith("#")) continue;
				// split by colon
				String[] tileInfos = line.split(":");
				// we know at the moment: MD5:TILENUMBER:SPLITVERSION:DATE:BOUNDS
				if (tileInfos.length == 5) {
					String hash = tileInfos[0];
					int id = Integer.parseInt(tileInfos[1]);
					long d = Long.parseLong(tileInfos[3]);
					String[] bbox = tileInfos[4].split(",");
					Bounds b = new Bounds(Double.parseDouble(bbox[0]),Double.parseDouble(bbox[1]),Double.parseDouble(bbox[2]),Double.parseDouble(bbox[3]));
					GarminTile t = new GarminTile(id,b,hash,d);

					tiles.add(t);
				}
			}
		} catch (IOException e) {
			System.err.println("Error while parsing tiles index");
		}
		return tiles;

	}
}
