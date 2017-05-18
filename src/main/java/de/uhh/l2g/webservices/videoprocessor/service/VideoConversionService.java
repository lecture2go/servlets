package de.uhh.l2g.webservices.videoprocessor.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.io.FilenameUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uhh.l2g.webservices.videoprocessor.dao.GenericDao;
import de.uhh.l2g.webservices.videoprocessor.model.CreatedVideo;
import de.uhh.l2g.webservices.videoprocessor.model.VideoConversion;
import de.uhh.l2g.webservices.videoprocessor.model.VideoConversionStatus;
import de.uhh.l2g.webservices.videoprocessor.model.opencast.Medium;
import de.uhh.l2g.webservices.videoprocessor.model.opencast.Publication;
import de.uhh.l2g.webservices.videoprocessor.util.FileHandler;
import de.uhh.l2g.webservices.videoprocessor.util.FilenameHandler;

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
			genericDao.update(videoConversion);
		} catch(BadRequestException e) {
			videoConversion.setStatus(VideoConversionStatus.ERROR_COPYING_TO_OC_BAD_REQUEST);
			genericDao.update(videoConversion);
			return;
		} catch(WebApplicationException e) {
			videoConversion.setStatus(VideoConversionStatus.ERROR_COPYING_TO_OC);
			genericDao.update(videoConversion);
			return;
		}
		//TODO: this should not be necessary if entity is managed by JPA/Hibernate
		 
		 */
		
		// polling for status
		
		// if successful save files to filesystem and name them
		/*
		String testvideo = "http://134.100.84.23:8080/static/mh_default_org/l2go/5003991e-2569-4f1b-993a-08ae17760bc6/8f6d39e6-dd4c-4a9c-9275-84671703da04/a.mp4";
		
		videoConversion.setStatus(VideoConversionStatus.COPYING_FROM_OC);
		//TODO: this should not be necessary if entity is managed by JPA/Hibernate
		genericDao.update(videoConversion);
		
		try {
			FileHandler.download(testvideo, target);
		} catch (Exception e) {
			videoConversion.setStatus(VideoConversionStatus.ERROR_COPYING_FROM_OC);
			return;
		}
		*/

		// create SMIL file and save it to the filesystem
		
		// remove files from opencast
		
	}
	
	private void cleanup() {
		// deletes old files
	}

	public void handleOpencastResponse(Long id, Boolean success) {
		// get the corresponding videoconversion object
		GenericDao genericDao = GenericDao.getInstance();
		VideoConversion videoConversion = genericDao.get(VideoConversion.class, id);
		
		if (success) {
			// the opencast workflow was successful
			
			// update the status of the video conversion
			videoConversion.setStatus(VideoConversionStatus.OC_SUCCEEDED);
			//TODO: this should not be necessary if entity is managed by JPA/Hibernate
			genericDao.update(videoConversion);
			
			// test without API call:
			ObjectMapper mapper = new ObjectMapper();
			Publication publication = null;
			try {
				publication = mapper.readValue(new File("/Users/matthiashitzler/Documents/10_opencast/publications_api_response.json"), Publication.class);
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// get the event details
			//Publication publication = OpencastApiCall.getPublications(videoConversion.getOpencastId());
			List<CreatedVideo> videos = new ArrayList<CreatedVideo>();
			for(Medium medium: publication.getMedia()) {
				CreatedVideo createdVideo = new CreatedVideo();
				String sourceFilePath = videoConversion.getSourceFilePath();
				
				try {
					// download the file
					String random = "a";
					String targetFilePath = FilenameHandler.switchBasename(sourceFilePath,random);
					FileHandler.download(medium.getUrl(), targetFilePath);
					createdVideo.setFilePath(targetFilePath);
				} catch (IOException e) {
					videoConversion.setStatus(VideoConversionStatus.ERROR_COPYING_FROM_OC);
					GenericDao.getInstance().update(videoConversion);
					return;
				}
				createdVideo.setBitrate(medium.getBitrate());
				createdVideo.setWidth(medium.getWidth());
				createdVideo.setHeight(medium.getHeight());
				
				// while downloading the files there may have been a filename change, reload the object
				videoConversion = genericDao.get(VideoConversion.class, id);
				// rename the file with an added width, example "originalname_1920.mp4"
				String filePath = FilenameHandler.addToBasename(videoConversion.getSourceFilePath(), "_" + medium.getWidth());
				try {
					FileHandler.rename(sourceFilePath, filePath);
				} catch (IOException e) {
					videoConversion.setStatus(VideoConversionStatus.ERROR_RENAMING);
					GenericDao.getInstance().update(videoConversion);
					return;
				}
				createdVideo.setFilePath(filePath);
				
				// persist the created video to database
				GenericDao.getInstance().save(createdVideo);
				
				// add video to list of videos
				videos.add(createdVideo);
			}
		
			// build SMIL file
			
			
		} else {
			// the opencast workflow failed
			
			// update the status of the video conversion
			videoConversion.setStatus(VideoConversionStatus.ERROR_OC_FAILED);
			//TODO: this should not be necessary if entity is managed by JPA/Hibernate
			genericDao.update(videoConversion);
		}
		
	}
}
