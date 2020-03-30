package com.brandmaker.mediapool;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brandmaker.mediapool.webhook.WebhookException;

/**
 * Wrapper class to encapsulate all possible Media Pool webhook events
 * 
 * @author axel.amthor
 * @since 03/2020
 * 
 */
public class MediaPoolWebHookEvents
{

	private static final Logger LOGGER = LoggerFactory.getLogger(MediaPoolWebHookEvents.class);

	/** A new asset has been created. This can either be by uploading a new asset through import or via API. The event fires, when the asset is created and available in import folder.*/
	private static final String EVENT_CREATED = "CREATED";
	/** An asset has been approved in the upload workflow*/
	private static final String EVENT_APPROVED = "APPROVED";
	/** An asset has been rejected in upload workflow*/
	private static final String EVENT_REJECTED = "REJECTED";
	/** An asset has been published to one of the channels. The event fires separateley for each affected channel.*/
	private static final String EVENT_PUBLISHED = "PUBLISHED";
	/** A publishing time has been reached. The event fires as soon as the asset is due to be published on a particular channel.*/
	private static final String EVENT_PUBLISHING_START = "PUBLISHING_START";
	/** A publishing end date has been reached. The event fires as soon as the asset gets depublished on a praticular channel.*/
	private static final String EVENT_PUBLISHING_END = "PUBLISHING_END";
	/** The asset is depublished. The event fires as soon as a user depublishes the asset. This is not the same as "PUBLISHING_END" as in this case the asset is still in a published state but the end of the "To" date has been reached*/
	private static final String EVENT_DEPUBLISHED = "DEPUBLISHED";
	/** Event fires if ANY metadata has been changed. This includes all free text fields. Except: Versions, Variants and related assets. This also does NOT INCLUDE changes to the theme tree associations, see below*/
	private static final String EVENT_METADATA_CHANGED = "METADATA_CHANGED";
	/** Fires if a new version is added*/
	private static final String EVENT_VERSION_ADDED = "VERSION_ADDED";
	/** Fires, if any version is deleted*/
	private static final String EVENT_VERSION_DELETED = "VERSION_DELETED";
	/** Fires, once a version is set to official*/
	private static final String EVENT_VERSION_OFFICIAL = "VERSION_OFFICIAL";
	/** Fires, once a version is set to unofficial*/
	private static final String EVENT_VERSION_UNOFFICIAL = "VERSION_UNOFFICIAL";
	/** Fires once variants are added*/
	private static final String EVENT_VARIANT_ADDED = "VARIANT_ADDED";
	/** Fires, once a variant is removed*/
	private static final String EVENT_VARIANT_REMOVED = "VARIANT_REMOVED";
	/** Fires, once a related asset is added*/
	private static final String EVENT_RELATION_ADDED = "RELATION_ADDED";
	/** Fires, once a related asset is removed*/
	private static final String EVENT_RELATION_REMOVED = "RELATION_REMOVED";
	/** The asset has been deleted. The event fires as soon as an asset is moved to the recycle bin VDB*/
	private static final String EVENT_ASSET_REMOVED = "ASSET_REMOVED";
	/** Fires as soon as an asset is moved to the archive VDB*/
	private static final String EVENT_ASSET_ARCHIVED = "ASSET_ARCHIVED";
	/** Fires as soon as an asset is moved from the archive VDB to any other VDB than recycle bin.*/
	private static final String EVENT_ASSET_REACTIVATED = "ASSET_REACTIVATED";
	/** An asset has been assigned to a theme tree topic.*/
	private static final String EVENT_CATEGORY_ADD = "CATEGORY_ADD";
	/** An asset has been removed from a theme tree topic.*/
	private static final String EVENT_CATEGORY_REMOVE = "CATEGORY_REMOVE";
	/** Attention:
	This actually is not a change in the asset, but in the theme tree: a sub topic is moved from one parent to another parent. This affects all assets underneath this subtopic!
	For sync purposes, where the client is as well synchronizing the theme tree, it's important to know about such "implicit moves" of assets*/
	private static final String EVENT_CATEGORY_MOVE = "CATEGORY_MOVE";
	/** Physically removed from recycle bin VDB*/
	private static final String EVENT_ASSET_DELETED = "ASSET_DELETED";

	/** general change of the entire category tree which need total resync */
	private static final String EVENT_TREE_CHANGED = "TREE_CHANGED";

	/** Global synchronization of all assets */
	private static final String EVENT_SYNCHRONIZE = "SYNCHRONIZE";

	/** Test event to test the endpoint */
	private static final String EVENT_TEST = "TEST";
	
	/** enum encapsulation to not to mess up */
	public static enum Event {

		/** Global synchronization of all assets */
		SYNCHRONIZE(EVENT_SYNCHRONIZE),
		
		/** Testing endpoint */
		TEST(EVENT_TEST),
		
		/** A new asset has been created. This can either be by uploading a new asset through import or via API. The event fires, when the asset is created and available in import folder.*/
		CREATED(EVENT_CREATED),
		/** An asset has been approved in the upload workflow*/
		APPROVED(EVENT_APPROVED),
		/** An asset has been rejected in upload workflow*/
		REJECTED(EVENT_REJECTED),
		/** An asset has been published to one of the channels. The event fires separateley for each affected channel.*/
		PUBLISHED(EVENT_PUBLISHED),
		/** A publishing time has been reached. The event fires as soon as the asset is due to be published on a particular channel.*/
		PUBLISHING_START(EVENT_PUBLISHING_START),
		/** A publishing end date has been reached. The event fires as soon as the asset gets depublished on a praticular channel.*/
		PUBLISHING_END(EVENT_PUBLISHING_END),
		/** The asset is depublished. The event fires as soon as a user depublishes the asset. This is not the same as ""PUBLISHING_END"" as in this case the asset is still in a published state but the end of the ""To"" date has been reached*/
		DEPUBLISHED(EVENT_DEPUBLISHED),
		/** Event fires if ANY metadata has been changed. This includes all free text fields. Except: Versions, Variants and related assets. This also does NOT INCLUDE changes to the theme tree associations, see below*/
		METADATA_CHANGED(EVENT_METADATA_CHANGED),
		/** Fires if a new version is added*/
		VERSION_ADDED(EVENT_VERSION_ADDED),
		/** Fires, if any version is deleted*/
		VERSION_DELETED(EVENT_VERSION_DELETED),
		/** Fires, once a version is set to official*/
		VERSION_OFFICIAL(EVENT_VERSION_OFFICIAL),
		/** Fires, once a version is set to unofficial*/
		VERSION_UNOFFICIAL(EVENT_VERSION_UNOFFICIAL),
		/** Fires once variants are added*/
		VARIANT_ADDED(EVENT_VARIANT_ADDED),
		/** Fires, once a variant is removed*/
		VARIANT_REMOVED(EVENT_VARIANT_REMOVED),
		/** Fires, once a related asset is added*/
		RELATION_ADDED(EVENT_RELATION_ADDED),
		/** Fires, once a related asset is removed*/
		RELATION_REMOVED(EVENT_RELATION_REMOVED),
		/** The asset has been deleted. The event fires as soon as an asset is moved to the recycle bin VDB*/
		ASSET_REMOVED(EVENT_ASSET_REMOVED),
		/** Fires as soon as an asset is moved to the archive VDB*/
		ASSET_ARCHIVED(EVENT_ASSET_ARCHIVED),
		/** Fires as soon as an asset is moved from the archive VDB to any other VDB than recycle bin.*/
		ASSET_REACTIVATED(EVENT_ASSET_REACTIVATED),
		/** An asset has been assigned to a theme tree topic.*/
		CATEGORY_ADD(EVENT_CATEGORY_ADD),
		/** An asset has been removed from a theme tree topic.*/
		CATEGORY_REMOVE(EVENT_CATEGORY_REMOVE),
		/** Attention:
		 * This actually is not a change in the asset, but in the theme tree: a sub topic is moved from one parent to another parent. This affects all assets underneath this subtopic!
		 * For sync purposes, where the client is as well synchronizing the theme tree, it's important to know about such ""implicit moves"" of assets
		 */
		CATEGORY_MOVE(EVENT_CATEGORY_MOVE),
		/** Physically removed from recycle bin VDB*/
		ASSET_DELETED(EVENT_ASSET_DELETED),

		/** general change of the entire category tree which need total resync */
		TREE_CHANGED(EVENT_TREE_CHANGED)

		;


		private final String text;

		Event(final String text) {
	        this.text = text;
	    }

	    /* (non-Javadoc)
	     * @see java.lang.Enum#toString()
	     */
	    @Override
	    public String toString() {
	        return text;
	    }

	}

	public static Event theEvent(String val) throws WebhookException {
	   try
	   {
		   return Event.valueOf(val);
	   }
	   catch ( Exception e)
	   {
		   LOGGER.info("Unknown Event Type: " + val);
		   throw new WebhookException("Unknown Event Type: " + val);
	   }
   }

}
