package de.uhh.l2g.webservices.videoprocessor.resources;

import java.net.URI;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import de.uhh.l2g.webservices.videoprocessor.dao.GenericDao;
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
		List<VideoConversion> videoConversions = GenericDao.getInstance().getAll(VideoConversion.class);
		if (videoConversions.isEmpty()) {
			// the default ExceptionMapper takes care of the correct header status code
            throw new NotFoundException();
		}
		return videoConversions;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postVideoConversions(VideoConversion videoConversion, @Context UriInfo uriInfo) {

		VideoConversionService vc = new VideoConversionService(videoConversion);
		VideoConversion videoConversionDb = vc.runVideoConversion();
		if (videoConversion == null) {
			throw new InternalServerErrorException();
		} 
        URI uri = uriInfo.getAbsolutePathBuilder().path(videoConversionDb.getSourceId().toString()).build();
		return Response.created(uri).build();
	}
}