package ru.johnlife.lifetools.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.net.http.AndroidHttpClient;
import android.util.Log;

public class NetworkClient {
	private static final String TAG = "NetworkClient";
	private static final int BUFFER_SIZE = 64*1024;
	private static final int TIMEOUT = 20000;
	private static final int READ_TIMEOUT = 180000;
	private static final long JSESSION_TIMEOUT = 1800000; //30*60*1000
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:17.0) Gecko/20100101 Firefox/17.0";
	private static AndroidHttpClient httpClient = AndroidHttpClient.newInstance(USER_AGENT);
	private static final Object lock = new Object();
	private static String jSessionId = null;
	private static long jSessionTimestamp;
	private static long lastSuccess;
	private static Thread timer = null;
	private static int active = 0;
	public static final HttpParams TIMEOUT_SHORT = new BasicHttpParams();
	public static final HttpParams TIMEOUT_LONG = new BasicHttpParams();

	static {
		HttpParams httpParameters = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 20000);

		HttpConnectionParams.setSoTimeout(TIMEOUT_SHORT, 20000);
		HttpConnectionParams.setSoTimeout(TIMEOUT_LONG, 180000);
	}

	public static HttpResponse execute(HttpRequestBase httpPost) throws IOException{
		synchronized (lock) {
			if (httpClient == null) {
				httpClient = AndroidHttpClient.newInstance(USER_AGENT);
				HttpParams httpParameters = httpClient.getParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, 20000);
				if (System.currentTimeMillis() - jSessionTimestamp > JSESSION_TIMEOUT) {
					jSessionId = null; //timed out
				}
			}
			active++;
		}
		httpPost.setHeader("Cookie", jSessionId );
		HttpResponse response;
		try {
			response = httpClient.execute(httpPost);
		} catch (IllegalStateException e) {
			Log.e(TAG, "Got Illegal state while requesting", e);
			//reset and retry
			reset();
			return execute(httpPost);
		}
		Header[] headers = response.getHeaders("Set-Cookie");
		for (Header header : headers) {
			String headerValue = header.getValue();
			if (headerValue.startsWith("JSESSIONID=")) {
				jSessionId = headerValue.substring(0, headerValue.lastIndexOf(';'));
				jSessionTimestamp = System.currentTimeMillis();
				Log.d("JSession", jSessionId);
			}
		}
		synchronized (lock) {
			active--;
		}
		lastSuccess = System.currentTimeMillis();
		if (timer == null || !timer.isAlive()) {
			timer = new Thread() {
				@Override
				public void run() {
					boolean reading = active > 0;
					if ((!reading && (System.currentTimeMillis() - lastSuccess) > TIMEOUT) ||
						(reading && (System.currentTimeMillis() - lastSuccess) > READ_TIMEOUT))
					{
						if (reading) {
							Log.w(TAG, "Resetting while reading!");
						} else {
							Log.d(TAG, "Resetting after succesfull read.");
						}
						reset();
					} else {
						try {
							sleep(3000);
							run();
						} catch (InterruptedException e) {}
					}
				}		
			};
			timer.start();
		}
		return response;
	}

	public static synchronized void reset() {
		if (null != httpClient) {
			ClientConnectionManager manager = httpClient.getConnectionManager();
			manager.closeIdleConnections(8000, TimeUnit.MILLISECONDS);
			manager.shutdown();
			((AndroidHttpClient) httpClient).close();
			httpClient = null;
		} 
	}

	protected static String getTagContent(InputStream is, String tagName, String matchPattern) throws IOException {
		final String startTag = "<"+tagName;
//		final String classAttr = "class=\""+matchPattern+'"';
		final String endTag = "</"+tagName;
		BufferedInputStream bis = new BufferedInputStream(is, BUFFER_SIZE);
		BufferedReader reader = new BufferedReader(new InputStreamReader(bis));
		boolean found = false;
		StringBuilder tagBuilder = new StringBuilder();
		int count = 0;
		String line = "";
		while ((line=reader.readLine()) != null) {
			if (!found) {
				found = line.contains(startTag) && line.contains(matchPattern); 
			}
			if (found) {
				if (line.contains(startTag)) count++;
				tagBuilder.append(line);
				if (line.contains(endTag)) {
					if (--count == 0) {
						return tagBuilder.toString();
					}
				}
			}
		}
		bis.close();
		reader.close();
		return tagBuilder.toString();
	}

	/**
	 * Tag name and it's class declaration MUST be in a single line in source HTML for this method to work
	 */
	public static String getTagContent(HttpRequestBase httpMethod, String tagName, String matchPattern) throws IOException{
		HttpResponse response = execute(httpMethod);
		InputStream is = response.getEntity().getContent();
		String value = getTagContent(is, tagName, matchPattern);
		is.close();
		return value;
	}
	
	
}
