package org.openstreetmap.aiotm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.openstreetmap.aiotm.data.GarminLayer;
import org.openstreetmap.aiotm.data.GarminTile;
import org.openstreetmap.aiotm.data.TypFile;

import uk.me.parabola.mkgmap.main.Main;

public class MkgmapController {


	public static File unGzip(String infilePath, String outDir) throws IOException {
		File infile = new File(infilePath);
		GZIPInputStream gin = new GZIPInputStream(new FileInputStream(infile));
		File outFile = new File(outDir, infile.getName().substring(0,infile.getName().lastIndexOf('.')));
		FileOutputStream fos = new FileOutputStream(outFile);
		byte[] buf = new byte[100000]; // Buffer size is a matter of taste and application...
		int len;
		while ( ( len = gin.read(buf) ) > 0 )
			fos.write(buf, 0, len);
		gin.close();
		fos.close();
		return outFile;
	}

	public static void createGmapsupp(List<GarminLayer> layers, String outDir) {
		for (GarminLayer l : layers) {
			if (l.getName().equals(GarminLayer.makeDummy().getName())) continue;
			List<String> mkgmapOptions = new ArrayList<String>();

			String typFilePath = "";
			TypFile tf;
			if ( (tf=l.getTypFile()) != null ) {
				typFilePath+=tf.getFilePath();
			}
			mkgmapOptions.add("--family-id="+l.getFamilyID());
			mkgmapOptions.add("--product-id="+l.getProductID());
			List<GarminTile> gtl = l.getSelected();
			MkgmapController.combineTiles(gtl,mkgmapOptions,outDir,typFilePath);
		}
	}

	public static void combineTiles(List<GarminTile> tiles, List<String> mkgmapOptions, String outDir, String typFilePath) {
		List<String> args;
		if (mkgmapOptions == null) {
			args = new ArrayList<String>();
		} else {
			args = mkgmapOptions;
		}
		args.add("--output-dir="+outDir);
		args.add("--gmapsupp");
		for (GarminTile t : tiles) {
			if (t.isCached()) {
				try {
					args.add(unGzip(t.getFilePath(),outDir).getAbsolutePath());
				} catch (IOException e) {
					System.err.println("Error while extracting "+t.getFilePath());
				}
			} else {
				System.out.println("Tile "+t.getNumber()+" is not cached so it is ignored. Please, download it first.");
			}
		}
		args.add(typFilePath);
		MkgmapController.callMkgmap(args);
	}

	public static void callMkgmap(List<String> args) {
		String argline = "mkgmap";
		for (String arg : args) {
			argline+=" "+arg;
		}
		System.out.println(argline);
		// call the mkgmap Main Class with arguments
		Main.main(args.toArray(new String[0]));
	}


}
