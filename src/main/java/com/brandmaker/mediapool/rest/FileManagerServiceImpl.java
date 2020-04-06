/**
 * 
 */
package com.brandmaker.mediapool.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.brandmaker.mediapool.MediaPoolAsset;
import com.brandmaker.mediapool.webhook.MediaPoolEvent;

/**
 * @see FileManagerService
 * 
 * @author axel.amthor
 *
 */
public class FileManagerServiceImpl implements FileManagerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileManagerService.class);
	
	/** directory where to store the local copies of the assets */
	@Value("${spring.application.system.basepath}")
	private String basepath;
	
	/* (non-Javadoc)
	 * @see com.brandmaker.mediapool.rest.FileManagerService#storeMetadata(com.brandmaker.mediapool.MediaPoolAsset)
	 */
	@Override
	public void storeMetadata(MediaPoolAsset mpAsset) {
		File path = getOrCreateTargetFolder(mpAsset.getMediaPoolEvent());
		FileOutputStream outputStream =  null;
		
		File metadata = new File(path, "metadata.json");
		try {
			outputStream = new FileOutputStream(metadata);
		    byte[] bytes = mpAsset.toJson().toString(4).getBytes();
		    outputStream.write(bytes);
		}
		catch ( Exception e ) {
			LOGGER.error("Error on writing meta data", e);
		}
		finally {
			
			try {
				if ( outputStream != null )
					outputStream.close();
			}
			catch ( Exception e ) {
				LOGGER.error("Error on closing streams", e);
			}
			
		}
	}

	/* (non-Javadoc)
	 * @see com.brandmaker.mediapool.rest.FileManagerService#storeBinarydata(com.brandmaker.mediapool.MediaPoolAsset)
	 */
	@Override
	public void storeBinarydata(MediaPoolAsset mpAsset) {
		File path = getOrCreateTargetFolder(mpAsset.getMediaPoolEvent());
		FileOutputStream outputStream = null;
		InputStream inputStream = null;
		
		try {
			
			// lets get an inputsream. Based on the rendering scheme, the filename and suffix may alter!
			inputStream = mpAsset.getDataInputStream();
			
			// use the actual name and suffix and create the output file
			File binary = new File(path, mpAsset.getFilename() + "." + mpAsset.getSuffix());
			
			// open out stream
			outputStream = new FileOutputStream(binary);
			
			// copy streams
		    long n = IOUtils.copy(inputStream, outputStream);
		    
		    LOGGER.info("Copied {} bytes to file " + binary.getAbsolutePath(), n);
		    
		}
		catch ( Exception e ) {
			LOGGER.error("Error on writing meta data", e);
		}
		finally {
			
			try {
				if ( outputStream != null )
					outputStream.close();
				if ( inputStream != null )
					inputStream.close();
			}
			catch ( Exception e ) {
				LOGGER.error("Error on closing streams", e);
			}
			
		}
		
	}

	/* (non-Javadoc)
	 * @see com.brandmaker.mediapool.rest.FileManagerService#deleteFiles(com.brandmaker.mediapool.webhook.MediaPoolEvent)
	 */
	@Override
	public void deleteFiles(MediaPoolEvent event) {
		File path = getOrCreateTargetFolder(event);

		try {
			
			FileUtils.deleteDirectory(path);
		} 
		catch (IOException e) {
			LOGGER.error("Error on removing files", e);
		}
		
	}
	
	
	private File getOrCreateTargetFolder(MediaPoolEvent event) {
		
		String path = basepath 
				+ event.getCustomerId() + "/" 
				+ event.getSystemId() + "/"				
				+ event.getAssetId();
		
		File dir = new File(path);
		
		dir.mkdirs();
		
		return dir;
	}

}
