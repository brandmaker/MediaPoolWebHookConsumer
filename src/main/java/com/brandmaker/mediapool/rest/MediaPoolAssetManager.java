package com.brandmaker.mediapool.rest;

import com.brandmaker.mediapool.MediaPoolAsset;
import com.brandmaker.mediapool.MediaPoolWebHookEvents.Event;
import com.brandmaker.mediapool.webhook.MediaPoolEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>The AssetManager is actually processing the dequeued event and executing all necessary tasks on the REST API of Media Pool
 * in order to maintain the local file copy of an asset.
 * 
 * <p>It is using the REST API calls encapsulated in the RestServicesWrapper class
 * 
 * @see RestServicesWrapper
 * 
 * @author axel.amthor
 *
 */
public class MediaPoolAssetManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(MediaPoolAssetManager.class);
	
	@Autowired
	RestServicesWrapper restService;
	
	@Autowired
	FileManagerService fileManagerService;
	
	/**
	 * Synchronize the local file copy of the asset
	 * @param event
	 */
	public void synchronize(MediaPoolEvent event) {

		LOGGER.info("start sync now");
		
		try {
			
			MediaPoolAsset mpAsset = null;
			Event evt = event.getEvent();
			boolean dataLoaded = false;
			
			switch ( evt ) {
				
				case TEST:
					LOGGER.info("Test event reveived" );
					break;
					
				case TREE_CHANGED: // not implemented in the demo
					LOGGER.info("Full category tree sync, not implemented" );
					break;
					
				case ASSET_DELETED:
					// the asset may be gone in MP, so we do not retrieve it from there, we just drop the files
					dispatchEvent(event, null);
					break;
					
				default:
	
					// get the meta data of the Media Pool Asset
					// if necessary, this will also start the download task generation in order to retrieve the binary
					mpAsset = new MediaPoolAsset(restService, event);
	
					dataLoaded = mpAsset.loadAssetData();
					
					if ( dataLoaded ) {
						dispatchEvent(event, mpAsset);
						LOGGER.info("File updated" );
					}
					else {
						LOGGER.info("could not load asset " + event.getAssetId() );
					}
					break;
			}
			
		} 
		catch ( Exception e) {
			LOGGER.error("A general error", e);
		}
		
	}
	
	/**
	 * Check the MP event and act accordingly
	 *
	 * @param event
	 * @param mpAsset
	 */
	private void dispatchEvent(MediaPoolEvent event, MediaPoolAsset mpAsset)
	{
		Event evt = event.getEvent();

		if ( event.isMyChannel() ) {
			
			LOGGER.info("Prosess Webhook Event " + evt.name() );
			
			switch (evt)
			{
	
				/*
				 * Asset Events
				 */
				case ASSET_REACTIVATED:
				case SYNCHRONIZE:
				case PUBLISHED:
				case PUBLISHING_START:
					if ( mpAsset != null && mpAsset.isStateReady() ) {
						
						// stoe Meta Data
						fileManagerService.storeMetadata(mpAsset);
						
						// store Binary Data
						fileManagerService.storeBinarydata(mpAsset);
						
					}
					break;
	
				case METADATA_CHANGED:
					if ( event.isMyChannel() && mpAsset != null && mpAsset.isStateReady() ) {
					
						// store Meta Data
						fileManagerService.storeMetadata(mpAsset);
					}
					break;
					
					
				case PUBLISHING_END:
				case DEPUBLISHED:
					if ( event.isMyChannel() ) {
						
						// delete the file(s)
						fileManagerService.deleteFiles(event);
						
					}
					break;
	
				case VERSION_ADDED:
				case VERSION_OFFICIAL:
					if ( mpAsset != null && mpAsset.isStateReady() ) {
	
						// store Binary Data
						fileManagerService.storeBinarydata(mpAsset);
						fileManagerService.storeMetadata(mpAsset);
					}
					break;
	
				case VERSION_DELETED:
				case VERSION_UNOFFICIAL:
					if ( mpAsset != null && mpAsset.isStateReady() ) {
						
						// delete the file(s)
						fileManagerService.deleteFiles(event);
						
					}
					break;
	
				case ASSET_DELETED:
				case ASSET_REMOVED:
						
					// delete the file(s)
					fileManagerService.deleteFiles(event);
					
					break;
	
				case ASSET_ARCHIVED:
					if ( mpAsset != null && mpAsset.isStateReady() ) {
					
						// delete the file(s)
						fileManagerService.deleteFiles(event);
						
					}
					break;
	
				/*
				 * currently not covered / used by this demo
				 */
					
				case TREE_CHANGED:
				case CATEGORY_ADD:
				case CATEGORY_REMOVE:
				case CATEGORY_MOVE:
				case VARIANT_ADDED:
				case VARIANT_REMOVED:
				case RELATION_ADDED:
				case RELATION_REMOVED:
				case CREATED:
				case APPROVED:
				case REJECTED:
				default:
					LOGGER.info("event " + evt.toString() + "not implemented");
					break;
	
			}
		}
		else
			LOGGER.info("Not my business: " + event.getChannelsFromPayload().toString() );
	}
}
