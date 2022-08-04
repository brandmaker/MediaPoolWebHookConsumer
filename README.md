<img align="right" src="https://raw.githubusercontent.com/brandmaker/MediaPoolWebHookConsumer/master/BrandMaker_Logo_on_light_bg.png" alt="BrandMaker" width="30%" height="30%">

# BrandMaker MediaPool WebHook Consumer Example

## Motivation

In order to synchronize contents and assets, which are stored in BrandMakers DAM Solution "Media Pool", a powerfull WebHook integration is offered.

To register and retrieve events from this integration, integrators need to implement a REST endpoint to capture the POST data and pull 
the according information from the MediaPool API.

## Scope

This repository contains a blue print for a webhook, which is capable of the following:

* :heavy_check_mark: register for events like "PUBLISHED" or "DEPUBLISHED"
* :heavy_check_mark: retrieve the POST message for the events
* :heavy_check_mark: validate POST data
* :heavy_check_mark: Add a job to an internal processing queue
* :heavy_check_mark: Within the queue listener
	* :heavy_check_mark: pull asset metadata from Media Pool via REST API
	* :heavy_check_mark: pull binary of requested version and rendition from REST API
	* :heavy_check_mark: store binary to local file system
	* :heavy_check_mark: store metadata as JSON to local file system

## Prerequisits

### Environment

* Java >= 9
* Spring https://spring.io/
* ActivMQ https://activemq.apache.org/
* Eclipse / IntelliJ
* Maven 
* Github

### BrandMaker Media Pool

Please make yousrself familiar with the basic principles of Webhooks. There are two comprehensive and recommended introductions available here:

* https://requestbin.com/blog/working-with-webhooks/
* https://en.wikipedia.org/wiki/Webhook. 

Furthermore, to understand how BrandMaker Media Pool is making use of this kind of loosely coupled integrations, read the tutorial available here
https://github.com/brandmaker/MediaPoolWebHookConsumer/blob/master/Media-Pool_WebHook-Push-API-Description_6-6_EN_20200203.pdf

### Authentication

BrandMaker releases prior to 7.0 need Basic Authentication. Please provide a user id and a password within the application.yaml file for these versions.

With LTS release 7.0 and newer, please use CAS in order to authenticate via oAuth2 access tokens. In order to retrieve those tokens, follow the guideline available on https://developers.brandmaker.com/guides/auth/ and the following steps:

1. Register your client in the administration (Fusion / Registered Apps)
1. Authenticate a user via usual oAuth2 web flow, i.e. with "postman"
1. Store the tokens etc. into a JSON file in the working directory of the running application
1. Configure the file in the application.yaml and uncomment userid and password

Example of a credentials JSON file:

```
{
  "server" : "https://cas.brandmaker.com/api/v1.1/token",
  "clientId" : "9d4 ... ca",
  "clientSecret" : "1 ... Eo",
  "accessToken" : {
    "token" : "__--CAS--__uXWiXloK6Y3fA j.. .y6durAClyKMmlyclfDgb19Od6M8s8",
    "expires" : "2022-08-04T08:43:52Z"
  },
  "refreshToken" : {
    "token" : "__--CAS--__UlrLUt_yPjqtY. .. .MeUDhocmE9DN6wUVnmX6XNPvkepQTXsCk",
    "expires" : "2023-08-04T07:43:52Z"
  }
}
```


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

[![Java CI with Maven](https://github.com/brandmaker/MediaPoolWebHookConsumer/actions/workflows/maven.yml/badge.svg)](https://github.com/brandmaker/MediaPoolWebHookConsumer/actions/workflows/maven.yml)


# Further Information

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.3.0.M3/maven-plugin/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.3.0.M3/maven-plugin/html/#build-image)
