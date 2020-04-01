package com.brandmaker.mediapool.rest;

import com.brandmaker.mediapool.MediaPoolAsset;
import com.brandmaker.mediapool.webhook.MediaPoolEvent;




/**
 * Manages the local file copy of an asset
 * 
 * <p>The files are stored in the path given by the application.yaml. here is a directory per asset ID and underneath a binary file and a metadata JSON:
 * 
 * <pre>
 * 
 *    &lt;basepath>/
 *    		&lt;asstid>/
 *    			&lt;originalfilename>.&lt;suffix>
 *    			metadata.json
 *    
 * </pre>
 * @author axel.amthor
 *
 */
public interface FileManagerService {

	void storeMetadata(MediaPoolAsset mpAsset);

	void storeBinarydata(MediaPoolAsset mpAsset);

	void deleteFiles(MediaPoolEvent event);

}
