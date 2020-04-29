package de.uhh.l2g.webservices.videoprocessor.resources;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
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
//@Secured
@Path("videoconversion")
public class VideoConversionsResource {
	
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
     * Delegates calls to a specific id to the VideoConversionResource class
     *
     * @param id the specific videoConversion
     * @return the single videoConversion resource
     */
	@Path("{id}")
	public VideoConversionResource getVideoConversion(@PathParam("id") Long id) {
		return new VideoConversionResource(id, tenant);
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
		return new VideoConversionResourceBySourceId(sourceId, tenant);
	}
	
    /**
     * Returns a list of video conversions as JSON
     *
     * @return a list of videoconversions, or a NOT FOUND status code when no videoconversions existing
     */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<VideoConversion> getVideoConversions() {
		// return the video conversion list filtered by tenant
		List<VideoConversion> videoConversions = GenericDao.getInstance().getByFieldValue(VideoConversion.class, "tenant", tenant);
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
		// add the tenant
		videoConversion.setTenant(tenant);
		VideoConversionService vc = new VideoConversionService(videoConversion);
		VideoConversion videoConversionDb = vc.runVideoConversion();
		if (videoConversionDb == null) {
			throw new InternalServerErrorException();
		} 
        URI uri = uriInfo.getAbsolutePathBuilder().path(videoConversionDb.getId().toString()).build();
		return Response.created(uri).build();
	}
	
	/**
	 * Rebuilds all SMIL files with the given quality caps
	 * @param uriInfo
	 * @return TODO
	 */
	@Path("rebuild-smil")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response rebuildSmil(Map<String,Object> map, @Context UriInfo uriInfo) {
		if (map == null) {
			throw new BadRequestException();
		}
		String[] tenants = null;
		long maxHeight = 0;
		long maxBitrate= 0;
		
		// get tenants from request body
		ArrayList<String> tenantsList = (ArrayList<String>) map.get("tenants");
		tenants = tenantsList.toArray(new String[0]);
		
		// get max height from request body
		if (map.containsKey("maxHeight")) {
			try {
				maxHeight = Long.valueOf((String) map.get("maxHeight"));
			} catch (NumberFormatException e) {
				// no maxHeight property or could not be parsed, use default
			}
		}
		
		if (map.containsKey("maxBitrate")) {
			try {
				maxBitrate = Long.valueOf((String) map.get("maxBitrate"));
			} catch (NumberFormatException e) {
				// no maxBitrate property or could not be parsed, use default
			}
		}
				
		// pass empty videoconversion to Service as we do not want to run a new video conversion, only trigger the SMIL rebuild
		VideoConversion v = new VideoConversion();
		VideoConversionService vc = new VideoConversionService(v);
		
		long errorCount = vc.rebuildAllSmil(tenants, maxHeight, maxBitrate);
		
		if (errorCount > 0 ){
			Map<String, String> errorDetails = new HashMap<>();
			errorDetails.put("errorCount", String.valueOf(errorCount));
			return Response.serverError().entity(errorDetails).build();
		}
		
		return Response.ok().build();
	}
}