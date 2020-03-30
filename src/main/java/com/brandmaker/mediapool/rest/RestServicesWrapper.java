package com.brandmaker.mediapool.rest;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import org.json.JSONObject;
import org.springframework.context.annotation.Bean;

import com.brandmaker.mediapool.MediaPoolAsset;
import com.brandmaker.mediapool.webhook.MediaPoolEvent;
import com.brandmaker.mediapool.webhook.WebhookException;

/**
 *
 * Wraps all REST API calls against any MediaPool Instance
 *
 * @author axel.amthor
 *
 */
/**
 * @author axel.amthor
 *
 */
public interface RestServicesWrapper
{

	/**
	 * Retrieve asset data from MP REST  API
	 *
	 * sample URL: /rest/mp/assets/68614/versions/official?expand=vdb,license,language,variants,countries,customProperties,persons,downloadApproval,themes,watchers,publishedChannels,relations,freeFields,structuredKeywords,uploadApproval,division
	 *
	 * @see <a href="https://nexus.dev.brandmaker.com/repository/documentation/com.brandmaker.mms/mediapool-rest-api/53.0.0-6.3-SNAPSHOT/rest-api/resource_AssetRestService.html#resource_AssetRestService_findByIdOfficialVersion_GET">Nexus REST Doku</a>
	 *
	 * <p>Result:
	 * <pre>
	 * {
		    "items": [{
		        "@type": "object",
		        "fields": {
		            "themes": {
		                "@type": "object_set",
		                "items": [
		                    {
		                    "@type": "object",
		                    "fields": {
		                        "id": {
		                            "@type": "long",
		                            "value": 65
		                            },
		                        "text": {
		                            "@type": "text",
		                            "value": "/Test/BMa"
		                            },
		                        "text_multi": {
		                            "@type": "multilang",
		                            "value": {"EN": "/Test/BMa"}
		                            }
		                        }
		                    },
		                    {
		                    "@type": "object",
		                    "fields": {
		                        "id": {
		                            "@type": "long",
		                            "value": 551
		                            },
		                        "text": {
		                            "@type": "text",
		                            "value": "/New Theme"
		                            },
		                        "text_multi": {
		                            "@type": "multilang",
		                            "value": {"EN": "/New Theme"}
		                            }
		                        }
		                    },
		                    {
		                    "@type": "object",
		                    "fields": {
		                        "id": {
		                            "@type": "long",
		                            "value": 90
		                            },
		                        "text": {
		                            "@type": "text",
		                            "value": "/Peter's Theme"
		                            },
		                        "text_multi": {
		                            "@type": "multilang",
		                            "value": {"EN": "/Peter's Theme"}
		                            }
		                        }
		                    },
		                    {
		                    "@type": "object",
		                    "fields": {
		                        "id": {
		                            "@type": "long",
		                            "value": 77
		                            },
		                        "text": {
		                            "@type": "text",
		                            "value": "/new value"
		                            },
		                        "text_multi": {
		                            "@type": "multilang",
		                            "value": {"EN": "/new value"}
		                            }
		                        }
		                    }
		                ]
		                },
		            "uploadDate": {
		                "@type": "date",
		                "value": "2019-03-12T09:52:57Z"
		                },
		            "keywords_multi": {
		                "@type": "multilang_list",
		                "value": {"EN": [
		                    "Shop",
		                    "BrandMaker",
		                    "MarketingShop"
		                ]}
		                },
		            "vdb": {
		                "@type": "object",
		                "fields": {
		                    "name_multi": {
		                        "@type": "multilang",
		                        "value": {"EN": "test data (playground)"}
		                        },
		                    "id": {
		                        "@type": "long",
		                        "value": 1001
		                        }
		                    }
		                },
		            "versions": {
		                "@type": "object_set",
		                "items": [{
		                    "@type": "object",
		                    "fields": {
		                        "assetId": {
		                            "@type": "long",
		                            "value": 27518
		                            },
		                        "fileResource": {
		                            "@type": "object",
		                            "fields": {
		                                "fileName": {
		                                    "@type": "text",
		                                    "value": "100500000001_Marketing-Shop_Administration-Manual_6-1_EN_20180420"
		                                    },
		                                "extension": {
		                                    "@type": "text",
		                                    "value": "pdf"
		                                    },
		                                "fileResourceTypeName_multi": {
		                                    "@type": "multilang",
		                                    "value": {"EN": "Image"}
		                                    },
		                                "fileResourceType": {
		                                    "@type": "long",
		                                    "value": 2
		                                    },
		                                "audioFrameSizeBits": {
		                                    "@type": "long",
		                                    "value": 0
		                                    },
		                                "md5hash": {
		                                    "@type": "text",
		                                    "value": "JOZv1+VzQrYcj+skulxULA=="
		                                    },
		                                "mimeType": {
		                                    "@type": "text",
		                                    "value": "application/pdf"
		                                    },
		                                "title": {
		                                    "@type": "text",
		                                    "value": ""
		                                    },
		                                "resolution": {
		                                    "@type": "long",
		                                    "value": 72
		                                    },
		                                "generatedName": {
		                                    "@type": "text",
		                                    "value": "100500000001_Marketing-Shop_Administration-Manual_6-1_EN_20180420_27518_0"
		                                    },
		                                "duration": {
		                                    "@type": "long",
		                                    "value": 0
		                                    },
		                                "colorSpace": {
		                                    "@type": "text",
		                                    "value": "GRAY"
		                                    },
		                                "audioEncoding": {
		                                    "@type": "text",
		                                    "value": ""
		                                    },
		                                "id": {
		                                    "@type": "long",
		                                    "value": 27981
		                                    },
		                                "height": {
		                                    "@type": "long",
		                                    "value": 297
		                                    },
		                                "pageCount": {
		                                    "@type": "long",
		                                    "value": 152
		                                    },
		                                "unitName": {
		                                    "@type": "text",
		                                    "value": "mm"
		                                    },
		                                "fileResourceTypeName": {
		                                    "@type": "text",
		                                    "value": "Image"
		                                    },
		                                "actors": {
		                                    "@type": "text",
		                                    "value": ""
		                                    },
		                                "unit": {
		                                    "@type": "long",
		                                    "value": 3
		                                    },
		                                "audioChannels": {
		                                    "@type": "long",
		                                    "value": 0
		                                    },
		                                "fileSize": {
		                                    "@type": "long",
		                                    "value": 1653
		                                    },
		                                "passwordProtected": {
		                                    "@type": "bool",
		                                    "value": false
		                                    },
		                                "width": {
		                                    "@type": "long",
		                                    "value": 210
		                                    },
		                                "colorDepth": {
		                                    "@type": "long",
		                                    "value": 16
		                                    },
		                                "compression": {
		                                    "@type": "text",
		                                    "value": "Undefined"
		                                    }
		                                }
		                            },
		                        "versionNumber": {
		                            "@type": "long",
		                            "value": 0
		                            }
		                        }
		                    }]
		                },
		            "title_multi": {
		                "@type": "multilang",
		                "value": {"EN": "100500000001_Marketing-Shop_Administration-Manual_6-1_EN_20180420"}
		                },
		            "description_multi": {
		                "@type": "multilang",
		                "value": {"EN": ""}
		                },
		            "lastUpdatedTime": {
		                "@type": "date",
		                "value": "2019-03-28T15:03:08Z"
		                },
		            "id": {
		                "@type": "long",
		                "value": 27518
		                },
		            "actualVersionNumber": {
		                "@type": "long",
		                "value": 0
		                },
		            "uploadApprovalData": {
		                "@type": "object",
		                "fields": {
		                    "approveStateType": {
		                        "@type": "text",
		                        "value": "APPROVED"
		                        },
		                    "approveState_multi": {
		                        "@type": "multilang",
		                        "value": {"EN": "Approved"}
		                        },
		                    "approveState": {
		                        "@type": "text",
		                        "value": "Approved"
		                        }
		                    }
		                },
		            "hideIfNotValid": {
		                "@type": "bool",
		                "value": false
		                }
		            }
		        }],
		    "paging": {
		        "@type": "offset",
		        "offset": 0,
		        "limit": 1
		        },
		    "totalHits": 1
		    }
	 * </pre>
	 *
	 * @param tenant
	 * @param event
	 * @return
	 */
	
	JSONObject getAssetData(MediaPoolEvent event);

	/**
	 * Get an inputsream from the download URL in order to stream data to somewhere els directly
	 *
	 * @param downloadUrl
	 * @param lastUploadDate
	 * @return
	 */
	
	InputStream getDataInputStream(String downloadUrl);

	/**
	 * Get info about the current last/official version
	 *
	 * @param tenant
	 * @param mediaPoolEvent
	 * @return
	 * @throws Exception
	 */
	
	JSONObject getVersionInfo(MediaPoolEvent mediaPoolEvent) throws Exception;

	/**
	 * Create a download task for this particular asset in this particular rendering scheme.
	 * We need to wait until the rendering engine on Media Pool has finished!
	 * 
	 * @param downloadUrl
	 * @param taskRequest
	 * @return task ID
	 */
	
	String createDownloadTask(String downloadUrl, JSONObject taskRequest) throws WebhookException;

	/**
	 * Poll until the rendering has finished, max 30 min
	 * @see #createDownloadTask(String, JSONObject)
	 * 
	 * @param downloadUrl
	 * @param tenant
	 * @return http connection to download the data
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	
	HttpURLConnection pollDownloadTask(String downloadUrl) throws MalformedURLException, IOException, InterruptedException;

	/**
	 * get the input stream from an HTTP conection
	 * @param conn
	 * @return
	 */
	
	InputStream getDataInputStream(HttpURLConnection conn);

	
	/**
	 * Retrieve the effective publishing data for a particular version of the asset
	 * 
	 * 
	 * @param mediaPoolEvent
	 * @param mpAsset
	 * @return JSON Object with the publishing data
	 * @throws Exception
	 */
	
	JSONObject loadPublishingData(MediaPoolEvent mediaPoolEvent, MediaPoolAsset mpAsset) throws Exception ;

}
