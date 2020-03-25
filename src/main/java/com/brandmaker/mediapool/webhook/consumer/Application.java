package com.brandmaker.mediapool.webhook.consumer;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import com.brandmaker.mediapool.webhook.rest.controller.HookController;




/**
 * <p> Spring Boot Application starter
 * <p>Security auto config is <b>not loaded</b here
 * @see com.brandmaker.mediapool.webhook.consumer.SecurityConfiguration
 * 
 * @author axel.amthor
 *
 */
@SpringBootApplication(exclude={SecurityAutoConfiguration.class})

// our controller is in a sibling package, give Spring some hints where to find it
@ComponentScan(basePackageClasses = HookController.class,basePackages={"com.brandmaker.mediapool.queue"})

public class Application {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HookController.class);
	
	public static void main(String[] args) {
		
		LOGGER.info("Here we go");
		
		SpringApplication.run(Application.class, args);
	}

}
