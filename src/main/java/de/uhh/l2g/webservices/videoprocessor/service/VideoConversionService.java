package de.uhh.l2g.webservices.videoprocessor.service;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.WebApplicationException;

import de.uhh.l2g.webservices.videoprocessor.dao.GenericDao;
import de.uhh.l2g.webservices.videoprocessor.model.VideoConversion;
import de.uhh.l2g.webservices.videoprocessor.model.VideoConversionStatus;

public class VideoConversionService {
	
	public void runVideoConversion(VideoConversion videoConversion) {
		// save metadata to database (id / path)
		GenericDao genericDao = GenericDao.getInstance();
		videoConversion.setStatus(VideoConversionStatus.COPYING_TO_OC);
		videoConversion = genericDao.save(videoConversion);

		// delete old files
		cleanup();
		
		// create a new opencast event via the opencast API
		try {
			String opencastId = OpencastApiCall.postNewEventRequest(videoConversion.getSourceFilePath(), videoConversion.getSourceFileName(), videoConversion.getSourceId());
			videoConversion.setOpencastId(opencastId);
			videoConversion.setStatus(VideoConversionStatus.OC_RUNNING);
		} catch(BadRequestException e) {
			videoConversion.setStatus(VideoConversionStatus.ERROR_COPYING_TO_OC_BAD_REQUEST);
		} catch(WebApplicationException e) {
			videoConversion.setStatus(VideoConversionStatus.ERROR_COPYING_TO_OC);
		}
		//TODO: this should not be necessary if entity is managed by JPA/Hibernate
		genericDao.update(videoConversion);
		
		// polling for status
		
		// if successful save files to filesystem and name them
		
		// create SMIL file and save it to the filesystem
		
		// remove files from opencast
		
	}
	
	private void cleanup() {
		// deletes old files
	}
}
