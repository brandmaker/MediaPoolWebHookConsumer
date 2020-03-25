package com.brandmaker.mediapool.queue;

import java.io.IOException;

import javax.jms.JMSException;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.glassfish.jersey.internal.inject.ParamConverters.TypeValueOf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;

import com.brandmaker.mediapool.rest.MediaPoolApiWrapper;
import com.brandmaker.mediapool.webhook.MediaPoolEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>The consumer of the internal event queue.
 * <p>This consumer is doing the real work with Media Pool. It implements a JMS and ActiveMQ Listener, which will
 * <ul>
 * 		<li>Receive an event from the queue
 * 		<li>Analyze the event type, and if it is "PUBLISHED" then
 * 		<li>Connect to Media Pool via REST API
 * 		<li>Retrieve meta data and store to JSON file
 * 		<li>Retrieve the binary in requested rendition and version and store to local file system
 * </ul>
 * <p>This is just an example on how to use the REST API of Media Pool to get access to any data stored there.
 * <p><b>Hint:</b> Do not create worker threads here, leave the configuration of any parallelism up to the queue itself as this will give more control and even flexibility!
 * 
 * @author axel.amthor
 *
 */
public class QueueConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueueConsumer.class);


	/**
	 * <p>This method will be called as soon as something is enqueued and avaliable for the consumer(s)
	 * 
	 * @param message
	 */
	@JmsListener(destination = "${spring.active-mq.queue-name}")
	public void onMessage(Object message) {
		
		LOGGER.debug("received a message = '{}'", message.toString());
		try {
			
			if ( message instanceof ActiveMQTextMessage ) {
				
				// ActiveMQTextMessage has a lot of methods to manage dequeued messages, we are not making use of them in this example
				ActiveMQTextMessage tMessage = (ActiveMQTextMessage)message;
				
				// deserialize the event from the message
				MediaPoolEvent event = new ObjectMapper().readValue(tMessage.getText(), MediaPoolEvent.class);
				
				LOGGER.info("dequeued event " + event.getEvent().toString() + " for asset " + event.getAssetId() );
				
				// process the event now. We have a "MediaPoolApiWrapper" class which is handling all Media Pool API stuff
				MediaPoolApiWrapper mpapiwrapper = new MediaPoolApiWrapper(event);
				
				mpapiwrapper.synchronize();
				
			}
			else
				LOGGER.error("Oops, unknow message type " + message.getClass().getCanonicalName() );
			
		} catch ( JMSException | IOException e) {
			
			LOGGER.error("Problems on deserialization", e);
		}

	}
}
