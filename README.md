<img align="right" src="https://raw.githubusercontent.com/brandmaker/MediaPoolWebHookConsumer/master/BrandMaker_Logo_on_light_bg.png" alt="BrandMaker" width="30%" height="30%">

# BrandMaker MediaPool WebHook Consumer Example

## Motivation

In order to synchronize contents and assets, which are stored in BrandMakers DAM Solution "Media Pool", a powerfull WebHook integration is offered.

To register and retrieve events from this integration, integrators need to implement a REST endpoint to capture the POST data and pull 
the according information from the MediaPool API.

## Scope

This repository contains a blue print for a webhook, which is capable of the following:

* (/) register for events like "PUBLISHED" or "DEPUBLISHED"
* retrieve the POST message for the events
* validate POST data
* Add a job to an internal processing queue
* Within the queue listener
	* pull asset metadata from Media Pool via REST API
	* pull binary of requested version and rendition from REST API
	* store binary to local file system
	* store metadata as JSON to local file system

## Prerequisits

### Environment

* Java >= 8
* Spring https://spring.io/
* ActivMQ https://activemq.apache.org/
* Eclipse / IntelliJ
* Maven 
* Github
* Travis-CI https://travis-ci.org/getting_started

### BrandMaker Media Pool

Please make yousrself familiar with the basic principles of Webhooks. There are two comprehensive and recommended introductions available here:

* https://requestbin.com/blog/working-with-webhooks/
* https://en.wikipedia.org/wiki/Webhook. 

Furthermore, to understand how BrandMaker Media Pool is making use of this kind of loosely coupled integrations, read the tutorial available here
https://github.com/brandmaker/MediaPoolWebHookConsumer/blob/master/Media-Pool_WebHook-Push-API-Description_6-6_EN_20200203.pdf

## General Concept

According to the recommendations to not to process the events immediately within the hook itself, the structure of a basic consumer looks like

![Consumer Structure](./Media%20Poool%20Web-Hooks%20Consumer.png)

The processing queue in the above flow chart will be implemented with the use of Spring JMS and ActiveMQ. In order to make this example as stand alone as possible, 
we will use the embedded broker of ActiveMQ. Any available broker can be configured through the `application.yaml` file. The internally used broker wille be configured persistant.
The queue topic can be configured within the `application.yaml` as well.

On https://codenotfound.com/spring-jms-activemq-example.html  you will find further information on how to integrate Spring JMS and ActiveMQ. 

The effective processing of the event (i.e. picking up meta data and binary from Media Pool via REST API etc.) takes place in the queue listener.

## Usage

### Build

Within the root directory of your project, run `mvnw clean package` and `mvnw javadoc:javadoc`

### Test

In order to test your Media Pool Webhook, open a CLI in the root directory of your project and start springboot with

```
java -Dserver.address=0.0.0.0 -jar target\webhook.consumer-0.0.1-SNAPSHOT.jar
```

Access to the Webhook: POST to http://localhost:8080/hook

Api Documentation and test client generated with open-api and swagger-ui: http://localhost:8080/api-docs.html

## Project state

[![Build Status](https://travis-ci.org/brandmaker/MediaPoolWebHookConsumer.svg?branch=master)](https://travis-ci.org/brandmaker/MediaPoolWebHookConsumer)


# Further Information

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.3.0.M3/maven-plugin/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.3.0.M3/maven-plugin/html/#build-image)
