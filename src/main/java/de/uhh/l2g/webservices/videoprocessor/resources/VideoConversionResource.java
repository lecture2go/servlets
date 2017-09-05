package de.uhh.l2g.webservices.videoprocessor.resources;

import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.uhh.l2g.webservices.videoprocessor.dao.GenericDao;
import de.uhh.l2g.webservices.videoprocessor.filter.BasicAuthenticationFilter.Secured;
import de.uhh.l2g.webservices.videoprocessor.filter.LoggingFilter.Logged;
import de.uhh.l2g.webservices.videoprocessor.model.VideoConversion;
import de.uhh.l2g.webservices.videoprocessor.service.VideoConversionService;

/**
 * A videoConversion as a resource
 *
 * Allowed methods: GET, PUT, DELETE
 * All requests to this resource will be logged
 */
@Logged
@Secured
public class VideoConversionResource {
	protected Long id;
	protected VideoConversion videoConversion;
	
	public VideoConversionResource() {
	}
	
	public VideoConversionResource(Long id) {
		this.id = id;
		videoConversion = GenericDao.getInstance().get(VideoConversion.class, id);
	}
	
    /**
     * Returns a videoConversion as JSON
     *
     * @return the videoConversion with the given id, or a NOT FOUND status code if videoConversion not exists
     */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public VideoConversion getVideoConversion() {
		if (videoConversion == null) {
			// the default ExceptionMapper takes care of the correct header status code
            throw new NotFoundException();
		}
		return videoConversion;
	}
	

    /**
     * Renames the files of an existing videoConversion
     *
     * @param filenameMap a simple key value pair with a "sourceFileName" as key and the necessary value
     * @return a 200 ok response if successful, or a NOT FOUND status code if resource not existing or renaming 
     */
	@Path("filename")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postFilenameForVideoConversion(HashMap<String,String> filenameMap) {
		VideoConversionService vc = new VideoConversionService(videoConversion);
		if (vc.renameFiles(filenameMap.get("sourceFileName"))) {
			return Response.ok().build();
		} else {
			throw new InternalServerErrorException();
		}
	}
	
    /**
     * Passes a success or failure message to the videoConversion
     * This this used for notify this webservice if the video conversion was successful or not
     *
     * @param success boolean variable if the process was successful or not
     * @return a 200 ok response if successful, or a NOT FOUND status code if resource not existing or renaming 
     */
	@PUT
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED})
	public void putVideoConversionFormData(@FormParam("message") Boolean success) {
		VideoConversionService vc = new VideoConversionService(videoConversion);
		vc.handleOpencastResponse(success);
	}
	
    /**
     * Deletes a videoConversion
     *
     * @return a response with a status code 200 (ok), or a NOT FOUND status code via the ExceptionMapper
     */
	@DELETE
	public Response deleteVideoConversion() {
		VideoConversionService vc = new VideoConversionService(videoConversion);
		if (vc.delete()) {
			return Response.ok().build();
		} else {
			throw new InternalServerErrorException();
		}
	}
}
