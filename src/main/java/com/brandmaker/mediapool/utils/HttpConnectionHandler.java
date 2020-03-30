package com.brandmaker.mediapool.utils;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HttpConnectionHandler
{

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpConnectionHandler.class);

	private static final boolean DEBUG = false;

	protected CookieManager cmgr = new CookieManager();

	public HttpURLConnection openUrlConnection(String uri, Map<String,Map<String, String>>addcookies) throws MalformedURLException, IOException, URISyntaxException
	{
		return openUrlConnection(uri, "GET", addcookies);
	}
	/**
	 * <p>Open a URL connection and follow redirects to the final destination:
	 *
	 * <p>As the ...ty API makes redirects, we can not pass the URI directly to the parser
	 * but need to open a stream which follows the redirects.
	 *
	 * @param uri
	 * @param method
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public HttpURLConnection openUrlConnection(String uri, String method, Map<String,Map<String, String>>addcookies) throws MalformedURLException, IOException, URISyntaxException
	{
		HttpURLConnection conn = null;

		boolean follow = true;
		while ( follow )
		{
			conn = connectUri(uri, method);

			if ( addcookies != null )
			{
				for ( String name : addcookies.keySet() )
				{
					Map<String,String>cookie = addcookies.get(name);
					cmgr.setCookieValue(conn, name, cookie.get(name));
				}
			}
			cmgr.setCookies(conn);

			conn.connect();
			cmgr.storeCookies(conn);

			int status = conn.getResponseCode();
			if (status != HttpURLConnection.HTTP_OK)
			{
				if (status == HttpURLConnection.HTTP_MOVED_TEMP
					|| status == HttpURLConnection.HTTP_MOVED_PERM
						|| status == HttpURLConnection.HTTP_SEE_OTHER)
				{
					uri = conn.getHeaderField("Location");
					conn.disconnect();
				}
				else
				{
					LOGGER.info("Error on connect: " + status );
					String response = this.getRequestResponseString(conn);
					LOGGER.info( "Response: " + response );

					throw new IOException("Error on connect: " + status);
				}
			}
			else
				follow = false;
		}
//		LOGGER.info(SEVERITY.TRACE, "GET: " + cmgr.toString() );

		return conn;
	}

	public HttpURLConnection connectUri(String url, String method) throws MalformedURLException, IOException {
		URL mpUrl = new URL(url);
//		LOGGER.info("Open: " + url);

		HttpURLConnection.setFollowRedirects(true);
		HttpURLConnection  conn = (HttpURLConnection)mpUrl.openConnection();

		conn.setReadTimeout(1000*60*3); // 3 Minutes to wait for data
		conn.setConnectTimeout(1000*60*3); // 3 Minutes to wait for connection

		conn.setDoOutput(true);
		conn.setDoInput(true);

		conn.setRequestMethod(method);
		conn.setRequestProperty("Connection", "close");
		conn.setRequestProperty("User-Agent", "WebCache - https://www.brandmaker.com");
		conn.setInstanceFollowRedirects(true);

		return conn;
	}

	public HttpURLConnection connectUri(String url) throws MalformedURLException, IOException {
		return 	connectUri(url, "GET");
	}

	public InputStream getRequestInputStream(HttpURLConnection conn) {
		String error;
		try
		{
			conn.connect();
			return (conn.getInputStream());
		}
		catch ( IOException ioe )
		{
			InputStream err = conn.getErrorStream();
			if ( err != null )
			{
				error = readErrorResponse(new BufferedReader(new InputStreamReader(err)));
				LOGGER.info("Error Response: " + error );
	//			ioe.printStackTrace();
			}
			else
				LOGGER.info( "Connection error" );
		}
		return null;
	}

	public byte[] getRequestResponseBytes(HttpURLConnection conn)
	{
		byte[] response = null;
		String error;
		try
		{
			conn.connect();
			response = getRequestResponseBytes(conn.getInputStream());
			conn.disconnect();
		}
		catch ( IOException ioe )
		{
			InputStream err = conn.getErrorStream();
			if ( err != null )
			{
				error = readErrorResponse(new BufferedReader(new InputStreamReader(err)));
				LOGGER.info("Error Response: " + error );
	//			ioe.printStackTrace();
			}
			else
				LOGGER.info( "Connection error" );
		}
		return response;
	}

	public String getRequestResponseString(HttpURLConnection conn) {
		String response = null;
		try
		{
			conn.connect();
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			response = getRequestResponseString(rd);
			conn.disconnect();
		}
		catch ( IOException ioe )
		{
			InputStream err = conn.getErrorStream();
			if ( err != null )
			{
				response = readErrorResponse(new BufferedReader(new InputStreamReader(err)));
				LOGGER.info("Error Response: " + response );
	//			ioe.printStackTrace();
			}
			else
				LOGGER.info( "Connection error" );
		}
		return response;
	}

	public String readErrorResponse(BufferedReader rd) {
		String response = null;
		try
		{
			response = getRequestResponseString(rd);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return response;
	}

	public ByteArrayOutputStream getRequestResponseByteStream(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] byteChunk = new byte[4096*2];
		int n;

		while ((n = is.read(byteChunk)) > 0) {
			baos.write(byteChunk, 0, n);
		}
		LOGGER.info("got " + baos.size()/(1024.0*1024.0) + " MB" );
		return baos;
	}

	public byte[] getRequestResponseBytes(InputStream is) throws IOException {
		return getRequestResponseByteStream(is).toByteArray();
	}

	public String getRequestResponseString(BufferedReader rd) throws IOException {
		String line;
		String response = "";
//		LOGGER.info("read response");
		while ((line = rd.readLine()) != null) {
//			LOGGER.info("read " + line);
		    response += line;
		}
		rd.close();
		return response;
	}

	public String getDataString(HashMap<String, String> params) throws UnsupportedEncodingException{
	    StringBuilder result = new StringBuilder();
	    boolean first = true;
	    for(Map.Entry<String, String> entry : params.entrySet()){
	        if (first)
	            first = false;
	        else
	            result.append("&");    
	        result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
	        result.append("=");
	        result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
	    }    
	    return result.toString();
	}
	
	public byte[] getResponse(String method, String serviceUrl, HashMap<String, String> body, HashMap<String, String> headers) throws UnsupportedEncodingException, MalformedURLException, IOException 
	{
		byte[] postData = null;
		
		if ( body != null && body.size() > 0 )
			postData = getDataString(body).getBytes( StandardCharsets.UTF_8 );
		
		if ( DEBUG ) LOGGER.info( serviceUrl + ": " + new String(postData));
		
		HttpURLConnection conn = connectUri(serviceUrl, method );
		conn.setUseCaches(false);
		
		if ( headers != null ) {
			for ( Entry<String, String> entry : headers.entrySet() ) {
				conn.setRequestProperty(entry.getKey(), entry.getValue()); 
			}
		}
		if ( postData != null ) {
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
			conn.setRequestProperty("charset", "utf-8");
			conn.setRequestProperty("Content-Length", Integer.toString(postData.length ));
			try(DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
			   wr.write( postData );
			}
		}
		byte[] response = getRequestResponseBytes(conn);
		return response;
	}

}