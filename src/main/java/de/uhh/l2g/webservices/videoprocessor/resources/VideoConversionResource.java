package de.uhh.l2g.webservices.videoprocessor.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.uhh.l2g.webservices.videoprocessor.dao.GenericDao;
import de.uhh.l2g.webservices.videoprocessor.model.VideoConversion;

public class VideoConversionResource {
	private Long id;
	
	public VideoConversionResource(Long id) {
		this.id = id;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public VideoConversion getVideoConversion() {
		return GenericDao.getInstance().get(VideoConversion.class, id);
	}
	

	@PUT
	@Consumes({MediaType.APPLICATION_JSON})
	public Response putVideoConversion() {
		System.out.println("it is a json");

		return null;
	}
	
	@PUT
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED})
	public Response putVideoConversionFormData(@FormParam("message") String vc) {
		System.out.println("it is a form data");

		return null;
	}
	
	
	
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteVideoConversion() {
		return null;
	}
}
