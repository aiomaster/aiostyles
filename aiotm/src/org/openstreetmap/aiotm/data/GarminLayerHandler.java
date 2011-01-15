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

	private final Vector<GarminLayer> layerList = new Vector<GarminLayer>();
	private final Map<String, GarminLayer> loadingLayers = new HashMap<String, GarminLayer>();

	private final Vector<GarminLayerListener> listener = new Vector<GarminLayerListener>();

	public GarminLayerHandler() {
		layerList.add(GarminLayer.makeDummy());

	}

	public void lookupAvailableLayers() {
		GarminLayer dummy = layerList.firstElement();
		layerList.clear();
		layerList.add(dummy);
		Aiotm.main.dm.downloadFile(Aiotm.main.pref.get("serverpath")+"/index/layers", new File(Aiotm.main.pref.get("cachedir"),"available/layers"), this);
	}

	public Vector<GarminLayer> getLayers() {
		return layerList;
	}

	public GarminLayer getLayerByName(String name) {
		for (GarminLayer l : layerList) {
			if (l.getName().equals(name))
				return l;
		}
		return null;
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
			gll.afterLayerlistChange(layerList);
		}
	}

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
			Vector<GarminLayer> layers = new LayersIndexParser(in).parseLayers();
			for (GarminLayer l : layers) {
				loadingLayers.put(l.getName(), l);
				Aiotm.main.dm.downloadFile(Aiotm.main.pref.get("serverpath")+"/index/"+l.getName()+"/areas.list", new File(layerPath,l.getName()+"/areas.list"), this);
			}
		} else if (f.getName().equals("areas.list")) {
			String layername = f.getParentFile().getName();
			GarminLayer l;
			if ((l = loadingLayers.get(layername)) != null){
				if (!layerList.contains(l)) {
					l.addAllTiles(new AreasListParser(in).parseAreas());
					addListenerToLayer(l);
					layerList.add(l);
				} else {
					System.err.println("Layer \""+l.getName()+"\" exists local.");
				}
				loadingLayers.remove(l);
				notifyLayerlistChanged();
			} else {
				System.err.println("Layer \""+layername+"\" could not be found in loading list.");
			}
		}
	}

}
