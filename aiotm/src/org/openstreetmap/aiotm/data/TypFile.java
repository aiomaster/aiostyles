package org.openstreetmap.aiotm.data;

public class TypFile {

	private final String name;
	private String path;

	public TypFile(String n){
		name = n;
	}

	public String getName() {
		return name;
	}

	public void setFilePath(String p) {
		path = p;
	}

	public String getFilePath() {
		return path;
	}

}
