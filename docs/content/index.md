---
date: 2016-12-31T20:29:38-05:00
title: Introduction
type: index
weight: 0
---


## Why this OAuth 2.0 Authorization Server

### Fast and small memory footprint to lower production cost.

The Development Edition can support 60000 user login and get authorization code redirect
and can generate 700 access tokens per second on my laptop. 

The Enterprise Edition has 6 microservices connected with in-memory data grid 
and each service can be scaled individually.


### More secure than other implementations

OAuth 2.0 is just a specification and a lot of details are in the individual
implementation. Our implementation has a lot of extensions and enhancements 
for additional security and prevent users making mistakes. For example, we
have added an additional client type called "trusted" and only this type of
client can issue resource owner password credentials grant type. 

### Seamlessly integration with Light-Java framework

* Built on top of Light-Java
* Light-Java Client and Security modules manages all the communication with OAuth2
* Support service on-boarding from Light-Portal
* Support client on-boarding from Light-Portal
* Support user management from Light-Portal
* Open sourced OpenAPI specifications for all microserivces

### Development Edition for API or Service development and integration

It has no dependency and everything is in memory with externalized configuration 
for users and clients. Seamlessly move to enterprise edition or provider edition
for production.

### Public key certificate distribution

With distributed security verification, JWT signature public key certficates must
but distributed to all resource servers. The traditional push approach is not
working with microservices architecture and pull approach is adopted. There is a 
key service with endpoint to retrieve public key certificate from microservices 
during runtime based on the key_id from JWT header.  

### OAuth2 server, portal and light Java to form ecosystem

[light-java](https://github.com/networknt/light-java) to build API

[light-oauth2](https://github.com/networknt/light-oauth2) to control API access

[light-portal](https://github.com/networknt/light-portal) to manage clients and APIs


