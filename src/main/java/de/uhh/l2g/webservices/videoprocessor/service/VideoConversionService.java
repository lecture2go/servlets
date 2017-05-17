package de.uhh.l2g.webservices.videoprocessor.service;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.WebApplicationException;

import de.uhh.l2g.webservices.videoprocessor.dao.GenericDao;
import de.uhh.l2g.webservices.videoprocessor.model.VideoConversion;
import de.uhh.l2g.webservices.videoprocessor.model.VideoConversionStatus;
import de.uhh.l2g.webservices.videoprocessor.util.FileHandler;

public class VideoConversionService {
	
	public void runVideoConversion(VideoConversion videoConversion) {
		videoConversion.setStatus(VideoConversionStatus.COPYING_TO_OC);
		
		// save metadata to database (id / path)
		GenericDao genericDao = GenericDao.getInstance();
		
		// there is no autoincrement id, as the source id is used. a new videoconversion process results in the cleanup 
		// of a current or finished videoconversion with the same id
		VideoConversion videoConversionDb = genericDao.get(VideoConversion.class, videoConversion.getSourceId());
		if (videoConversionDb == null) {
			videoConversion = genericDao.save(videoConversion);
		} else {
			cleanup();
			genericDao.deleteById(VideoConversion.class, videoConversionDb.getSourceId());
			genericDao.save(videoConversion);
		}
		//videoConversion = genericDao.save(videoConversion);

		// delete old files
		// cleanup();
		/*
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
		 
		 */
		genericDao.update(videoConversion);
		
		// polling for status
		
		// if successful save files to filesystem and name them
		String testvideo = "http://134.100.84.23:8080/static/mh_default_org/l2go/5003991e-2569-4f1b-993a-08ae17760bc6/8f6d39e6-dd4c-4a9c-9275-84671703da04/a.mp4";
		String target = "/Users/matthiashitzler/Documents/10_opencast/testvideos/" + videoConversion.getSourceFileName() + "_oc.mp4";
		
		videoConversion.setStatus(VideoConversionStatus.COPYING_FROM_OC);
		//TODO: this should not be necessary if entity is managed by JPA/Hibernate
		genericDao.update(videoConversion);
		
		try {
			FileHandler.download(testvideo, target);
		} catch (Exception e) {
			videoConversion.setStatus(VideoConversionStatus.ERROR_COPYING_FROM_OC);
		}

		// create SMIL file and save it to the filesystem
		
		// remove files from opencast
		
	}
	
	private void cleanup() {
		// deletes old files
	}
}
