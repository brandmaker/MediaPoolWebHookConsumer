package com.brandmaker.mediapool.rest;

import com.brandmaker.mediapool.MediaPoolAsset;
import com.brandmaker.mediapool.webhook.MediaPoolEvent;




/**
 * Manages the local file copy of an asset
 * 
 * <p>The files are stored in the path given by the application.yaml. There is a directory per asset ID and underneath a binary file and a metadata JSON:
 * 
 * <pre>
 * 
 *    &lt;basepath>/
 *    		&lt;customer ID>/
 *    			&lt;system id>/
 *    				&lt;asstid>/
 *    					&lt;originalfilename>.&lt;suffix>
 *    					metadata.json
 *    
 * </pre>
 * @author axel.amthor
 *
 */
public interface FileManagerService {

	/**
	 * Convert the Asset to a JSON Object and store into file
	 * 
	 * @param mpAsset
	 */
	void storeMetadata(MediaPoolAsset mpAsset);

	/**
	 * <p>Poll the download task and retrieve the binary data stream.
	 * <p>Copy this to a file.
	 * <p>There is a timeout on waiting for the download task! As Media Pool needs to generate the rquested rendition, the data might not be ready immediately,
	 * that's why we have to poll, please refer to the Media Pool REST API documentation here"
	 * 
	 * @param mpAsset
	 */
	void storeBinarydata(MediaPoolAsset mpAsset);

	/**
	 * Delete all files of this asset from the local file store
	 * 
	 * @param event
	 */
	void deleteFiles(MediaPoolEvent event);

}
