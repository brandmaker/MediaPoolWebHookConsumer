package com.brandmaker.mediapool.webhook.rest.controller;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
	
	
	/**
	 * <p>basic request validator method
	 * <p>the rest endpoint is simply "/hook"
	 * 
	 * @param body the raw request body
	 * 
	 * @return Response object with detailed status and error code
	 */
	@PostMapping("/hook")
	public Response validator(@RequestBody String body) {
		
		LOGGER.info(body);
		
		Response response = new Response(body, 0);
		
		return response;
	}
	
}
