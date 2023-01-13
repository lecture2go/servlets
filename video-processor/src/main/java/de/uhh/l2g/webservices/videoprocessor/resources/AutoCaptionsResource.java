package de.uhh.l2g.webservices.videoprocessor.resources;

import java.net.URI;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
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
import de.uhh.l2g.webservices.videoprocessor.filter.LoggingFilter.Logged;
import de.uhh.l2g.webservices.videoprocessor.model.AutoCaption;
import de.uhh.l2g.webservices.videoprocessor.model.VideoConversion;
import de.uhh.l2g.webservices.videoprocessor.service.AutoCaptionService;
import de.uhh.l2g.webservices.videoprocessor.service.VideoConversionService;

/**
 *  A list of autocaptions as a resource
 *
 *  Allowed methods: GET, POST
 *  All requests to this resource will be logged
 */
@Logged
@Path("autocaption")
public class AutoCaptionsResource {
protected String tenant;
	
	/**
	 * This method is called upon each request to set the tenant from the header if there is any
	 */
	@Context
	public void setServletContext(HttpServletRequest request) {
		String tenant = request.getHeader("Tenant");
		this.tenant = tenant;
	}
	
    /**
     * Delegates calls to a specific id to the AutoCaptionResource class
     *
     * @param id the specific autoCaption
     * @return the single autoCaption resource
     */
	@Path("{id}")
	public AutoCaptionResource getVideoConversion(@PathParam("id") Long id) {
		return new AutoCaptionResource(id, tenant);
	}
	
    /**
     * Delegates calls to a specific source-id to the VideoConversionResourceBySourceId class
     *
     * @param sourceId the specific videoConversion via the source id
     * @return the single videoConversion resource
     */
	@Path("sourceid/{id}")
	public AutoCaptionResourceBySourceId getAutoCaptionBySourceId(@PathParam("id") Long sourceId) {
		// the sourceId is used as an identifier
		return new AutoCaptionResourceBySourceId(sourceId, tenant);
	}
	
    /**
     * Returns a list of auto captions as JSON
     *
     * @return a list of auto captions, or a NOT FOUND status code when no auto captions existing
     */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<AutoCaption> getAutoCaptions() {
		// return the video conversion list filtered by tenant
		List<AutoCaption> autoCaptions = GenericDao.getInstance().getByFieldValue(AutoCaption.class, "tenant", tenant);
		if (autoCaptions.isEmpty()) {
			// the default ExceptionMapper takes care of the correct header status code
            throw new NotFoundException();
		}
		return autoCaptions;
	}
	
    /**
     * Starts a new auto caption
     *
     * @param autoCaption the new autoCaption
     * @param uriInfo the URI of the request
     * @return a response with the new URI of the resource
     */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postAutoCaptions(AutoCaption autoCaption, @Context UriInfo uriInfo) {
		// add the tenant
		autoCaption.setTenant(tenant);
		AutoCaptionService ac = new AutoCaptionService(autoCaption);
		AutoCaption autoCaptionDb = ac.runAutoCaptioning();
		if (autoCaptionDb == null) {
			throw new InternalServerErrorException();
		} 
        URI uri = uriInfo.getAbsolutePathBuilder().path(autoCaptionDb.getId().toString()).build();
		return Response.created(uri).build();
	}
}
