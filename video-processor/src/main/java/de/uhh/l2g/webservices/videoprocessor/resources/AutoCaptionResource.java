package de.uhh.l2g.webservices.videoprocessor.resources;

import java.util.HashMap;
import java.util.Objects;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.uhh.l2g.webservices.videoprocessor.dao.GenericDao;
import de.uhh.l2g.webservices.videoprocessor.filter.LoggingFilter.Logged;
import de.uhh.l2g.webservices.videoprocessor.model.AutoCaption;
import de.uhh.l2g.webservices.videoprocessor.model.VideoConversion;
import de.uhh.l2g.webservices.videoprocessor.service.AutoCaptionService;
import de.uhh.l2g.webservices.videoprocessor.service.VideoConversionService;

/**
 * A autoCaption as a resource
 *
 * Allowed methods: GET
 * All requests to this resource will be logged
 */
@Logged
public class AutoCaptionResource {
	protected AutoCaption autoCaption;
	protected String tenant;
	
	public AutoCaptionResource() {
	}
	
	public AutoCaptionResource(Long id) {
		autoCaption = GenericDao.getInstance().get(AutoCaption.class, id);
	}
	
	public AutoCaptionResource(Long id, String tenant) {
		this.tenant = tenant;
		// get the autoCaption by id (ignore the tenant for now as we must allow tenant-independent communication for a PUT request)
		autoCaption = GenericDao.getInstance().get(AutoCaption.class, id);
	}
	
    /**
     * Returns a autoCaption as JSON
     *
     * @return the autoCaption with the given id, or a NOT FOUND status code ifa utoCaption not exists
     */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public AutoCaption getAutoCaption() {
		// check if autoCAption does not exist or tenant is incorrect
		if (autoCaption == null || !Objects.equals(autoCaption.getTenant(),tenant)) {
			// the default ExceptionMapper takes care of the correct header status code
            throw new NotFoundException();
		}
		return autoCaption;
	}
	
    /**
     * Passes a success or failure message to the autoCaption
     * This this used for notify this webservice if the auto caption was successful or not
     *
     * @param success boolean variable if the process was successful or not
     * @return a 200 ok response if successful, or a NOT FOUND status code if resource not existing or renaming 
     */
	@PUT
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED})
	public void putAutoCaptionFormData(@FormParam("message") Boolean success) {
		AutoCaptionService ac = new AutoCaptionService(autoCaption);
		ac.handleS2TResponse(success);
	}
}
