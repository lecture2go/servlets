package de.uhh.l2g.webservices.videoprocessor.resources;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.uhh.l2g.webservices.videoprocessor.model.VideoConversion;
import de.uhh.l2g.webservices.videoprocessor.service.VideoConversionService;

@Path("videoconversion")
public class VideoConversionsResource {
	
	@Path("{id}")
	public VideoConversionResource getVideoConversion(@PathParam("id") Long id) {
		return new VideoConversionResource(id);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<VideoConversion> getVideoConversions() {
		return null;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postVideoConversions(VideoConversion videoConversion) {
		VideoConversionService vc = new VideoConversionService();
		vc.runVideoConversion(videoConversion);
		return null;
	}
}