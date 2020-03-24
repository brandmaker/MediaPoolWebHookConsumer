package com.brandmaker.mediapool.webhook;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * <p>As the MediaPoolEvent Object gets added to the Event Job Queue, this Object msut be entirely serializable!
 * 
 * <p>We cannot use jackson over here, as the data structure of each event type varies. We need to explicitly parse the submitted data and create an object
 *
 * @author axel.amthor
 *
 */
public class MediaPoolEvent
{

	public static final String PROP_RENDERINGSCHEME = "renderingScheme";
	public static final String PROP_CHANNELID = "channelId";
	public static final String CHANNEL_SHARE = "SHARE";
	public static final String CHANNEL_PUBLIC_LINKS = "PUBLIC_LINKS";
	public static final String PROP_SIGNATURE = "signature";
	public static final String PROP_EVENTTIME = "eventTime";
	public static final String PROP_EVENT = "eventType";
	public static final String PROP_EVENTDATA = "eventData";
	public static final String PROP_ASSETID = "assetId";
	public static final String PROP_ASSETIDS = "assetIds";
	public static final String PROP_BASEURL = "baseUrl";
	public static final String PROP_SYSTEMID = "systemId";
	public static final String PROP_CUSTOMERID = "customerId";
	public static final String PROP_TENANTID = "tenantId";
	public static final String PROP_DOWNNLOADSCHEME = "downloadScheme";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MediaPoolEvent.class);

	@Value("#{'${spring.application.system.channels}'.split(',')}")
	private static final ArrayList<String> mySyncChannels = null;
	
	/**
	 * Inner class which maps property values to class variables
	 * @author axel.amthor
	 *
	 * @param <IN> Type of setter value
	 * @param <OUT> Type of getter return value
	 */
	private class AttributeActor<IN, OUT> {

		public String attributeName;
		private boolean required = true;

		AttributeActor(String name)
		{
//			LOGGER.info("Mapping " + name);
			this.attributeName = name;
		}

		AttributeActor(String name, boolean isRequired)
		{
//			LOGGER.info("Mapping " + name);
			this.attributeName = name;
			this.required = isRequired;
		}

		public void set (IN t) throws Exception {};

		public OUT get() { return null; };

	}

	/**
	 * Mapping structure to call getters and setters based on property names
	 */
	private final AttributeActor<?, ?>[] REQUEST_ATTRIBUTES = {
			new AttributeActor<Long, Long>(PROP_TENANTID, false) {

				@Override
				public void set(Long val)
				{
					if ( val != null )
						setTenantId(val);
				}

				@Override
				public Long get()
				{
					return getTenantId();
				}

			},
			new AttributeActor<String, String>(PROP_CUSTOMERID) {

				@Override
				public void set(String val)
				{
					setCustomerId(val);
				}

				@Override
				public String get()
				{
					return getCustomerId();
				}

			},
			new AttributeActor<String, String>(PROP_SYSTEMID) {

				@Override
				public void set(String val)
				{
					setSystemId(val);
				}

				@Override
				public String get()
				{
					return getSystemId();
				}

			},
			new AttributeActor<String, String>(PROP_BASEURL) {

				@Override
				public void set(String val)
				{
					setBaseUrl(val);
				}

				@Override
				public String get()
				{
					try {
						return getBaseUrl();
					} catch (MalformedURLException e) {
						LOGGER.error("Exception",e);
						return null;
					}
				}

			},
			new AttributeActor<String, String>(PROP_ASSETID, false) {

				@Override
				public void set(String val)
				{
					setMediaPoolAssetId(val);
				}

				@Override
				public String get()
				{
					return getMediaPoolAssetId();
				}

			},
			
			new AttributeActor<String, JSONArray>(PROP_EVENTDATA, false) {

				@Override
				public void set(String val) throws JSONException
				{
					if ( val != null ) {
						val = val.trim();
						if ( val.startsWith("{") ) 
							val = "[" + val + "]";
						
						JSONArray pl = new JSONArray(val);
						setPayloadArray(pl);
					}
				}

				@Override
				public JSONArray get()
				{
					return getPayloadArray();
				}

			},
			new AttributeActor<String, MediaPoolWebHookEvents.Event>(PROP_EVENT) {

				@Override
				public void set(String val) throws WebhookException
				{
					MediaPoolWebHookEvents.Event evt = MediaPoolWebHookEvents.theEvent(val);
					setEvent(evt);
				}

				@Override
				public MediaPoolWebHookEvents.Event get()
				{
					return getEvent();
				}

			},
			new AttributeActor<Long, GregorianCalendar>(PROP_EVENTTIME) {

				@Override
				public void set(Long val)
				{
					GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
					cal.setTime(new Date(new Long(val)));

					setEventTime(cal);
				}

				@Override
				public GregorianCalendar get()
				{
					return getEventTime();
				}
			},
			new AttributeActor<String, String>(PROP_SIGNATURE, false) {

				@Override
				public void set(String val) throws JSONException
				{
					setSignature(val);
				}

				@Override
				public String get()
				{
					return getSignature();
				}

			}
	};

	private String user; // logged in user requesting the operation

	private String customerId;
	private String systemId;
	private String baseUrl;
	private ArrayList<Integer>mediaPollAssetIds;
	private String mediaPoolAssetId;
	private JSONArray payloadArray;
	private MediaPoolWebHookEvents.Event event;
	private GregorianCalendar eventTime;
	private String signature;

	private String webCacheAssetId;

	private long tenantId;

	/**
	 * Validates the request data and returns an event object if valid, otherwise null.
	 * Error messages are put back into the request object as "error": "message..."
	 *
	 * @param requestObject
	 * @return MediaPoolEvent
	 * @throws JSONException
	 *
	 */
	public static MediaPoolEvent factory(JSONObject requestObject) throws JSONException, WebhookException, Exception
	{
		MediaPoolEvent event = null;
		try
		{
			event = new MediaPoolEvent(requestObject);
		}
		catch (Exception e)
		{
			LOGGER.error(requestObject.toString(4) );
			LOGGER.error(e.getMessage(), e);
			requestObject.put("error", e.getMessage());
			throw e;
		}

		return event;
	}
	
	/**
	 * Default empty Constructor
	 */
	public MediaPoolEvent() {
	}

	/**
	 * Create an Event Object from a Map
	 * @param props
	 * @throws Exception If JSON is invalid or if attribute is missing
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MediaPoolEvent(Map<String, Object> props) throws Exception {
		
		for ( AttributeActor attr : REQUEST_ATTRIBUTES ) {

			if ( attr.required && !props.containsKey(attr.attributeName) )
				throw new Exception("missing parameter: " + attr.attributeName);

			attr.set( props.get(attr.attributeName) );
		}

	}

	/**
	 * Create an Event Object from a JSON structure
	 * @param request the JSON Object
	 * @throws Exception If JSON is invalid or if attribute is missing
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MediaPoolEvent(JSONObject request) throws WebhookException, Exception {
		
		for ( AttributeActor attr : REQUEST_ATTRIBUTES ) {

			// attribute is required but missing
			if ( attr.required && !request.has(attr.attributeName) )
				throw new WebhookException("missing parameter: " + attr.attributeName);

			Object val = null;
			if ( request.has(attr.attributeName) ) {
				switch (attr.attributeName) {
					case PROP_EVENTDATA:
						val = request.get(attr.attributeName).toString();
						break;

					case PROP_EVENTTIME:
						val = request.getLong(attr.attributeName);
						break;

					case PROP_EVENT:
						val = request.getString(attr.attributeName);
						break;
						
					case PROP_ASSETID:
						val = "" + request.getLong(attr.attributeName);
						break;

					default:
						val = request.getString(attr.attributeName);
						break;
				}
			}
			attr.set(val);
		}
	}

	/**
	 * Create a map of values of this event object
	 *
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> toMap() {

		HashMap<String, Object> eventMap = new HashMap<String, Object>();

		for ( AttributeActor<?, ?> attr : REQUEST_ATTRIBUTES ) {

			Object val = attr.get();
			if ( val != null ) {
				switch (attr.attributeName) {
					case PROP_EVENTDATA:
					case PROP_EVENT:
					case PROP_ASSETIDS:
						val = val.toString();
						break;

					case PROP_EVENTTIME:
						val = ((GregorianCalendar) val).getTimeInMillis();
						break;

					default:
						break;
				}
			}
			else {
				LOGGER.info("No value for " + attr.attributeName);
				continue;
			}

			eventMap.put(attr.attributeName, val );

		}

		return eventMap;
	}

	public JSONObject toJson() throws JSONException {

		JSONObject eventMap = new JSONObject();

		for ( AttributeActor<?, ?> attr : REQUEST_ATTRIBUTES ) {

			Object val;
			switch (attr.attributeName) {
				case PROP_EVENT:
					val = attr.get().toString();
					break;

				case PROP_EVENTTIME:
					val = ((GregorianCalendar) attr.get()).getTimeInMillis();
					break;

				default:
					val = attr.get();
					break;
			}
			if ( val == null ) {
				LOGGER.info("No value for " + attr.attributeName);
				continue;
			}

			eventMap.put(attr.attributeName, val );

		}

		return eventMap;
	}

	/**
	 * @throws JSONException
	 */
	private static JSONObject createPayload(String[] attrs) throws JSONException
	{
		JSONObject payload = new JSONObject();
		for ( String pattr : attrs )
			payload.put(pattr, "value");

		return payload;
	}

	/**
	 * @return the customerId
	 */
	public String getCustomerId()
	{
		return customerId;
	}

	/**
	 * @param customerId the customerId to set
	 */
	public void setCustomerId(String customerId)
	{
		this.customerId = customerId;
	}

	/**
	 * @return the systemId
	 */
	public String getSystemId()
	{
		return systemId;
	}

	/**
	 * @param systemId the systemId to set
	 */
	public void setSystemId(String systemId)
	{
		this.systemId = systemId;
	}

	/**
	 * @param event
	 * @return
	 * @throws MalformedURLException
	 */
	public String getBaseUrl() throws MalformedURLException
	{
		URL su = new URL(this.baseUrl);
		String baseUrl = su.getProtocol() + "://" + su.getHost() + ( su.getPort() > 0 ? (":" +  su.getPort()) : "");
		return baseUrl;
	}

	/**
	 * @param searchUrl the searchUrl to set
	 */
	public void setBaseUrl(String baseUrl)
	{
		this.baseUrl = baseUrl;
	}

	/**
	 * @return the payload
	 */
	public JSONArray getPayloadArray()
	{
		return payloadArray;
	}
	
	public boolean mustHaveChannel() {
		
		// all these events must have a channel specified in the payload.
		// currently only 4, see https://jira6.brandmaker.com/browse/MPO-3306
		switch ( this.event ) {
			case DEPUBLISHED:
			case PUBLISHED:
			case PUBLISHING_END:
			case PUBLISHING_START:
				return true;
			
			default:
				return false;
		}
	}
	
public boolean isPublishingEvent() {
		
		// all these events must have a channel specified in the payload.
		// currently only 4, see https://jira6.brandmaker.com/browse/MPO-3306
		switch ( this.event ) {
			case PUBLISHED:
			case PUBLISHING_START:
			case SYNCHRONIZE:
				return true;
			
			default:
				return false;
		}
	}
	
	/**
	 * do we need binary data to change content actually?
	 * 
	 * @return
	 */
	public boolean needsBinary() {
		switch ( this.event ) {
			case PUBLISHED:
			case PUBLISHING_START:
			case VERSION_ADDED:
			case VERSION_OFFICIAL:
			case ASSET_REACTIVATED:
			case SYNCHRONIZE:
				return true;
			
			default:
				return false;
		}
	}
	
	/**
	 * Is this a WebCache Channel Event?
	 * 
	 * @return
	 */
	public boolean isMyChannel() {
		
		ArrayList<String> channels = this.getChannelsFromPayload();
		
		for ( String channel : channels )
			if ( mySyncChannels != null && mySyncChannels.contains(channel) ) 
				return true;
		
		return false;
	}
	
	public String getRenderingScheme() {
		
		try {
			for ( int n = 0; payloadArray != null && n < payloadArray.length(); n++ ) {
				JSONObject channelData = payloadArray.getJSONObject(n);
				if ( channelData.has(PROP_CHANNELID) && channelData.has(PROP_RENDERINGSCHEME) ) {
					if ( channelData.getString(PROP_CHANNELID).equals(CHANNEL_PUBLIC_LINKS))
						return channelData.getString(PROP_RENDERINGSCHEME);
					
					if ( channelData.getString(PROP_CHANNELID).equals(CHANNEL_SHARE))
						return channelData.getString(PROP_RENDERINGSCHEME);
								
				}
			}
		}
		catch ( JSONException e )
		{
			LOGGER.error("JSON error", e);
		}
		return null;
	}
	
	/**
	 * @return true, if the event is for the "public Link" channel
	 */
	public boolean isPublicLinkChannel() {
		ArrayList<String> channels = this.getChannelsFromPayload();
		
		if ( isMyChannel() && channels.contains(CHANNEL_PUBLIC_LINKS) )
			return true;
		
		return false;
	}
	
	/**
	 * @return true, if the event is for the "sharing Link" channel
	 */
	public boolean isSharingChannel() {
		ArrayList<String> channels = this.getChannelsFromPayload();
		
		if ( isMyChannel() && channels.contains(CHANNEL_SHARE) )
			return true;
		
		return false;
	}
	
	/**
	 * Get the name of the channel from the payload
	 * 
	 * @return
	 */
	public ArrayList<String> getChannelsFromPayload()
	{
		ArrayList<String> channels = new ArrayList<String>();
		
		if ( payloadArray != null ) {
			for ( int n = 0; n < payloadArray.length(); n++ ) {
				try {
					JSONObject payload = payloadArray.getJSONObject(n);
					if ( payload.has(PROP_CHANNELID) )
						channels.add(payload.getString(PROP_CHANNELID));
				} catch (JSONException e) {
					LOGGER.error("JSON Error", e);
				}
			}
		}
		return channels;
	}
	
	public ArrayList<Integer> getMediaPoolAssetIds() {
		return this.mediaPollAssetIds;
	}
	
	public void setMediaPoolAssetIds(ArrayList<Integer> aids) {
		this.mediaPollAssetIds = aids;
	}

	/**
	 * @param payload the payload to set
	 */
	public void setPayloadArray(JSONArray payload)
	{
		this.payloadArray = payload;
	}

	/**
	 * @return the event
	 */
	public MediaPoolWebHookEvents.Event getEvent()
	{
		return event;
	}

	/**
	 * @param event the event to set
	 */
	public void setEvent(MediaPoolWebHookEvents.Event event)
	{
		this.event = event;
	}

	/**
	 * @return the eventTime
	 */
	public GregorianCalendar getEventTime()
	{
		return eventTime;
	}

	/**
	 * @param eventTime the eventTime to set
	 */
	public void setEventTime(GregorianCalendar eventTime)
	{
		this.eventTime = eventTime;
	}

	/**
	 * @return the mediaPoolAssetId
	 */
	public String getMediaPoolAssetId()
	{
		return mediaPoolAssetId;
	}

	/**
	 * @param mediaPoolAssetId the mediaPoolAssetId to set
	 */
	public void setMediaPoolAssetId(String mediaPoolAssetId)
	{
		this.mediaPoolAssetId = mediaPoolAssetId;
	}

	/**
	 * @return the webCacheAssetId
	 */
	public String getWebCacheAssetId()
	{
		return webCacheAssetId;
	}

	/**
	 * @param webCacheAssetId the webCacheAssetId to set
	 */
	public void setWebCacheAssetId(String webCacheAssetId)
	{
		this.webCacheAssetId = webCacheAssetId;
	}



	/**
	 * @return the signature
	 */
	public String getSignature()
	{
		return signature;
	}

	/**
	 * @param signature the signature to set
	 */
	public void setSignature(String signature)
	{
		this.signature = signature;
	}

	/**
	 * @return the user
	 */
	public String getUser()
	{
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user)
	{
		this.user = user;
	}

	/**
	 * @return the tenantId
	 */
	public long getTenantId()
	{
		return tenantId;
	}

	/**
	 * @param tenantId the tenantId to set
	 */
	public void setTenantId(long tenantId)
	{
		this.tenantId = tenantId;
	}


}