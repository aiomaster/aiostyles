package org.openstreetmap.aiotm.util;

import java.util.List;

import org.openstreetmap.aiotm.data.GarminTile;

import uk.me.parabola.mkgmap.main.Main;

public class MkgmapController {

	public static void combineTiles(List<GarminTile> tiles) {
		int count =  tiles.size();
		String[] args = new String[count+1];
		args[0] = "--gmapsupp";
		for (int i = 1; i <= count; i++) {
			args[i] = tiles.get(i-1).getFilePath();
		}

	}

	public static void callMkgmap(List<String> args) {
		// call the mkgmap Main Class with arguments
		Main.main((String[])args.toArray());
	}


}
