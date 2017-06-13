package de.uhh.l2g.webservices.videoprocessor.resources;

import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.uhh.l2g.webservices.videoprocessor.dao.GenericDao;
import de.uhh.l2g.webservices.videoprocessor.model.VideoConversion;
import de.uhh.l2g.webservices.videoprocessor.service.VideoConversionService;

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
	public Response putVideoConversion(VideoConversion videoConversion) {
		VideoConversion videoConversionDb = GenericDao.getInstance().get(VideoConversion.class, id);
		return null;
	}
	
	@Path("filename")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON) 
	public Response postFilenameForVideoConversion(HashMap<String,String> filenameMap) {
		VideoConversionService vc = new VideoConversionService();
		vc.renameFiles(id, filenameMap.get("sourceFileName"));
		
		return null;
	}
	
	@PUT
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED})
	public void putVideoConversionFormData(@FormParam("message") Boolean success) {
		VideoConversionService vc = new VideoConversionService();
		vc.handleOpencastResponse(id, success);
	}
	
	
	
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteVideoConversion() {
		VideoConversionService vc = new VideoConversionService();
		//vc.delete(id);

		return null;
	}
}
