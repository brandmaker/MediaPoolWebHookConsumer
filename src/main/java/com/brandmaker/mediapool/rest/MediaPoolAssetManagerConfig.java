package com.brandmaker.mediapool.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MediaPoolAssetManagerConfig {

	@Bean
	MediaPoolAssetManager assetManager() {
		return new MediaPoolAssetManager();
	}
}
