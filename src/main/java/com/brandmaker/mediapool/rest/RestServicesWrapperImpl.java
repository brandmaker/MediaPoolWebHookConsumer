package com.brandmaker.mediapool.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.brandmaker.mediapool.MediaPoolAsset;
import com.brandmaker.mediapool.utils.HttpConnectionHandler;
import com.brandmaker.mediapool.utils.OauthCredentials;
import com.brandmaker.mediapool.webhook.MediaPoolEvent;
import com.brandmaker.mediapool.webhook.WebhookException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * 
 * Encapsulation of all REST API calls to Media Pool
 * 
 * @author axel.amthor
 *
 */
public class RestServicesWrapperImpl extends HttpConnectionHandler implements RestServicesWrapper
{
	private static final Logger LOGGER = LoggerFactory.getLogger(RestServicesWrapper.class);

	private static final String MEDIAID_TPL = "{MEDIAID}";
	
	private static final long YEAR_MILLISECONDS = 365L*24L*60L*60L*1000L;

	/**
	 * Request Body to retrieve an asset by it's ID and all necessary attributes from the REST search API.
	 * @see <a href="https://nexus.dev.brandmaker.com/repository/documentation/com.brandmaker.mms/mediapool-rest-api/53.0.0-6.3-SNAPSHOT/rest-api/resource_SmartSearchRestService.html#top">https://nexus.dev.brandmaker.com/repository/documentation/com.brandmaker.mms/mediapool-rest-api/53.0.0-6.3-SNAPSHOT/rest-api/resource_SmartSearchRestService.html#top</a>
	 * 
	 * 
	 * sample response:
	{
	    "description": {
	        "@type": "text",
	        "value": ""
	        },
	    "description_multi": {
	        "@type": "multilang",
	        "value": {"EN": ""}
	        },
	    "title": {
	        "@type": "text",
	        "value": "DFU_All_Inclusive_Brochure_DSL14-0310.US_LIT29-A-242_REV12-14"
	        },
	    "actualVersionNumber": {
	        "@type": "long",
	        "value": 0
	        },
	    "uploadApprovalData": {
	        "@type": "object",
	        "fields": {
	            "approveStateType": {
	                "@type": "text",
	                "value": "APPROVED"
	                },
	            "approveState_multi": {
	                "@type": "multilang",
	                "value": {"EN": "Approved"}
	                },
	            "approveState": {
	                "@type": "text",
	                "value": "Approved"
	                }
	            }
	        },
	    "channelPublications": {
	        "@type": "object_set",
	        "items": [{
	            "@type": "object",
	            "fields": {
	                "renderingScheme": {
	                    "@type": "long",
	                    "value": 856
	                    },
	                "channelId": {
	                    "@type": "text",
	                    "value": "SHARE"
	                    }
	                }
	            }]
	        },
	    "themes": {
	        "@type": "object_set",
	        "items": [{
	            "@type": "object",
	            "fields": {
	                "id": {
	                    "@type": "long",
	                    "value": 34
	                    },
	                "text": {
	                    "@type": "text",
	                    "value": "/Test"
	                    },
	                "text_multi": {
	                    "@type": "multilang",
	                    "value": {"EN": "/Test"}
	                    }
	                }
	            }]
	        },
	    "uploadDate": {
	        "@type": "date",
	        "value": "2016-03-17T09:52:07Z"
	        },
	    "vdb": {
	        "@type": "object",
	        "fields": {
	            "name_multi": {
	                "@type": "multilang",
	                "value": {"EN": "Generally Available Data"}
	                },
	            "id": {
	                "@type": "long",
	                "value": 2
	                }
	            }
	        },
	    "title_multi": {
	        "@type": "multilang",
	        "value": {"EN": "DFU_All_Inclusive_Brochure_DSL14-0310.US_LIT29-A-242_REV12-14"}
	        },
	    "lastUpdatedTime": {
	        "@type": "date",
	        "value": "2020-02-12T21:11:27Z"
	        },
	    "id": {
	        "@type": "long",
	        "value": 3467
	        },
	    "hideIfNotValid": {
	        "@type": "bool",
	        "value": false
	        }
	    }

	 */
	private static final String SEARCH_ID_REQUEST_BODY = ""
			+ "{" +
			"	\"searchSchemaId\": \"asset\"," +
			"	\"lang\": \"EN\"," +
			"	\"output\": {" +
			"		\"items\": {" +
			"			\"fields\": [" +
			"				\"id\", " +
			"				\"title\", " +
//			"				\"forcesomeerror\", " +
			"				\"title_multi\", " +
			"				\"actualVersionNumber\", " +
			"				\"validFrom\", " +
			"				\"validTo\", " +
			"				\"description\", " +
			"				\"description_multi\", " +
			"				\"lastUpdatedTime\", " +
			"				\"themes\", " +
			"				\"alternativeImage\", " +
			"				\"hideIfNotValid\", " +
			"				\"publishFrom\", " +
			"				\"publishTo\", " +
			"				\"uploadDate\", " +
			"				\"programVersion\", " +
			"				\"language\", " +
			"				\"uploadApprovalData\", " +
			"			]," +
			"			\"objects\": [" +
			"				{\"name\": \"vdb\", \"fields\": [\"id\", \"name_multi\"]}" +
			"			]," +
			"			 \"objectSets\": [" +
			"				{\"name\": \"channelPublications\", \"fields\": [\"channelId\", \"renderingScheme\", \"publishedFrom\", \"publishedTo\"]}" +
			"            ]" +
			"		}," +
			"		\"paging\": {" +
			"			\"@type\": \"offset\"," +
			"			\"offset\": 0," +
			"			\"limit\": 25" +
			"		}," +
			"		\"sorting\": [{" +
			"			\"@type\": \"field\"," +
			"			\"field\": \"lastUpdatedTime\"," +
			"			\"asc\": false" +
			"		}]" +
			"	}," +
			"	\"criteria\": {" +
			"		\"@type\": \"and\"," +
			"		\"subs\": [{" +
			"			\"@type\": \"and\"," +
			"			\"subs\": [{" +
			"				\"@type\": \"not\"," +
			"				\"criteria\": {" +
			"					\"@type\": \"eq\"," +
			"					\"fields\": [\"vdb.id\"]," +
			"					\"long_value\": 3" +
			"				}" +
			"			}]" +
			"		}, {" +
			"			\"@type\": \"and\"," +
			"			\"subs\": [{" +
			"				\"@type\": \"match\"," +
			"				\"fields\": [\"id\"]," +
			"				\"value\": \"" + MEDIAID_TPL + "\"" +
			"			}]" +
			"		}]" +
			"	}" +
			"}";

	/** the media pool user from the application.yaml */
	@Value("${spring.application.system.user:#{null}}")
	private Optional<String> user;
	
	/** the media pool password from the application.yaml */
	@Value("${spring.application.system.password:#{null}}")
	private Optional<String> password;
	
	/** the oAuth2credentials file path from the application.yaml */
	@Value("${spring.application.system.oAauth2CredentialsFile:#{null}}")
	private Optional<String> oauthCredentialsFile;
	
	/* (non-Javadoc)
	 * @see com.brandmaker.webcache.core.asset.services.mediapool.RestServicesWrapper#createDownloadTask(java.lang.String, org.apache.sling.commons.json.JSONObject, com.brandmaker.webcache.core.tenant.WebCacheTenant)
	 */
	@Override
	public String createDownloadTask(String downloadUrl, JSONObject taskRequest) throws WebhookException {
		
		HttpURLConnection vconn = null;
		
		LOGGER.debug("Requesting binary from " + downloadUrl);
		
		try {
			LOGGER.debug(taskRequest.toString(4));
			
			vconn = connectUri(downloadUrl, "POST");
	
			handleCookies(downloadUrl, vconn);
			cmgr.setCookies(vconn);
	
			vconn.setRequestProperty("Authorization", getAuthentication() );
			vconn.setRequestProperty("Content-Type", "application/json");
			
			String rqBody = taskRequest.toString(4);
			LOGGER.debug(rqBody);

			vconn.connect();
			
			vconn.getOutputStream().write(rqBody.getBytes(), 0, rqBody.length());
	
			cmgr.storeCookies(vconn);
			
			int rc = vconn.getResponseCode();
			
			if ( rc != 201 ) // we only accept "201 - created"
			{
				String errorResponse = getRequestResponseString(vconn);
				LOGGER.error(errorResponse);
				throw new WebhookException(errorResponse);
			}
			
			
			String data = getRequestResponseString(vconn);
			vconn.disconnect();

			if ( data != null )
			{
				JSONObject responseObject = new JSONObject(data);
				
				String taskId = responseObject.getString("id");

				LOGGER.info("got new download task id: " + taskId);
				
				return taskId;
				
			}
			
		}
		catch ( WebhookException e )
		{
			throw e;
		}
		catch ( Exception e )
		{
			LOGGER.error("(2) An error", e);
		}
		finally {
			if ( vconn != null )
				vconn.disconnect();
		}

		return null;
	}
	
	
	/* (non-Javadoc)
	 * @see com.brandmaker.mediapool.rest.RestServicesWrapper#loadPublishingData(com.brandmaker.mediapool.webhook.MediaPoolEvent, com.brandmaker.mediapool.MediaPoolAsset)
	 */
	@Override
	public JSONObject loadPublishingData(MediaPoolEvent mediaPoolEvent, MediaPoolAsset mpAsset) throws Exception {
		
		JSONObject pubData = null;
		HttpURLConnection conn = null;
		String versionUrl = mediaPoolEvent.getBaseUrl() + "/rest/mp/assets/" + mpAsset.getMediaID() + "/versions/" + mpAsset.getVersion();
		
		versionUrl += "?expand=publishedChannels";
		LOGGER.info(versionUrl );
		
		try {
			
			conn = connectToDownloadTask(versionUrl);
			
			cmgr.storeCookies(conn);
	
			int rc = conn.getResponseCode();
	
			LOGGER.info("Response code is " + rc );
	
			if ( rc != 200 ) {
	
				LOGGER.error("Error on versions API: " + rc);
				return null;
			}
	
			String data = getRequestResponseString(conn);
			LOGGER.info(data );
			
			if ( data != null ) {
				pubData = new JSONObject(data);
			}
		}
		catch ( Exception e ) {
			
		}
		finally {
			if ( conn != null )
				conn.disconnect();
		}
		return pubData;
	}

	@Override
	public JSONObject getVersionInfo(MediaPoolEvent mediaPoolEvent) throws Exception
	{
		JSONObject versionInfo = null;
		HttpURLConnection vconn = null;

		try
		{

			String versionUrl = mediaPoolEvent.getBaseUrl() + "/rest/mp/versions/assets/" + mediaPoolEvent.getAssetId();

			LOGGER.debug("Retrieving versions from " + versionUrl);

			vconn = connectToDownloadTask(versionUrl);

			cmgr.storeCookies(vconn);

			int rc = vconn.getResponseCode();

			LOGGER.debug("Response code is " + rc );

			if ( rc != 200 ) {

				LOGGER.error("Error on versions API: " + rc);
				return null;
			}

			String data = getRequestResponseString(vconn);
			vconn.disconnect();

			if ( data != null ) {

				int highestVersion = -1;
				int highestVersionId = -1;
				int officialVersionId = -1;
				JSONArray versionsArray = new JSONArray(data);

				for ( int n = 0; n < versionsArray.length(); n++ ) {

					JSONObject version = versionsArray.getJSONObject(n);
					int vn = version.getInt("versionNumber");
					if ( vn > highestVersion ) {
						highestVersion = vn;
						highestVersionId = n;
					}

					boolean off = version.getBoolean("official");
					if ( off ) {
						officialVersionId = n;
					}

				}

				if (officialVersionId >= 0 )
					versionInfo = versionsArray.getJSONObject(officialVersionId);
				else
					versionInfo = versionsArray.getJSONObject(highestVersionId);
			}

		}
		catch ( Exception e )
		{
			LOGGER.error("(2) An error", e);
		}
		finally {
			if ( vconn != null )
				vconn.disconnect();
		}

		return versionInfo;
	}
	
	@Override
	public JSONObject getAssetData(MediaPoolEvent event)
	{
		HttpURLConnection mdconn = null;
		try
		{
			if ( event.getAssetId() == null ) {
				// sometimes the MP doesn't provide the array of asset IDs in the sync event.
				// this is a bug in older builds, which we are catching here
				LOGGER.error("Asset ID missing!");
				return null;
			}

			String restSearchUrl = event.getBaseUrl() + "/rest/mp/v1.1/search";

			LOGGER.info("Retrieving meta data from " + restSearchUrl);

			mdconn = connectUri(restSearchUrl, "POST");

			handleCookies(restSearchUrl, mdconn);
			cmgr.setCookies(mdconn);

			mdconn.setRequestProperty("Authorization", getAuthentication() );
			mdconn.setRequestProperty("Content-Type", "application/json");

			JSONObject requestObject = this.getSearchIdRequestBody(event.getAssetId());
			String rqBody = requestObject.toString(4);
			LOGGER.debug(rqBody);

			mdconn.connect();

			mdconn.getOutputStream().write(rqBody.getBytes(), 0, rqBody.length());
			cmgr.storeCookies(mdconn);

			int rc = mdconn.getResponseCode();

			LOGGER.info("Response code is " + rc );

			if ( rc != 200 ) {

				LOGGER.error("Error on search API: \"" + rc + " - " + mdconn.getResponseMessage() + "\" on URL " + restSearchUrl);
				InputStream err = mdconn.getErrorStream();
				if ( err != null )
				{
					String response = readErrorResponse(new BufferedReader(new InputStreamReader(err)));
					LOGGER.info("Error Response: " + response );
				}
				else
					LOGGER.info("Cannot read error respone");
				
				return null;
			}

			String data = getRequestResponseString(mdconn);
			mdconn.disconnect();

			if ( data != null )
			{
				JSONObject metaObject = new JSONObject(data);

				if ( metaObject.getLong("totalHits") == 0 ) {
					LOGGER.error("Asset not found by ID {}: " + metaObject.getLong("totalHits"), event.getAssetId());
					return null;
				}
				
				if ( metaObject.getLong("totalHits") > 1 ) {
					LOGGER.error("Ambigous result for ID {}: " + metaObject.getLong("totalHits"), event.getAssetId());
					return null;
				}

				return metaObject;
			}

		}
		catch ( Exception e )
		{
			LOGGER.error("(3) An error", e);
		}
		finally {
			if ( mdconn != null )
				mdconn.disconnect();
		}
		return null;
	}

	/**
	 * @param finalUrl
	 * @param mdconn
	 * @throws MalformedURLException
	 */
	private void handleCookies(String finalUrl, HttpURLConnection mdconn) throws MalformedURLException
	{
		Map<String, Map<String, String>>cookies = cmgr.getCookies(new URL(finalUrl));
		if ( cookies != null )
		{
			for ( String name : cookies.keySet() )
			{
				Map<String,String>cookie = cookies.get(name);
				cmgr.setCookieValue(mdconn, name, cookie.get(name));
			}
		}
	}

	/**
	 * Get the Authentication Header content. This is either "Basic" or "Bearer", according to the settings in the application.yaml
	 * 
	 * @see /src/main/resources/application.yaml 
	 * 
	 * @return
	 * @throws WebhookException
	 */
	private String getAuthentication() throws WebhookException {

		
		String auth = null;
		OauthCredentials credentials = getOAuthCredentials();
		if ( credentials != null ) {
			
			
			/*
			 * Let's check whether we have tokens, then let's use those:
			 */
			String accessToken = credentials.getAccessToken().getToken();
			String refreshToken = credentials.getRefreshToken().getToken();
			
			GregorianCalendar refreshExpires = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
			refreshExpires.setTime( credentials.getRefreshToken().getExpires() );
			
			GregorianCalendar accessExpires = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
			accessExpires.setTime( credentials.getAccessToken().getExpires() );
			
			
			if ( accessToken != null && refreshToken != null ) {
				
				GregorianCalendar calNow = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
				calNow.add(Calendar.SECOND, -10); // 10 seconds before effective expiration
				
				LOGGER.debug("Access token expires on " + toGMTString(accessExpires));
				LOGGER.debug("Refresh token expires on " + toGMTString(refreshExpires));
				LOGGER.debug("Now " + toGMTString(calNow));
				
				if ( calNow.after(accessExpires) ) {
					
					/*
					 * access token is expired, retrieve a new one
					 */
					LOGGER.info("Access token expired on " + toGMTString(accessExpires));
					
					if ( calNow.after(refreshExpires) ) {
						// refresh expired, we cannot login anymore
						LOGGER.error("Refresh token (also) expired on " + toGMTString(refreshExpires));
						throw new WebhookException("All tokens expired");
					}
					else
					{
						try {
							
							accessToken = refreshAndPresistTokens(credentials, accessToken, refreshExpires, accessExpires);
								
						} catch ( Exception e ) {
							LOGGER.error("Error refreshing tokens: " + e);
							throw new WebhookException("Error refreshing tokens");
						}
					}
				}
				
				auth = new String("Bearer " + accessToken );
			}
		}
		else {
			auth = getBasicAuth();
		}
		
		LOGGER.debug("Auth: " + auth);
		return auth;
		
    }


	/**
	 * Refresh all of the tokens and persist them in the credentials file given in the application settings
	 * 
	 * @param credentials
	 * @param accessToken
	 * @param refreshExpires
	 * @param accessExpires
	 * 
	 * @return the newly generated access token which can be used in subsequent calls
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws JSONException
	 */
	private String refreshAndPresistTokens(OauthCredentials credentials, String accessToken, GregorianCalendar refreshExpires, GregorianCalendar accessExpires)
			throws MalformedURLException, IOException, JSONException {
		
		String refreshToken;
		LOGGER.info("Refreshing tokens");
		
		// get the current time in order to later (!) calculate the expiration timestamp. 
		// if we do it after the POST call, we might have some glitches of a few seconds ...
		long tNow = System.currentTimeMillis();
		
		JSONObject refreshedTokensObject = refreshTokensFromCAS(credentials);
		
		if ( refreshedTokensObject != null ) {
					
			// store back these tokens !!!
			
			accessToken = refreshedTokensObject.getString("access_token");
			long expires = ( refreshedTokensObject.getLong("expires_in")*1000L ) + tNow;
			accessExpires.setTimeInMillis(expires);
			
			credentials.getAccessToken().setToken(accessToken);
			credentials.getAccessToken().setExpires(accessExpires.getTime());
			
			refreshToken = refreshedTokensObject.getString("refresh_token");
			refreshExpires.setTimeInMillis( YEAR_MILLISECONDS + tNow); // 365d is standard CAS
			
			credentials.getRefreshToken().setToken(refreshToken);
			credentials.getRefreshToken().setExpires(refreshExpires.getTime());
			
			LOGGER.info("Persisting new tokens");
			storeOAuthCredentials(credentials);
		}
		return accessToken;
	}
	
	/**
	 * Send a token refresh request to CAS with the given credentials etc.
	 * 
	 * @param refreshUrl
	 * @param postDataBytes
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public JSONObject refreshTokensFromCAS(OauthCredentials credentials) throws MalformedURLException, IOException {
		
		// retrieve new tokens from CAS (!!)
		String clientId = credentials.getClientId();
		String refreshUrl = credentials.getServer();
		String grant_type = "refresh_token";
		
		HashMap <String, String>postData = new HashMap<>();
		postData.put("client_id", clientId);
		postData.put("client_secret", credentials.getClientSecret());
		postData.put("grant_type", grant_type);
		postData.put("refresh_token", credentials.getRefreshToken().getToken());
		
		byte[] postDataBytes = getDataString(postData).getBytes( StandardCharsets.UTF_8 );
		
		HttpURLConnection conn;
		conn = connectUri(refreshUrl, "POST");
		
		LOGGER.debug("Post data: " + new String (postDataBytes));
		
		conn.setRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
		conn.setRequestProperty("charset", StandardCharsets.UTF_8.name());
		conn.setRequestProperty("Content-Length", Integer.toString(postDataBytes.length ));
		conn.connect();
		
		OutputStream os = conn.getOutputStream();
		os.write(postDataBytes);
		os.flush();
		os.close();
		
		InputStream is = null;
		try {
			is = conn.getInputStream();
		
		} catch ( IOException ioe ) {
			is = conn.getErrorStream();
			LOGGER.error("(1) Token refresh rejected: " + conn.getResponseCode());
		}
		
		String response = new String(is.readAllBytes());
		int responseCode = conn.getResponseCode();
		
		LOGGER.debug("Response code is " + responseCode );
		LOGGER.debug("Response is " + response );
		
		if ( response != null && response.length() > 0 ) {
			JSONObject resultObject = new JSONObject(response);
			LOGGER.debug("response: " + resultObject.toString(4));
			
			if ( responseCode != 200 || resultObject.has("status_code") ) {
				// indicates some error ...
				LOGGER.error("code: " + responseCode + " - response: " + resultObject.toString(4));
				return null;
			}
			return resultObject;
		}	
		return null;
	}

	
	/**
	 * Store the (new) tokens etc. back to the credentials file as JSON
	 * 
	 * @param credentials
	 */
	private void storeOAuthCredentials(OauthCredentials credentials ) {
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(oauthCredentialsFile.get()), credentials);
			
			
		} catch ( Exception e ) {
			LOGGER.error("Error serializing new tokens to '" + oauthCredentialsFile.get() + "' ", e);
		}
	}

	/**
	 * Deserialize the credentials file into PoJo OauthCredentials
	 * @see https://www.jsonschema2pojo.org/
	 * 
	 * @return credentials object, null if there is none or on any error
	 */
	private OauthCredentials getOAuthCredentials() {
		OauthCredentials credentials = null;
		
		if ( this.oauthCredentialsFile.isPresent() && !this.oauthCredentialsFile.get().isEmpty() ) {
			try {
				ObjectMapper mapper = new ObjectMapper();
	
				//JSON file to Java object
				credentials = mapper.readValue(new File(oauthCredentialsFile.get()), OauthCredentials.class);
	
				LOGGER.debug(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(credentials));
				
				return credentials;
				
			} catch ( Exception e ) {
				LOGGER.error("Invalid configuration: given credentails file '" + oauthCredentialsFile.get() + "' either missing or not well formatted", e);
			}
		}
		return null;
	}


	private String getBasicAuth() {
		String userPassword = user.get() + ":" + password.get();
		LOGGER.debug("Credentials: " + userPassword );
		
		return new String("Basic: " + Base64.getEncoder().encode(userPassword.getBytes()));
	}


	/**
	 * Get a download stream for the content of this asset.
	 * The stream is opened when it's requested, not before, in order to reduce load on the pool system!!
	 * Means: an asset with LoadStreamData.GETINPUTSTREAM does not have any binary asset data at all as long
	 * as they are not explicitly requested.
	 * @param downloadUrl
	 *
	 * @return
	 */
	@Override
	public InputStream getDataInputStream(String downloadUrl) {

		LOGGER.debug("retrieving input stream for " + downloadUrl);
		InputStream dataInputStream = null;
		HttpURLConnection conn = null;
		
		try
		{
			conn = pollDownloadTask(downloadUrl);

			int rc = conn.getResponseCode();
			
			if ( rc == 200 ) {
				dataInputStream = conn.getInputStream();
				return (dataInputStream);
			}
			
		}
		catch (Exception e)
		{
			LOGGER.error("(4) An error", e);
		}

		return null;
	}
	
	/**
	 * Get a download stream for the content of this asset.
	 * The stream is opened when it's requested, not before, in order to reduce load on the pool system!!
	 * Means: an asset with LoadStreamData.GETINPUTSTREAM does not have any binary asset data at all as long
	 * as they are not explicitly requested.
	 * @param downloadUrl
	 *
	 * @return
	 */
	@Override
	public InputStream getDataInputStream(HttpURLConnection conn) {
		InputStream dataInputStream = null;
		try
		{
			int rc = conn.getResponseCode();
			
			if ( rc == 200 ) {
				dataInputStream = conn.getInputStream();
				return (dataInputStream);
			}
			
		}
		catch (Exception e)
		{
			LOGGER.error("(4) An error", e);
		}

		return null;
	}

	/**
	 * @param downloadUrl
	 * @param tenant
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws WebhookException 
	 */
	@Override
	public HttpURLConnection pollDownloadTask(String downloadUrl) throws MalformedURLException, IOException, InterruptedException, WebhookException 
	{
		if ( downloadUrl == null || downloadUrl.isEmpty() )
			return null;
		
		long start = System.currentTimeMillis();
		int maxtries = 5*1000*6; // that's half an hour !!
		HttpURLConnection conn;
		conn = connectToDownloadTask(downloadUrl);
		
		int rc = conn.getResponseCode();
		
		while ( rc == 202 && --maxtries > 0 )
		{
			conn.disconnect();
			
			LOGGER.info("Task " + downloadUrl+ " not yet ready ...");
			Thread.sleep(5 * 1000);
			
			conn = connectToDownloadTask(downloadUrl);
			rc = conn.getResponseCode();
		}
		LOGGER.info("Polling for rendition took " + (System.currentTimeMillis() - start) + " msec, rc = " + rc);
		return conn;
		
	}

	/**
	 * @param downloadUrl
	 * @param tenant
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws WebhookException 
	 */
	private HttpURLConnection connectToDownloadTask(String downloadUrl) throws MalformedURLException, IOException, WebhookException 
	{
		
		cmgr.getCookies(new URL(downloadUrl));
		
		HttpURLConnection conn;
		conn = connectUri(downloadUrl, "GET");
		
		handleCookies(downloadUrl, conn);
		cmgr.setCookies(conn);

		conn.setRequestProperty("Authorization", getAuthentication() );
		conn.setRequestProperty("Content-Type", "application/json");
		
		conn.connect();
		return conn;
	}

	/**
	 * @return the searchIdRequestBody
	 */
	private JSONObject getSearchIdRequestBody(String assetId)
	{
		try
		{
			JSONObject requestObject = new JSONObject(SEARCH_ID_REQUEST_BODY.replace(MEDIAID_TPL, assetId));
			LOGGER.debug("Request: " + requestObject.toString(4));
			return requestObject;
			
		}
		catch (JSONException e)
		{
			LOGGER.error("JSON Error", e);
		}
		return null;
	}
	
	
	/**
	 * Format a GregorianCalendar Object to a string with "yyyy-MM-dd HH:mm:ss z"
	 *
	 * @return formatted datetime string with TZ
	 */
	private static String toGMTString(GregorianCalendar date)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssXXX");
		String gmt = sdf.format(date.getTime());
		return gmt;
	}

}

