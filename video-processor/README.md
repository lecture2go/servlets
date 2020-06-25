# Video-processor

# Description

## Overview

The video-processor module adds the possibility to do post-processing tasks with media uploaded to the Lecture2Go media portal software (but may also be used without Lecture2Go). It integrates with Opencast (https://github.com/opencast/opencast) in that way as it uses it as a "post-processing engine". Its main focus is to transcode a video file into multiple qualities and provide a corresponding metadata file for adaptive Streaming (SMIL). Opencast is only used as a post-processing provider and the data does NOT reside in the Opencast system longer than the post-processing takes time. As the module was meant as an optional add-on to Lecture2Go and for possible other purposes, the code is not integrated in the Lecture2Go code base, but as an external module. This servlet is completely controlled via a REST-API.

This documentation primarily describes the steps to get the video-processor servlet running with Lecture2Go, a detailed documentation regarding the REST-endpoints is missing at the moment. 

## Features

* sends media files from local storage/ NFS to Opencast, receives the generated media files after the workflow has finished and creates a SMIL file for adaptive Streaming
* file handling for transcoded files to support Lecture2Go-specific processes (activation/ deactivation/ deletion)
* support for generating a downloadable version of the video file with a configurable target bitrate
* support for getting thumbnails from Opencast in multiple qualities, if thumbnails are used in the Opencast workflow
* support for sending Opencast workflow attributes to enable complex workflows
* support for restricting the qualities in the SMIL file to max bitrates or max resolution via config and API
* supports multiple tenant systems with one servlet

# Installation

## Prerequisites

* Opencast installation with activated basic authentication for external API
* MySQL Server with empty database

## Installation steps

* Download the servlet code
* edit src/main/resources/config.properties (see "Configuration")
* edit /src/main/resources/META-INF/persistence.xml (see "Configuration")
* build the code with maven
* Copy resulting WAR file to a servlet container, e. g. Tomcat (may run on the same Tomcat as Lecture2Go, but access to this servlet should not be public)
* For integration with Lecture2Go, the video files must reside in the same folder structure as on the Lecture2Go system - on the same server or via NFS with the same mount points

## Configuration

* config.properties
  * the thumbnails properties are optional and may be set if the opencast workflow produces thumbnails (this is not the default case, as Lecture2Go historically generates the thumbnails itself) 
```

# url to this instance of the video-processor
url.videoconversion={path to servlet with trailing /videoconversion}, e.g. http://localhost:8081/video-processor/videoconversion

# optional: local folder where thumbnails are stored
folder.thumbnails=/fms/applications/images/
folder.thumbnails.suffix.medium=_m
folder.thumbnails.suffix.small=_s

# opencast api properties
opencast.url.api={url to opencast external API}, e.g. http://localhost:8080/api/
opencast.user={opencast user}
opencast.pass={opencast password}

# this is the default workflow, it can be overwritten by a request
opencast.conversion.workflow=your-workflow-name
opencast.conversion.acl.write=ROLE_ADMIN
opencast.conversion.acl.read=ROLE_USER

opencast.thumbnail.type.full=presenter/thumbnail
opencast.thumbnail.type.medium=presenter/thumbnail-medium
opencast.thumbnail.type.small=presenter/thumbnail-small

# limit the quality, to restrict videos from being used in the smil (0 deactivates restriction)
smil.restriction.max.height=0
smil.restriction.max.bitrate=0
```

* persistence.xml
  * change the following to match your mysql database for the video-processor:
    * hibernate.connection.url
    * hibernate.connection.username
    * hibernate.connection.password

## Integration in Lecture2Go

* Lecture2Go provides integration with the servlet, which can be activated in the Liferay properties file, e.g. portal-ext.properties

```
############ videoprocessing ############
lecture2go.videoprocessing.provider={url to the servlet, e.g. http://localhost:8081/video-processor}
lecture2go.videoprocessing.provider.videoconversion=${lecture2go.videoprocessing.provider}/videoconversion
# optional, the tenant id, must be used if multiple l2go instances work with the same video-processor
#lecture2go.videoprocessing.tenant=${lecture2go.web.root}

# the suffix of the downloadable file
lecture2go.videoprocessing.downloadsuffix=_dl
# the intended bit rate in bit/s for the downloadable file
lecture2go.videoprocessing.targetdownloadbitrate=1400000

# optional, the default workflow, may be set in the video-processor config
lecture2go.videoprocessing.workflow=your-workflow-name
```

## Opencast workflow/ encoding profile

* multiple resolutions should be generated to make good use of the adaptive streaming functionality
* the workflow and encoding profile must use the "presenter" source-flavor as the video from the video-processor is send to this flavor
* the flavor of the resulting files must have the target-flavor of "presenter/delivery"
* following workflow-operation must be used at the end of the workflow to communicate with the video-processor

```
 <operation
      id="http-notify"
      fail-on-error="true"
      description="Notify Lecture2Go"
      retry-strategy="hold"
      max-attempts="50"
      if="${id} &gt; 0">
      <configurations>
        <configuration key="url">${url}/${id}</configuration>
        <configuration key="subject">${id}</configuration>
        <configuration key="method">put</configuration>
        <configuration key="message">true</configuration>
        <configuration key="max-retry">5</configuration>
        <configuration key="timeout">240</configuration>
      </configurations>
    </operation>
```

## Security

* the servlet has the possibility to add basic http authentication, BUT as the Opencast-WOH http-notify seems to not support it, the servlet accessibility should be publicly restricted. At least from the Lecture2Go system and the Opencast admin node requests must be allowed.

# Usage

* Lecture2Go has an automatic integration with the servlet, so after configuring and installing it, all files uploaded are automatically sent to the video-processor for further processing. Furthermore the system-admin has a button to manually start a video conversion, e.g. for older files
* Notice: the data resides only during the post-processing in the Opencast system, it is deleted from there after Opencast has done its job.