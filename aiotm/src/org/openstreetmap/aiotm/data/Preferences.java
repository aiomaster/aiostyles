package org.openstreetmap.aiotm.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;

public class Preferences {

	public final Properties getDefaultProperties() {
		Properties def = new Properties();
		def.setProperty("cachedir", (new File(getAppPath(),"cache")).getAbsolutePath());
		def.setProperty("serverpath", "http://dev.openstreetmap.de/aio/garmintiles");
		return def;
	}

	private final Properties props = new Properties(getDefaultProperties());

	public Preferences() {
		loadProperties();
	}

	private File getAppPath() {
		File aiotmDir;
		String path = System.getenv("APPDATA");
		if (path != null) {
			aiotmDir = new File(path, "AIOTM");
		} else {
			aiotmDir = new File(System.getProperty("user.home"), ".aiotm");
		}
		aiotmDir.mkdirs();
		return aiotmDir;
	}

	private File getPropertiesFile() {
		return new File(getAppPath(),"properties");
	}

	public void saveProperties() {
		try {
			File propFile = getPropertiesFile();
			props.store(new OutputStreamWriter(new FileOutputStream(propFile), "utf-8"),"All-in-One-TileManager Configuration File");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadProperties() {
		try {
			File propFile = getPropertiesFile();
			if (propFile.exists()) {
				props.load(new InputStreamReader(new FileInputStream(propFile), "utf-8"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String get(String key) {
		return props.getProperty(key);
	}

}
