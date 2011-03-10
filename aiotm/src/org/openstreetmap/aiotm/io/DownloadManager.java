package org.openstreetmap.aiotm.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.openstreetmap.gui.jmapviewer.JobDispatcher;


public class DownloadManager {
	/**
	 * Holds the used user agent used for HTTP requests. If this field is
	 * <code>null</code>, the default Java user agent is used.
	 */
	public static String USER_AGENT = null;
	public static String ACCEPT = "*/*";

	private final JProgressBar bar = new JProgressBar();
	private final JLabel speedLabel = new JLabel();

	JobDispatcher jobDispatcher;

	int downloadTasks = 0;
	int received = 0;

	public DownloadManager() {
		jobDispatcher = JobDispatcher.getInstance();
	}

	public JProgressBar getProgressBar() {
		return bar;
	}

	public synchronized void addGlobalJobSize(int jobsize){
		bar.setMaximum(bar.getMaximum()+jobsize);
	}

	public synchronized void updateGlobalBar(int done){
		bar.setValue(bar.getValue()+done);
	}

	public JLabel getSpeedLabel() {
		return speedLabel;
	}

	public void downloadFile(String url, File file, DownloadListener listener) {
		downloadFile(url,file,listener,bar);
	}

	public void downloadFile(String url, File file, DownloadListener listener, JProgressBar bar) {
		jobDispatcher.addJob(createFileLoaderJob(url,file,listener,bar));
	}

	private synchronized void incTasks(int n) {
		downloadTasks+=n;
	}

	private synchronized void incReceivedKiloBytes(int r) {
		received+=r;
	}

	private synchronized void setReceivedKiloBytes(int r) {
		received=r;
	}

	public int measureDownloadSpeed() {
		setReceivedKiloBytes(0);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			System.err.println("TimeMeasuring interrupted");
		}
		return received/3;
	}

	public Runnable createFileLoaderJob(final String url, final File file, final DownloadListener listener, final JProgressBar filebar) {
		incTasks(1);
		if (downloadTasks == 1) {
			new Thread(new Runnable(){

				@Override
				public void run() {
					while (downloadTasks > 0) {
						speedLabel.setText(measureDownloadSpeed()+"KB/s");
					}
				}

			}).start();
		}

		return new Runnable() {

			public void run() {

				try {
					System.out.println("Download from: "+url+"  to: "+file.getAbsolutePath());

					// Thread.sleep(500);
					URLConnection conn = loadFile(url);

					int size = conn.getContentLength();
					synchronized (filebar) {
						filebar.setMaximum(size);
					}
					addGlobalJobSize(size);
					InputStream input = conn.getInputStream();
					file.getParentFile().mkdirs();
					FileOutputStream f = new FileOutputStream(file);

					byte[] data = new byte[8192];
					int count;
					while ((count = input.read(data)) != -1) {
						f.write(data,0,count);
						synchronized (filebar) {
							filebar.setValue(filebar.getValue()+count);
						}
						updateGlobalBar(count);
						incReceivedKiloBytes(count/1024);
					}

					input.close();
					f.close();
					incTasks(-1);
					if (listener != null)
						listener.fileLoadingFinished(file, true);
				} catch (Exception e) {
					if (listener != null)
						listener.fileLoadingFinished(file, false);
					System.err.println("failed loading "  + e.getMessage());

				}
			}

		};
	}

	public long getRemoteFileDate(String url) {
		URLConnection uc = null;
		long timestamp = 0;
		try {
			uc = loadFile(url);
			timestamp = uc.getHeaderFieldDate("Last-Modified", 0l);
		} catch (IOException e) {
			System.err.println("No such file on server: "+url);
		}
		return timestamp;
	}

	protected URLConnection loadFile(String urlstring) throws IOException {
		URL url;
		url = new URL(urlstring);
		URLConnection urlConn = url.openConnection();
		if (urlConn instanceof HttpURLConnection) {
			prepareHttpUrlConnection((HttpURLConnection)urlConn);
		}
		urlConn.setReadTimeout(30000); // 30 seconds read timeout
		return urlConn;
	}

	protected void prepareHttpUrlConnection(HttpURLConnection urlConn) {
		if (USER_AGENT != null) {
			urlConn.setRequestProperty("User-agent", USER_AGENT);
		}
		urlConn.setRequestProperty("Accept", ACCEPT);
	}
}
