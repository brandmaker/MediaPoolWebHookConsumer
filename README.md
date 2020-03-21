<img align="right" src="https://raw.githubusercontent.com/brandmaker/MediaPoolWebHookConsumer/master/BrandMaker_Logo_on_light_bg.png" alt="BrandMaker" width="30%" height="30%">

# BrandMaker MediaPool WebHook Consumer Example

## Motivation

In order to synchronize contents and assets, which are stored in BrandMakers DAM Solution "Media Pool", a powerfull WebHook integration is offered.

To register and retrieve events from this integration, integrators need to implement a REST endpoint to capture the POST data and pull 
the according information from the MediaPool API.

## Scope

This repository contains a blue print for a webhook, which is capable of the following:

* register for events like "PUBLISHED" or "DEPUBLISHED"
* retrieve the POST message for the events
* validate POST data
* pull asset metadata from Media Pool via REST API
* pull binary of requested version and rendition from REST API
* store binary to local file system
* store metadata as JSON to local file system

## Prerequisits

Please make yousrself familiar with the basic principles of Webhooks. There are two comprehensive and recommended introductions available here:

* https://requestbin.com/blog/working-with-webhooks/
* https://en.wikipedia.org/wiki/Webhook. 

Furthermore, to understand how BrandMaker Media Pool is making use of this kind of loosely coupled integrations, read the tutorial available here
https://github.com/brandmaker/MediaPoolWebHookConsumer/blob/master/Media-Pool_WebHook-Push-API-Description_6-6_EN_20200203.pdf

## Usage

- TBD

## Project state

[![Build Status](https://travis-ci.org/brandmaker/MediaPoolWebHookConsumer.svg?branch=master)](https://travis-ci.org/brandmaker/MediaPoolWebHookConsumer)


# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.3.0.M3/maven-plugin/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.3.0.M3/maven-plugin/html/#build-image)
