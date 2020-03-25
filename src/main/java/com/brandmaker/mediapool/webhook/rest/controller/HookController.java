package com.brandmaker.mediapool.webhook.rest.controller;

import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.brandmaker.mediapool.queue.QueueConsumer;
import com.brandmaker.mediapool.queue.Sender;
import com.brandmaker.mediapool.webhook.MediaPoolEvent;
import com.brandmaker.mediapool.webhook.WebhookException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>Hook controller
 * 
 * <p>This is supposed to pick the post message and basically validate the contents and the signature based on the given settings
 * 
 * @author axel.amthor
 *
 */
@RestController
public class HookController {

	/** our logger is log4j */
	private static final Logger LOGGER = LoggerFactory.getLogger(HookController.class);
	
	/** pick value from application.yaml */
	@Value("${spring.application.system.publickey}")
	private String pubkey;
	
	@Value("${spring.application.system.customerId}")
	private String customerId;
	
	@Value("${spring.application.system.systemId}")
	private String systemId;
	
	@Autowired
	private Sender sender;
	
	private String[] copyProps = { MediaPoolEvent.PROP_CUSTOMERID, MediaPoolEvent.PROP_SYSTEMID, MediaPoolEvent.PROP_BASEURL };
	
	/**
	 * <p>basic request validator method
	 * <p>the rest endpoint is simply "/hook"
	 * 
	 * @param body the pojo with the request body, @see {@link HookRequestBody}
	 * 
	 * @return Response object with detailed status and error code
	 */
	@PostMapping("/hook")
	public Response post(@RequestBody HookRequestBody requestBody, HttpServletResponse httpResponse) {
		
		long start = System.currentTimeMillis();
		Response response = null;
		
		try {
			JSONObject eventObject = new JSONObject();
			JSONArray responseArray = new JSONArray();
			
			LOGGER.debug(requestBody.toString(4));
			
			String eventData = requestBody.getData();
			String signature = requestBody.getSignature();
			
			// ToDo: validate the data with the signature and the configured pub key
			
			
			
			/*
			 * parse data property and parse the inner structure as JSON
			 */
			JSONObject dataObject = new JSONObject(eventData);
			
			/* this is the array of actual media pool events submitted in this request */
			JSONArray eventArray = dataObject.getJSONArray("events");
			LOGGER.debug("decoded data: " + dataObject.toString(4));
			
			/*
			 * process event array
			 */
			for ( int n = 0; n < eventArray.length(); n++ )
			{
				// pick one event
				eventObject = eventArray.getJSONObject(n);
				
				/*
				 * these props need to go into each event element, as within the subsequent queue, 
				 * there is no "batch" but single, disjoint events
				 */
				for ( String prop : copyProps ) {
					if ( dataObject.has(prop) )
						eventObject.put(prop, dataObject.getString(prop));
				}
				
				/*
				 * validate event data
				 */
				MediaPoolEvent mediapoolEvent = MediaPoolEvent.factory(eventObject);
				
				// check source system IDs of this event
				// if you want to listen for a particular instance and custoomer ID, uncomment the following and the `else` branch below
//				if ( mediapoolEvent.getCustomerId().equals(customerId) && mediapoolEvent.getSystemId().equals(systemId) ) 
				{
					/*
					 * Push event to the processing queue
					 * We will not process this event within this loop!
					 * 
					 * We are using spring JMS together with ActiveMQ as a broker. Configuration can be done via the application.yaml
					 * 
					 */
					
					// serialize the event object to a json string
					String serializedEvent = new ObjectMapper().writeValueAsString(mediapoolEvent);
					
					// send this serialized event to media pool processing queue
					sender.send(serializedEvent);
					
					LOGGER.info( (n+1) + ". Event " + mediapoolEvent.getEvent().toString() + " for Asset " + mediapoolEvent.getAssetId() + " queued." );
				}
//				else
//					LOGGER.error("Event " + mediapoolEvent.getEvent().toString() 
//							+ " ignored for customer " + mediapoolEvent.getCustomerId() + " on system " + mediapoolEvent.getSystemId() );
				
			}
			
			// now we are done here and will send back the response to the requester
			httpResponse.setStatus(HttpServletResponse.SC_ACCEPTED);
			return new Response("accepted", HttpServletResponse.SC_ACCEPTED);
			
		}
		catch (JSONException e)
		{
			LOGGER.error("A JSON error occured", e);
			LOGGER.info("(1) Invalid MP Sync Request: " + requestBody );
		}
		catch (WebhookException e)
		{
			LOGGER.error("An error occured", e);
			LOGGER.info("(2) Invalid MP Sync Request: " + requestBody );
		}
		catch (Exception e)
		{
			LOGGER.error("A general error occured", e);
			LOGGER.info("(4) Invalid MP Sync Request: " + requestBody );
		}
		finally
		{
			LOGGER.info("Finished processing webhook request  in " + (System.currentTimeMillis() - start) + " msec");
		}

		httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		return new Response("Invalid Request", HttpServletResponse.SC_BAD_REQUEST);
	}
	
}
