#Alfresco Connector

Enables interactions in your BonitaBPM processes with an [Alfresco ECM](https://www.alfresco.com/products/enterprise-content-management/community) using [_Apache Abdera_](https://abdera.apache.org/).

##Description

This connector provides the 4 actions: _Create a folder_,_Delete a folder by path_,_Upload a file_ and _Delete an item by id_. 

##Build

__Clone__ or __fork__ this repository, then at the root of the project run:

`mvn clean verify`

##Release

Before releasing a new version make sure that the versioning of `.def` files are consistent with the implementation. In order to create a new release of the connector use the [_maven release plugin_](http://maven.apache.org/maven-release/maven-release-plugin/):

`mvn release:prepare`

It creates a new git _tag_ and update the current development version. New tags are automatically built on our CI infrastructure.
