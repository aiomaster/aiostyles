package org.openstreetmap.aiotm.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.openstreetmap.aiotm.Aiotm;
import org.openstreetmap.aiotm.io.AreasListParser;
import org.openstreetmap.aiotm.io.DownloadListener;
import org.openstreetmap.aiotm.io.LayersIndexParser;

public class GarminLayerHandler implements DownloadListener {

	private final Map<String,GarminLayer> localLayers = new HashMap<String,GarminLayer>();
	private final Map<String,GarminLayer> availableLayers = new HashMap<String,GarminLayer>();
	private final Vector<GarminLayer> selectableLayers = new Vector<GarminLayer>();

	private final Vector<GarminLayerListener> listener = new Vector<GarminLayerListener>();

	public GarminLayerHandler() {
		selectableLayers.add(GarminLayer.makeDummy());

		/*
		List<GarminTile> comb = new ArrayList<GarminTile>();
		for (int i = 0; i<3 ; i++){

			GarminTile t = (GarminTile)mapRectangleList.get(i);
			t.setFilePath("/home/master/workspace/aiotm/7000300"+(i+1)+".img");
			comb.add(t);
		}
		MkgmapControler.combineTiles(comb);
		 */
	}

	public void lookupAvailableLayers() {
		GarminLayer dummy = selectableLayers.firstElement();
		selectableLayers.clear();
		selectableLayers.add(dummy);
		Aiotm.main.dm.downloadFile(Aiotm.main.pref.get("serverpath")+"/index/layers", new File(Aiotm.main.pref.get("cachedir"),"available/layers"), this);
	}

	public Vector<GarminLayer> getLayers() {
		return selectableLayers;
	}


	public void addGarminLayerListener(GarminLayerListener gll) {
		listener.add(gll);
	}

	public void removeGarminLayerListener(GarminLayerListener gll) {
		listener.remove(gll);
	}

	public void addListenerToLayer(GarminLayer l) {
		for (GarminLayerListener gll :  listener) {
			l.addGarminLayerListener(gll);
		}
	}

	public void notifyLayerlistChanged() {
		for (GarminLayerListener gll :  listener) {
			gll.afterLayerlistChange(selectableLayers);
		}
	}

	/*
	public List<GarminTile> getLayerTiles() {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream("/home/master/workspace/aiotm/areas.list");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new AreasListParser(fis).parseAreas();
	}
	 */
	@Override
	public void fileLoadingFinished(File f, boolean success) {
		FileInputStream in = null;
		try {
			in = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		File layerPath = new File(Aiotm.main.pref.get("cachedir"),"available");
		if (f.getName().equals("layers")) {
			availableLayers.clear();
			GarminLayer dummy = GarminLayer.makeDummy();
			availableLayers.put(dummy.getName(), dummy);
			Vector<GarminLayer> layers = new LayersIndexParser(in).parseLayers();
			for (GarminLayer l : layers) {
				availableLayers.put(l.getName(), l);
				Aiotm.main.dm.downloadFile(Aiotm.main.pref.get("serverpath")+"/index/"+l.getName()+"/areas.list", new File(layerPath,l.getName()+"/areas.list"), this);
			}
		} else if (f.getName().equals("areas.list")) {
			String layername = f.getParentFile().getName();
			GarminLayer l;
			if ((l = availableLayers.get(layername)) != null){
				l.addAllTiles(new AreasListParser(in).parseAreas());
				selectableLayers.add(l);
				addListenerToLayer(l);
				notifyLayerlistChanged();
			}
		}
	}

}
