package com.brandmaker.mediapool.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestServicesWrapperImplConfig {

	@Bean
	RestServicesWrapper getRestService() {
		return new RestServicesWrapperImpl();
	}
}
