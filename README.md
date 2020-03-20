<img align="right" src="https://raw.githubusercontent.com/brandmaker/MediaPoolWebHookConsumer/master/BrandMaker_Logo_on_light_bg.png" alt="BrandMaker" width="30%" height="30%">

# BrandMaker MadiaPool WebHook API Example

In order to synchronize contents and assets, which are stored in BrandMakers DAM Solution "Media Pool", a powefull WebHook integration is offered.

To register and retrieve events from this integration, integrators need to implement a REST endpoint to capture the POST data and pull 
the according information from the MediaPool API.

This repository contains a blue print for a webhook, which is capable of the following:

* register for events like "PUBLISHED" or "DEPUBLISHED"
* retrieve the POST message for the events
* validate POST data
* pull asset metadata from Media Pool via REST API
* pull binary of requested version and rendition from REST API
* store binary to local file system
* store metadata as JSON to local file system


----
