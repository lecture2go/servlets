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
		VideoConversion videoConversion = GenericDao.getInstance().get(VideoConversion.class, id);
		if (videoConversion == null) {
			// the default ExceptionMapper takes care of the correct header status code
            throw new NotFoundException();
		}
		return videoConversion;
	}
	

	
	@Path("filename")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postFilenameForVideoConversion(HashMap<String,String> filenameMap) {
		VideoConversion videoConversion = GenericDao.getInstance().get(VideoConversion.class, id);
		VideoConversionService vc = new VideoConversionService(videoConversion);
		if (vc.renameFiles(filenameMap.get("sourceFileName"))) {
			return Response.ok().build();
		} else {
			throw new InternalServerErrorException();
		}
	}
	
	@PUT
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED})
	public void putVideoConversionFormData(@FormParam("message") Boolean success) {
		VideoConversion videoConversion = GenericDao.getInstance().get(VideoConversion.class, id);
		VideoConversionService vc = new VideoConversionService(videoConversion);
		vc.handleOpencastResponse(success);
	}
	
	
	
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteVideoConversion() {
		VideoConversion videoConversion = GenericDao.getInstance().get(VideoConversion.class, id);
		VideoConversionService vc = new VideoConversionService(videoConversion);
		if (vc.delete()) {
			return Response.ok().build();
		} else {
			throw new InternalServerErrorException();
		}
	}
}
