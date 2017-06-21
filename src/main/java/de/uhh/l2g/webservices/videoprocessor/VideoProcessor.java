package de.uhh.l2g.webservices.videoprocessor;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/")
public class VideoProcessor extends ResourceConfig {
	
	public VideoProcessor() {
		// Register resources and providers using package-scanning.
        packages("de.uhh.l2g.webservices.videoprocessor");
	}
}
