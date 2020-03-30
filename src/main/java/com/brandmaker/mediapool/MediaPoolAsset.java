package com.brandmaker.mediapool;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.mail.internet.ContentDisposition;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brandmaker.mediapool.rest.RestServicesWrapper;
import com.brandmaker.mediapool.webhook.MediaPoolEvent;
import com.brandmaker.mediapool.webhook.WebhookException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonInclude;


/**
 * Media Object reflecting XML and JSON structure of Mediapool API
 *
 * @see <a href="https://jira4.brandmaker.com/confluence/display/MP/GetMediaForExternalApplication.do">GetMediaForExternalApplication.do</a>
 * @see <a href="https://nexus.dev.brandmaker.com/repository/documentation/com.brandmaker.mms/mediapool-rest-api/53.0.0-6.3-SNAPSHOT/rest-api/resource_AssetRestService.html#resource_AssetRestService_findByIdOfficialVersion_GET">AssetRestService_findByIdOfficialVersion_GET</a>
 *
 */
public class MediaPoolAsset
{
	@JsonIgnore
	private static final Logger LOGGER = LoggerFactory.getLogger(MediaPoolAsset.class);

	private String id;
	
	@JsonIgnore
	private long numId;

	/** indicates, whether all data has been loaded succsessfully */
	public enum states {ready, fault, missing};
	@JsonIgnore
	private states state = states.fault;

	private String mediaTitle;
	private Hashtable<String, String> mediaTitles = new Hashtable<String, String>();

	private String filename;
	private String suffix;

	private String generated_filename;
	private String mimeType;
	private String compressionType;
	private String version;
	private String width;
	private String height;
	private String units;
	private long vdbId;
	
	@JsonIgnore
	private String downloadUrl;

	private String mediaDescription;
	private Hashtable<String, String> mediaDescriptions = new Hashtable<String, String>();
	private Hashtable<String, Long> channels = new Hashtable<String,Long>();
	private Hashtable<String, String> vdbNames = new Hashtable<String, String>();
	
	/**
	 * Binary data read from download url;
	 */
	@JsonIgnore
	byte[] byteData = null;

	private Date officialFrom;

	private Date lastUpdateDate;
	private Date lastUploadDate;

	private String mediaID;

	@JsonIgnore
	private InputStream dataInputStream;
	
	private boolean hasMediaTitleSetByField = false;
	private long fileSizeKiloBytes;

	private HashSet<String> assignedCategoryIds = new HashSet<String>();

	@JsonIgnore
	private String serviceUrl;

	@JsonIgnore
	private MediaPoolEvent mediaPoolEvent;
	@JsonIgnore
	private RestServicesWrapper restService;

	@JsonIgnore
	private String downloadTaskId;

	public class PropertyMapper<T>
	{

		@SuppressWarnings("unchecked")
		PropertyMapper(JSONObject data, String jsonPath) throws Exception
		{
			JSONObject jsonObject = data;
			T value = null;

			String[] jsonPathElements = jsonPath.split("\\.");

			Object jobj = jsonObject;

			int numElements = jsonPathElements.length;

			for ( int n = 0; n < numElements; n++ ) {

				if ( n + 1 == numElements ) // last element
				{
					if ( ! ((JSONObject)jobj).has(jsonPathElements[n]) )
						continue;

					LOGGER.debug("mapping " + jsonPathElements[n]);
					
					Object subObject = ((JSONObject)jobj).get(jsonPathElements[n]);

					// check class of JSON element
					if ( subObject instanceof JSONObject ) {
						
						// analyze type hint in element
						String type = ((JSONObject)subObject).getString("@type");

						try {
							if ( type.equals("multilang") && ((JSONObject)subObject).has("value") ) {
								value = (T) getLangValues( ((JSONObject)subObject).getJSONObject("value") );
							}
							else if ( type.equals("object_set")  )
							{
								if ( ((JSONObject)subObject).has("items") )
									value = (T) ((JSONObject)subObject).get("items");
								else
									value = null;
							}
							else if ( type.equals("long") && ((JSONObject)subObject).has("value") )
							{
								Long v = ((JSONObject)subObject).getLong("value");
								value = (T) v;
							}
							else if ( ((JSONObject)subObject).has("value")  )
								value = (T) ((JSONObject)subObject).get("value");
							else {
								value = null;
								LOGGER.error("Not recognized " + type + " = " + ((JSONObject)data).toString(4));
							}
						}
						catch ( JSONException j )
						{
							LOGGER.info("JSON Error: " + ((JSONObject)subObject).toString(4));
						}
						
						if ( value != null ) LOGGER.debug("Cast " + value.getClass().getName() + " Value " + jsonPathElements[n] + " / " + type);
					}
					else
						value = (T) subObject; // assign element vlaue of type (T)
						
				}
				else
				{
					LOGGER.debug("Object " + jsonPathElements[n] );
					jobj = ((JSONObject)jobj).get(jsonPathElements[n]);
				}
			}
			
			// call generic setter. this is overriden in general
			set(value);
		}

		/**
		 * @param value
		 * @return
		 * @throws JSONException
		 */
		private Hashtable<String, String> getLangValues(JSONObject value) throws JSONException
		{
			Hashtable<String, String>langvals = new Hashtable<String, String>();

			Iterator<String> props = value.keys();
			while ( props.hasNext() ) {
				String prop = props.next();
				String val = value.getString(prop);
				langvals.put(prop, val);
			}
			return langvals;
		}

		public void set (T t) throws Exception {};

		public T get() { return null; };

	}

	/**
	 * create a new and empty MediaPool Asset in context of an event
	 * 
	 * @throws MalformedURLException
	 */
	public MediaPoolAsset(RestServicesWrapper restService, MediaPoolEvent event) throws MalformedURLException
	{
		this.mediaPoolEvent = event;
		this.restService = restService;

		this.serviceUrl = event.getBaseUrl();
		
		try {
			LOGGER.debug(event.toJson().toString(4) );
		} 
		catch (JSONException e) {
			LOGGER.error("An error", e);
		}
	}
	
	/**
	 * The publishing info is loaded with the search into this object
	 * This method will add this information to the event structure, as in the SYNC event this is missing.
	 * This is only necessary for the SYNC event, as there is no Publishing information send
	 * 
	 * Current structure:
	 * <pre>
{
    "tenantId": 8997,
    "customerId": "kfb-kzk-nbn",
    "systemId": "902-489-410",
    "baseUrl": "https://is-dev2.brandmaker.com",
    "assetId": "35566",
    "eventType": "SYNCHRONIZE",
    "eventTime": 1581543493000
}
	 * </pre>
	 * Required structure:
	 * <pre>
{
    "tenantId": 8997,
    "customerId": "kfb-kzk-nbn",
    "systemId": "902-489-410",
    "baseUrl": "https://is-dev2.brandmaker.com",
    "assetId": "3467",
    "eventData": [{
        "channelId": "PUBLIC_LINKS",
        "startDate": null,
        "endDate": null,
        "renderingScheme": 856
        }],
    "eventType": "PUBLISHED",
    "eventTime": 1552667068052
}
	 * </pre>
	 *
	 */
	private void mapPublishingData() {
		
		JSONArray eventData = new JSONArray();
		
		try {
			
			for ( Entry<String, Long> chnEntry : getChannels().entrySet() ) {
				JSONObject eentry = new JSONObject();
				eentry.put(MediaPoolEvent.PROP_CHANNELID, chnEntry.getKey());
				eentry.put(MediaPoolEvent.PROP_RENDERINGSCHEME, chnEntry.getValue() );
				eventData.put(eentry);
			}
			mediaPoolEvent.setPayloadArray(eventData);
		} 
		catch (Exception e) {
			LOGGER.error("Error", e);
		}
		
	}

	/**
	 * Retrieve asset data from MP REST API
	 * Available from 6.3 release on (!)
	 *
	 */
	private boolean loadAssetMetaData()
	{

		try
		{
			/*
			 * get the assets basic meta data from the search API by its asset ID
			 */
			JSONObject result = restService.getAssetData(mediaPoolEvent);
			
			// in case of error or no result, this is already catched and returns a null pointer
			if ( result == null ) {
				setState(states.fault);
				return false;
			}
			LOGGER.debug(result.toString(4) );
			
			// if the result is not structured like this we let it crash hard:
			JSONObject metaObject = result.getJSONArray("items").getJSONObject(0).getJSONObject("fields");
			LOGGER.debug(metaObject.toString(4) );

			/*
			 * map meta data to object properties
			 */
			extractMetaInformation(metaObject);

			/*
			 * retrieve the versions and file ressource data and pick official / latest version
			 */
			JSONObject versionInfo = restService.getVersionInfo( mediaPoolEvent );
			LOGGER.debug(versionInfo != null ? versionInfo.toString(4)  : "No version info found ..:" );

			/*
			 * map to object properties
			 */
			if ( versionInfo != null )
				extractVersionInformation(versionInfo);

			setState(states.ready);

			return true;
		}
		catch (JSONException e)
		{
			LOGGER.error("JSON error", e);
			setState(states.fault);
		}
		catch (Exception e)
		{
			LOGGER.error("Error loading asset data: " + e.getMessage(), e);
			setState(states.fault);
		}
		return false;

	}

	/**
	 * retrieve information from the versions object
	 *
	 * @param versionInfo
	 * @throws Exception
	 */
	private void extractVersionInformation(JSONObject versionInfo) throws Exception
	{
		if ( versionInfo.has("fileResource") ) {
			JSONObject fileResource = versionInfo.getJSONObject("fileResource");
			LOGGER.debug( fileResource.toString(4) );
			
			new PropertyMapper<String>(fileResource, "fileName") {
				@Override
				public void set(String value) {
					setFilename(value);
				}
			};
	
			new PropertyMapper<String>(fileResource, "generatedName") {
				@Override
				public void set(String value) {
					setGenerated_filename(value);
				}
			};
	
			new PropertyMapper<String>(fileResource, "compression") {
				@Override
				public void set(String value) {
					setCompressionType(value);
				}
			};
	
			new PropertyMapper<Integer>(fileResource, "fileSize") {
				@Override
				public void set(Integer value) {
					if ( value != null )
						setFileSizeKiloBytes(value);
				}
			};
	
			new PropertyMapper<String>(fileResource, "mimeType") {
				@Override
				public void set(String value) {
					setMimeType(value);
				}
			};
	
			new PropertyMapper<String>(fileResource, "suffix") {
				@Override
				public void set(String value) {
					setSuffix(value);
				}
			};
	
			new PropertyMapper<Integer>(fileResource, "width") {
				@Override
				public void set(Integer value) {
					if ( value != null )
						setWidth(value.toString());
				}
			};
	
			new PropertyMapper<Integer>(fileResource, "height") {
				@Override
				public void set(Integer value) {
					if ( value != null )
						setHeight(value.toString());
				}
			};
		}

		new PropertyMapper<Integer>(versionInfo, "versionNumber") {
			@Override
			public void set(Integer value) {
				if ( value != null )
					setVersion(value.toString());
				else
					setVersion("0");
			}
		};

		new PropertyMapper<String>(versionInfo, "insertedTime") {
			@Override
			public void set(String value) throws ParseException {

				Date lu = parseRFC3339Date(value);

				setLastUploadDate(lu);
			}
		};
	}
	
	/**
	 * parse a non-well-formed Media Pool Date String
	 *
	 * Media Pool timestamps are like "2018-02-16T08:06:11+01:00" which cannot be parsed directly
	 * as the colon ':' leads to unexpected results
	 *
	 *
	 *
	 * @param datestring
	 * @return
	 * @throws java.text.ParseException
	 * @throws IndexOutOfBoundsException
	 */
	public static java.util.Date parseRFC3339Date(String datestring) throws java.text.ParseException, IndexOutOfBoundsException {
		Date d = null;

		if ( datestring == null || datestring.trim().isEmpty() )
			return null;
		
//		LOGGER.info("Parsing " + datestring);
		
		// if there is no time zone, we don't need to do any special parsing.
		if (datestring.endsWith("Z")) {
			try {
				SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // spec for RFC3339
				d = s.parse(datestring);
				return d;
			} 
			catch (java.text.ParseException pe) {	// try again with optional
													// decimals
				SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"); // spec for RFC3339 (with fractional seconds)
				s.setLenient(true);
				d = s.parse(datestring);
				return d;
			}
		}

		// step one, split off the timezone.

		if ( datestring.charAt(datestring.length()-6) == '-' || datestring.charAt(datestring.length()-6) == '+' )
		{
			char splitter = datestring.charAt(datestring.length()-6);
			String firstpart = datestring.substring(0, datestring.lastIndexOf(splitter));
			String secondpart = datestring.substring(datestring.lastIndexOf(splitter));

			// step two, remove the colon from the timezone offset
			secondpart = secondpart.substring(0, secondpart.indexOf(':')) + secondpart.substring(secondpart.indexOf(':') + 1);
			datestring = firstpart + secondpart;
		}
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");// spec for RFC3339
		
		try {
			d = s.parse(datestring);
			return d;
		} 
		catch (java.text.ParseException pe) {// try again with optional decimals
			s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ"); // spec for RFC3339 (with  fractional seconds)
			s.setLenient(true);
			d = s.parse(datestring);
			return d;
		}
	}


	/**
	 * @param metaObject
	 * @throws Exception
	 */
	private void extractMetaInformation(JSONObject metaObject) throws Exception
	{
		
		/*
		 * The action is taken within the construtor: pick the according staructor from the json, convert it and call the set method down below.
		 * This needs to be overwritten to assign the converted value from the JSON to the proper fields in the parent class.
		 * 
		 */
		new PropertyMapper<Long>(metaObject, "id") {
			
			/* (non-Javadoc)
			 * @see com.brandmaker.webcache.core.asset.services.mediapool.MediaPoolAsset.PropertyMapper#set(java.lang.Object)
			 */
			@Override
			public void set(Long value) {
				if ( value != null )
					setId(value.toString());
				else {
					try {
						LOGGER.info("ID null ??? " + metaObject.toString(4));
					} catch (JSONException e) {
						LOGGER.error("Error", e);
					}
				}
			}
		};

		new PropertyMapper<JSONArray>(metaObject, "channelPublications") {
			String channelId;
			String from;
			String to;
			Long renderingScheme;
			
			/**
			 * <p>See com.brandmaker.webcache.core.asset.services.mediapool.MediaPoolAsset.PropertyMapper#set(java.lang.Object)
			 * 
			 * <p>Fetch the list of publishing channels.
			 * We only save ID and Rendering Scheme after we have checked the from and to dates.
			 * The list of publcation channels may be empty afterwards and it may contain channels, which are not pointing to 
			 * the channels ("SHARE", "PUBLIC_LINKS") we are listening on
			 */
			@Override
			public void set(JSONArray value) throws Exception {

				
				LOGGER.debug(value != null ? value.toString(4) : " - is null?");
				
				if ( value == null )
					return;
				
				int chns  = value.length();
				for ( int n = 0; n < chns; n++ )
				{
					JSONObject chnentry = value.getJSONObject(n);
					setChannelId(null);
					setRenderingScheme(-1L);
					setFrom(null);
					setTo(null);
					
					LOGGER.debug(chnentry.toString(4) );
					
					new PropertyMapper<String>(chnentry, "fields.channelId") {
						@Override
						public void set(String value) throws JSONException {
							setChannelId(value);
						}
					};
					
					if ( channelId != null && mediaPoolEvent.getMySyncChannels() != null && mediaPoolEvent.getMySyncChannels().contains(channelId) ) {
						
						new PropertyMapper<String>(chnentry, "fields.publishedFrom") {
							@Override
							public void set(String value) throws JSONException {
								setFrom(value);
							}
						};
						
						new PropertyMapper<String>(chnentry, "fields.publishedTo") {
							@Override
							public void set(String value) throws JSONException {
								setTo(value);
							}
						};
						
						new PropertyMapper<Long>(chnentry, "fields.renderingScheme") {
							@Override
							public void set(Long value) throws JSONException {
								setRenderingScheme(value);
							}
						};
						
						if ( getChannelId() != null && getRenderingScheme() >= 0 ) {
							channels.put(getChannelId(), getRenderingScheme());
						}
						
						// check pub dates, if we are outside the range, we ignore the channel!
						if ( from != null && !from.isEmpty() ) {
							if ( parseRFC3339Date(from).after(new Date()) ) {
								LOGGER.info("future release on " + getChannelId());
								continue;
							}
						}
						
						if ( to != null && !to.isEmpty() ) {
							if ( parseRFC3339Date(to).before(new Date()) ) {
								LOGGER.info("expired release on " + getChannelId() + " on " + to);
								continue;
							}
						}
					}
					
				}
				LOGGER.debug(channels.toString() );
				
			}

			/**
			 * @return the channelId
			 */
			public String getChannelId() {
				return channelId;
			}

			/**
			 * @param channelId the channelId to set
			 */
			public void setChannelId(String channelId) {
				this.channelId = channelId;
			}

			/**
			 * @return the renderingScheme
			 */
			public Long getRenderingScheme() {
				return renderingScheme;
			}

			/**
			 * @param renderingScheme the renderingScheme to set
			 */
			public void setRenderingScheme(Long renderingScheme) {
				this.renderingScheme = renderingScheme;
			}

			/**
			 * @return the from
			 */
			@SuppressWarnings("unused")
			public String getFrom() {
				return from;
			}

			/**
			 * @param from the from to set
			 */
			public void setFrom(String from) {
				this.from = from;
			}

			/**
			 * @return the to
			 */
			@SuppressWarnings("unused")
			public String getTo() {
				return to;
			}

			/**
			 * @param to the to to set
			 */
			public void setTo(String to) {
				this.to = to;
			}
		};
		
		new PropertyMapper<JSONArray>(metaObject, "themes") {
			@Override
			public void set(JSONArray value) throws Exception {

				assignedCategoryIds = new HashSet<String>();

				// fetch the list of assigned categories of this asset. Only ID is relevant.
				int ctgs  = value != null ? value.length() : 0;
				for ( int n = 0; n < ctgs; n++ )
				{
					JSONObject ctgentry = value.getJSONObject(n);
					LOGGER.debug(ctgentry.toString(4) );

					new PropertyMapper<Long>(ctgentry, "fields.id") {
						@Override
						public void set(Long value) throws JSONException {
							if ( value != null )
								assignedCategoryIds.add(value.toString());
						}
					};
				}
			}
		};

		new PropertyMapper<String>(metaObject, "title") {
			@Override
			public void set(String value) throws JSONException {
				setMediaTitle(value);
			}
		};

		new PropertyMapper<Hashtable<String, String>>(metaObject, "title_multi") {
			@Override
			public void set(Hashtable<String, String> value) throws JSONException {
				setMediaTitles(value);
			}
		};

		new PropertyMapper<String>(metaObject, "lastUpdatedTime") {
			@Override
			public void set(String value) throws ParseException {

				Date lu = parseRFC3339Date(value);

				setLastUpdateDate(lu);
			}
		};

		new PropertyMapper<String>(metaObject, "uploadDate") {
			@Override
			public void set(String value) throws ParseException {

				if ( value != null && !value.isEmpty() ) {
					Date lu = parseRFC3339Date(value);
	
					setLastUploadDate(lu);
				}
			}
		};

		new PropertyMapper<String>(metaObject, "description") {
			@Override
			public void set(String value) throws WebhookException {
				setDescription(value);
			}

		};

		new PropertyMapper<Hashtable<String, String>>(metaObject, "description_multi") {
			@Override
			public void set(Hashtable<String, String> value) throws JSONException {
				setDescriptions(value);
			}

		};
		
		new PropertyMapper<Hashtable<String, String>>(metaObject, "vdb.fields.name_multi") {
			@Override
			public void set(Hashtable<String, String> value) throws JSONException {
				setvdbNames(value);
			}

		};

		new PropertyMapper<Long>(metaObject, "vdb.fields.id") {
			@Override
			public void set(Long value) throws WebhookException {
				setVdbId(value);
			}
		};
	}

	/**
	 * Load binary Data in to this Object.
	 * This first starts a new download task and saves the task id in the download URL
	 *
	 * @throws Exception
	 *
	 */
	private boolean loadAdditionalAssetInformation() throws Exception
	{

		if ( this.isStateReady() && mediaPoolEvent.needsBinary() ) {
			
			setDownloadUrl(serviceUrl + "/rest/mp/v1.2/file-generation-task");
			
			LOGGER.info("ID: " + getMediaID() );
			LOGGER.info("Version: " + getVersion() );
			LOGGER.info("Rendition: " +  mediaPoolEvent.getRenderingScheme() );
			LOGGER.info("Channels: " +  mediaPoolEvent.getChannelsFromPayload().toString() );
			
			if ( this.getMediaID() == null || this.getVersion() == null || mediaPoolEvent.getRenderingScheme() == null ) {
				downloadTaskId = null;
				setDownloadUrl(null);
			}
			else {
				
				// start downloading the binary data
				try {
					// issue the rendering task request in order to have the binary loaded lateron
					JSONObject  taskRequest = new JSONObject();
					taskRequest.put("@type", "published_asset"); 
					taskRequest.put("assetId", this.getMediaID() );
					taskRequest.put("versionNumber", this.getVersion());
					taskRequest.put("renderingSchemeId",  mediaPoolEvent.getRenderingScheme()); 
					
					downloadTaskId = restService.createDownloadTask(getDownloadUrl(), taskRequest);
					
					if ( downloadTaskId != null && !downloadTaskId.isEmpty() )
						setDownloadUrl(serviceUrl + "/rest/mp/v1.2/download/file-generation-task/" + downloadTaskId);
				}
				catch ( WebhookException e )
				{
					// creation of download task has failed ... ?
					this.downloadTaskId = null;
					setDownloadUrl(null);
					LOGGER.error("Creating download task failed");
					return false;
				}
			}
		}
		else {
			this.downloadTaskId = null;
			setDownloadUrl(null);
		}
		
		return true;
	}

	/**
	 * Get a download stream for the content of this asset.
	 * The stream is opened when it's requested, not before, in order to reduce load on the pool system!!
	 * Means: an asset with LoadStreamData.GETINPUTSTREAM does not have any binary asset data at all as long
	 * as they are not explicitly requested.
	 *
	 * @return
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws javax.mail.internet.ParseException 
	 * @throws WebhookException 
	 */
	public InputStream getDataInputStream() throws MalformedURLException, IOException, InterruptedException, javax.mail.internet.ParseException, WebhookException {

		if ( isStateReady() )
		{
			
			/*
			 * as the requested rendering scheme may not be of the same type as the original file,
			 * we need to correct the mime type and Suffix of the asset here (!).
			 */
			HttpURLConnection conn = restService.pollDownloadTask(downloadUrl);
			
			if ( conn != null && conn.getResponseCode() == 200 ) 
			{
				String mimeType = conn.getContentType();
				String disposition = conn.getHeaderField("Content-Disposition");
				ContentDisposition cd = new ContentDisposition(disposition);
				String filename = cd.getParameter("filename");
				String suffix = FilenameUtils.getExtension(filename);
				filename = FilenameUtils.getBaseName(filename);
				
				LOGGER.info("Content-Disposition: " + filename + " : " + suffix + " : " + mimeType);
				
				this.setSuffix(suffix);
				this.setMimeType(mimeType);
				
				dataInputStream = restService.getDataInputStream(conn);
				
			}
			else {
				dataInputStream = null;
				throw new WebhookException("Error downloading binary data from '" + downloadUrl + "': " + (conn != null ? conn.getResponseCode() : "-") );
			}
			
		}
		return dataInputStream;
	}
	
	
	public JSONObject toJson() {
		 
        ObjectMapper mapper = new ObjectMapper(); 
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        try { 
  
           String jsonStr = mapper.writeValueAsString(this); 
  
           JSONObject jobj = new JSONObject(jsonStr);
           
           return jobj;
           
        }
        catch ( Exception e ) {
        	LOGGER.error("some error", e);
        }
		return null;
	}
	
	/* ============== Getters and Setters ==================================== */

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
		if ( id.startsWith("M-") )
		{
			this.numId = Long.parseLong(id.substring(2)); // numeric w/o the "M-"
			this.mediaID = id.substring(2);
		}
		else
		{
			this.numId = Long.parseLong(id); // numeric w/o the "M-"
			this.mediaID = id;
		}


	}

	public String getMediaTitle() {
		return mediaTitle;
	}

	public void setMediaTitle(String mediaTitle) {
		this.mediaTitle = mediaTitle;
	}

	public String getMediaTitle(String lang) {
		return mediaTitles.get(lang);
	}

	public void setMediaTitles(String lang, String title) {
		this.mediaTitles.put(lang, title);
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getGenerated_filename() {
		return generated_filename;
	}

	public void setGenerated_filename(String generated_filename) {
		this.generated_filename = generated_filename;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mediaType) {
		this.mimeType = mediaType;
	}

	public String getCompressionType() {
		return compressionType;
	}

	public void setCompressionType(String compressionType) {
		this.compressionType = compressionType;
	}

	public String getDescription() {
		return mediaDescription;
	}

	public void setDescription(String itemDescription) {
		this.mediaDescription = itemDescription;
	}

	public String getDescription(String lang) {
		return mediaDescriptions.get(lang);
	}

	public void setDescription(String lang, String desc) {
		this.mediaDescriptions.put(lang, desc);
	}

	public String getCategoriesAsString() {
		return StringUtils.join(assignedCategoryIds, ",");
	}

	public HashSet<String> getCategories() {
		return assignedCategoryIds;
	}

	public void addCategorie(String id) {
		this.assignedCategoryIds.add(id);
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public Date getOfficialFrom() {
		return officialFrom;
	}

	public void setOfficialFrom(Date officialFrom) {
		this.officialFrom = officialFrom;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public Hashtable<String, String> getMediaTitles() {
		return mediaTitles;
	}

	public Hashtable<String, String> getDescriptions() {
		return mediaDescriptions;
	}

	public String getSuffix() {

		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public String getOriginalSuffix() {
		return suffix;
	}

	@JsonIgnore
	public boolean isStateReady()
	{
		return (getState() == states.ready);
	}

	@JsonIgnore
	public states getState() {
		return state;
	}

	@JsonIgnore
	public void setState(states state) {
		this.state = state;
	}


	public long getNumId() {
		return numId;
	}


	public String getVersion() {
		return version;
	}


	public void setVersion(String version) {
		this.version = version;
	}


	public byte[] getByteData() {
		return byteData;
	}


	public void setByteData(byte[] byteData) {
		this.byteData = byteData;
	}


	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}


	public void setLastUpdateDate(Date value) {
		this.lastUpdateDate = value;
	}


	public String getMediaID() {
		return mediaID;
	}


	public void setMediaID(String mediaID) {
		this.mediaID = mediaID;
	}


	public void setNumId(long numId) {
		this.numId = numId;
	}


	public void setMediaTitles(Hashtable<String, String> mediaTitles) {
		this.mediaTitles = mediaTitles;
	}


	public void setDescriptions(Hashtable<String, String> itemDescriptions) {
		this.mediaDescriptions = itemDescriptions;
	}


	public void namefieldSet(boolean b) {
		this.hasMediaTitleSetByField  = b;

	}

	public boolean namefieldSet() {
		return this.hasMediaTitleSetByField;

	}

	/**
	 * This will retrieve all metadata from MP regarding this asset.
	 * It will either load the binary data into the mem (bad idea!) or just opens
	 * an input stream on the data
	 * @param event
	 *
	 * @throws Exception
	 */
	public boolean loadAssetData() throws Exception {

		/*
		 * load all necessary meta data from the MP REST API and
		 * map them to object properties
		 */
		boolean rc = loadAssetMetaData();

		if ( rc == false ) {
			setState(states.fault);
			return rc;
		}

		/*
		 * get the according channels and rendering schemes, as they are currently not populated to the SYNCHRONIZE 
		 * en not to the MATADATA_CHANGED event and put them into the artifical event here
		 */
		if ( mediaPoolEvent.getEvent() == MediaPoolWebHookEvents.Event.SYNCHRONIZE ||
				mediaPoolEvent.getEvent() == MediaPoolWebHookEvents.Event.METADATA_CHANGED )
			mapPublishingData();
		
		/*
		 * start the download task if necessary
		 */
		rc = loadAdditionalAssetInformation();

		if ( rc == false ) {
			setState(states.fault);
			return rc;
		}
		
		LOGGER.info("Channel Publication Info: ");
		LOGGER.info("Media ID " + this.getMediaID() );
		LOGGER.info("Title " + this.getMediaTitle() );
		LOGGER.info("Filename " + (this.getFilename() != null ? this.getFilename() : this.getGenerated_filename()) );
		LOGGER.info("Suffix " + this.getSuffix() );
		LOGGER.info("DL Url " + this.getDownloadUrl() );
		LOGGER.info("Version # " + this.getVersion() );
		LOGGER.info("--------------------------");
		return rc;
	}

	public synchronized final long getFileSizeKiloBytes() {
		return fileSizeKiloBytes;
	}

	public synchronized final void setFileSizeKiloBytes(long fileSizeKiloBytes) {
		this.fileSizeKiloBytes = fileSizeKiloBytes;
	}

	/**
	 * @return the lastUploadDate
	 */
	public synchronized final Date getLastUploadDate() {
		return lastUploadDate;
	}

	/**
	 * @param lastUploadDate the lastUploadDate to set
	 */

	public synchronized final void setLastUploadDate(Date lastUploadDate) {
		this.lastUploadDate = lastUploadDate;
	}



	/**
	 * @return the channels
	 */
	public Hashtable<String, Long> getChannels() {
		return channels;
	}



	/**
	 * @param channels the channels to set
	 */
	public void setChannels(Hashtable<String, Long> channels) {
		this.channels = channels;
	}

	public long getVdbId() {
		return vdbId;
	}

	public void setVdbId(long vdbId) {
		this.vdbId = vdbId;
	}

	public Hashtable<String, String> getvdbNames() {
		return vdbNames;
	}

	public void setvdbNames(Hashtable<String, String> vdbNames) {
		this.vdbNames = vdbNames;
	}
}
