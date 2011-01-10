package org.openstreetmap.aiotm.io;

import java.io.File;

public interface DownloadListener {
	public void fileLoadingFinished(File f, boolean success);
}
