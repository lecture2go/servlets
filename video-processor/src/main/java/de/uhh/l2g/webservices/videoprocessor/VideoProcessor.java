package de.uhh.l2g.webservices.videoprocessor;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * Jersey/Jax-RS specific entry point for the webservice
 * (avoid the need of a web.xml file)
 */
@ApplicationPath("/")
public class VideoProcessor extends ResourceConfig {
	
	public VideoProcessor() {
		// Register resources and providers using package-scanning.
        packages("de.uhh.l2g.webservices.videoprocessor");
	}
}
