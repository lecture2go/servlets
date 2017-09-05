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
import de.uhh.l2g.webservices.videoprocessor.filter.BasicAuthenticationFilter.Secured;
import de.uhh.l2g.webservices.videoprocessor.filter.LoggingFilter.Logged;
import de.uhh.l2g.webservices.videoprocessor.model.VideoConversion;
import de.uhh.l2g.webservices.videoprocessor.service.VideoConversionService;

/**
 *  A list of videoconversions as a resource
 *
 *  Allowed methods: GET, POST
 *  All requests to this resource will be logged
 */
@Logged
@Secured
@Path("videoconversion")
public class VideoConversionsResource {
	
    /**
     * Delegates calls to a specific id to the VideoConversionResource class
     *
     * @param id the specific videoConversion
     * @return the single videoConversion resource
     */
	@Path("{id}")
	public VideoConversionResource getVideoConversion(@PathParam("id") Long id) {
		return new VideoConversionResource(id);
	}
	
    /**
     * Delegates calls to a specific source-id to the VideoConversionResourceBySourceId class
     *
     * @param sourceId the specific videoConversion via the source id
     * @return the single videoConversion resource
     */
	@Path("sourceid/{id}")
	public VideoConversionResourceBySourceId getVideoConversionBySourceId(@PathParam("id") Long sourceId) {
		// the sourceId is used as an identifier
		return new VideoConversionResourceBySourceId(sourceId);
	}
	
    /**
     * Returns a list of video conversions as JSON
     *
     * @return a list of videoconversions, or a NOT FOUND status code when no videoconversions existing
     */
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
	
    /**
     * Starts a new video conversion
     *
     * @param videoConversion the new videoConversion
     * @param uriInfo the URI of the request
     * @return a response with the new URI of the resource
     */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postVideoConversions(VideoConversion videoConversion, @Context UriInfo uriInfo) {

		VideoConversionService vc = new VideoConversionService(videoConversion);
		VideoConversion videoConversionDb = vc.runVideoConversion();
		if (videoConversionDb == null) {
			throw new InternalServerErrorException();
		} 
        URI uri = uriInfo.getAbsolutePathBuilder().path(videoConversionDb.getId().toString()).build();
		return Response.created(uri).build();
	}
}