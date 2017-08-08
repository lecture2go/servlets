package de.uhh.l2g.webservices.videoprocessor.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uhh.l2g.webservices.videoprocessor.dao.GenericDao;
import de.uhh.l2g.webservices.videoprocessor.model.CreatedFile;
import de.uhh.l2g.webservices.videoprocessor.model.CreatedVideo;
import de.uhh.l2g.webservices.videoprocessor.model.VideoConversion;
import de.uhh.l2g.webservices.videoprocessor.model.VideoConversionStatus;
import de.uhh.l2g.webservices.videoprocessor.model.opencast.Medium;
import de.uhh.l2g.webservices.videoprocessor.model.opencast.Publication;
import de.uhh.l2g.webservices.videoprocessor.model.opencast.Video;
import de.uhh.l2g.webservices.videoprocessor.util.FileHandler;
import de.uhh.l2g.webservices.videoprocessor.util.FilenameHandler;
import de.uhh.l2g.webservices.videoprocessor.util.SmilBuilder;


/**
 * The VideoConversionService handles all logic of the video conversion
 */
public class VideoConversionService {
	
	private static final Logger logger = LogManager.getLogger(VideoConversionService.class);
	private VideoConversion videoConversion;
	
	public VideoConversionService(VideoConversion videoConversion) {
		this.videoConversion = videoConversion;
	}
	
	/**
	 * Persists a given status of a video conversion
	 * @param status the status to persist (as given in the VideoConversionStatus enum)
	 */
	public void persistVideoConversionStatus(VideoConversionStatus status) {
		//TODO: this should not be necessary if entity is managed by JPA/Hibernate
		GenericDao.getInstance().get(VideoConversion.class, videoConversion.getSourceId());
		videoConversion.setStatus(status);
		GenericDao.getInstance().update(videoConversion);
		logger.info("The new status of the videoConversion with source id {} is {}", videoConversion.getSourceId(), status);
	}
	
	/**
	 * This runs the actual video conversion
	 * A new event request with the file is sent to opencast to do the heavy lifting
	 */
	public VideoConversion runVideoConversion() {		

		logger.info("A new videoConversion is started for the sourceId {}", videoConversion.getSourceId());

		// save metadata to database (id / path)
		
		// there is no autoincrement id, as the source id is used. a new videoconversion process results in the cleanup 
		// of a current or finished videoconversion with the same id
		VideoConversion videoConversionDb = GenericDao.getInstance().get(VideoConversion.class, videoConversion.getSourceId());
		if (videoConversionDb != null) {
			// delete old files
			cleanup(videoConversionDb);
		}
		videoConversion = GenericDao.getInstance().save(videoConversion);
		
		persistVideoConversionStatus(VideoConversionStatus.COPYING_TO_OC);
		
		// create a new opencast event via the opencast API
		try {
			String opencastId = OpencastApiCall.postNewEventRequest(videoConversion.getSourceFilePath(), videoConversion.getSourceFilename(), videoConversion.getSourceId());
			videoConversion.setOpencastId(opencastId);
			persistVideoConversionStatus(VideoConversionStatus.OC_RUNNING);
		} catch(BadRequestException e) {
			persistVideoConversionStatus(VideoConversionStatus.ERROR_COPYING_TO_OC_BAD_REQUEST);
			return null;
		} catch(WebApplicationException e) {
			persistVideoConversionStatus(VideoConversionStatus.ERROR_COPYING_TO_OC);
			return null;
		}
		return videoConversion;
	}
	
	/**
	 * The oc workflow may a send a http-notify, this is how it is handled.
	 * 
	 * Upon receiving a success message, the videos are downloaded, added to the database, 
	 * a SMIL file is created for adaptive Streaming and the files are deleted in opencast
	 * 
	 * @param success true if oc has succeeded, false if there was an error
	 */
	public void handleOpencastResponse(Boolean success) {
		logger.info("Opencast has sent a http-notify for videoConversion with sourceId {} with the result: {}", videoConversion.getSourceId(), Boolean.toString(success));
		if (success) {
			// the opencast workflow was successful
			persistVideoConversionStatus(VideoConversionStatus.OC_SUCCEEDED);
			
			// get the event details and map them to createdVideo objects
			List<Video> videos = OpencastApiCall.getVideos(videoConversion.getOpencastId());
			if (videos.isEmpty()) {
				persistVideoConversionStatus(VideoConversionStatus.ERROR_RETRIEVING_VIDEO_METADATA_FROM_OC);
				return;
			}
			List<CreatedVideo> createdVideos = mapMediaToCreatedVideos(videos);
			
			for(CreatedVideo createdVideo: createdVideos) {
				// download to a temporary filename
				downloadVideo(createdVideo);
				GenericDao.getInstance().update(createdVideo);
			}
						
			// reload the videoConversion object to retrieve possible filename changes while downloading
			videoConversion = GenericDao.getInstance().get(VideoConversion.class, videoConversion.getSourceId());
			// reload the createdVideos list
			createdVideos = videoConversion.getCreatedVideos();
			for(CreatedVideo createdVideo: createdVideos) {
				// rename the video file
				renameVideo(createdVideo);
				GenericDao.getInstance().update(createdVideo);
			}
		
			// build SMIL file
			buildSmil(createdVideos);
			
			/*
			// delete event (and files) in opencast
			try {
				OpencastApiCall.deleteEvent(videoConversion.getOpencastId());
			} catch(Exception e) {
				persistVideoConversionStatus(VideoConversionStatus.ERROR_DELETING_FROM_OC);
				return;
			}
			*/
	
			// the process is finished
			persistVideoConversionStatus(VideoConversionStatus.FINISHED);
		} else {
			// the opencast workflow failed
			persistVideoConversionStatus(VideoConversionStatus.ERROR_OC_FAILED);
			/*
			// delete event (and files) in opencast
			try {
				OpencastApiCall.deleteEvent(videoConversion.getOpencastId());
			} catch(Exception e) {
				persistVideoConversionStatus(VideoConversionStatus.ERROR_DELETING_FROM_OC);
				return;
			}
			*/
		}
	}
	
	private void downloadVideos() {
		videoConversion = GenericDao.getInstance().get(VideoConversion.class, videoConversion.getSourceId());
		List<CreatedFile> createdFiles = videoConversion.getCreatedFiles();
		
		for (CreatedFile createdFile: createdFiles) {
			if (createdFile instanceof CreatedVideo) {
				downloadVideo((CreatedVideo) createdFile);
			}
		}
	}
	
	private void renameVideos() {
		// reload the videoConversion object to retrieve possible filename changes before downloading
		videoConversion = GenericDao.getInstance().get(VideoConversion.class, videoConversion.getSourceId());
		List<CreatedFile> createdFiles = videoConversion.getCreatedFiles();
		
		for (CreatedFile createdFile: createdFiles) {
			if (createdFile instanceof CreatedVideo) {
				renameVideo((CreatedVideo) createdFile);
			}
		}
	}

	/**
	 * Renames the file
	 * If the rename happens before any files are created, it will only be set in the database, so the current name can be fetched after downloading
	 * If there are created files, they will be renamed.
	 * @param filename
	 */
	public boolean renameFiles(String filename) {
		logger.info("Renaming Files for videoConversion with sourceId {} to {}", videoConversion.getSourceId(), filename);

		// this is the old filename without extension, which provides the foundation for the renaming
		String oldBaseFilename = FilenameUtils.getBaseName(videoConversion.getSourceFilename());
		
		// the new filename without extension
		String newBaseFilename = FilenameUtils.getBaseName(filename);
		
		// persist the new filename to the database for the given videoConversion id
		videoConversion.setSourceFilename(filename);
		GenericDao.getInstance().update(videoConversion);
		//videoConversion.getCreatedVideos();

		// if there already exist files for the videoConversion we need to rename them
		List<CreatedFile> createdFiles = videoConversion.getCreatedFiles();
		if (!createdFiles.isEmpty()) {			
			for (CreatedFile createdFile: createdFiles) {
				// the old SMIL file will be deleted as it is now outdated
				if (createdFile.getFilename().toLowerCase().endsWith("smil")) {
					logger.info("Delete old SMIL file for videoConversion with sourceId {} and id of createdFile {}", videoConversion.getSourceId(), createdFile.getId());
					// delete file
					FileHandler.deleteIfExists(createdFile.getFilePath());
					// delete from database
					GenericDao.getInstance().deleteById(CreatedFile.class, createdFile.getId());
					// reload owning videoConversion class after delete
					videoConversion = GenericDao.getInstance().get(VideoConversion.class, videoConversion.getSourceId());
				} else {
					String oldFilePath = createdFile.getFilePath();
					String newFilename = createdFile.getFilename().replace(oldBaseFilename, newBaseFilename);
					
					createdFile.setFilename(newFilename);
					GenericDao.getInstance().update(createdFile);
					String newFilePath = createdFile.getFilePath();
					try {
						// delete the old file if somehow existing (and oldfilename differs from newfilename)
						if (newFilePath != oldFilePath) {
							FileHandler.deleteIfExists(newFilePath);
						}
						FileHandler.rename(oldFilePath, newFilePath);
						persistVideoConversionStatus(VideoConversionStatus.RENAMED);
					} catch (IOException e) {
						persistVideoConversionStatus(VideoConversionStatus.ERROR_RENAMING);
						// if one file can not be renamed, stop the renaming process 
						return false;
					}
				}
			}
			// build SMIL file with renamed files
			buildSmil();
			persistVideoConversionStatus(VideoConversionStatus.FINISHED);
		}
		return true;
	}
	

	/**
	 * The current videoConversion object is deleted (including files and database entries)
	 * @return 
	 */
	public boolean delete() {
		logger.info("Delete Everything for videoConversion with sourceId {}", videoConversion.getSourceId());
		// delete event (and files) in opencast
		try {
			OpencastApiCall.deleteEvent(videoConversion.getOpencastId());
		}
		catch(NotFoundException e) {
			// this simply means there is no event at opencast, this is default after a video encoding process is finished
		} catch(WebApplicationException e) {
			persistVideoConversionStatus(VideoConversionStatus.ERROR_DELETING_FROM_OC);
		} 
		return cleanup();
	}

	/**
	 * Deletes all files in the filesystem for the VideoConversion object
	 * @return 
	 */
	private boolean fileCleanup() {
		List<CreatedFile> createdFiles = videoConversion.getCreatedFiles();
		if (createdFiles != null) {
			for(CreatedFile createdFile: createdFiles) {
				try {
					FileHandler.deleteIfExists(createdFile.getFilePath());
				} catch (Exception e) {
					persistVideoConversionStatus(VideoConversionStatus.ERROR_DELETING);
					e.printStackTrace();
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Builds SMIL file for adaptive streaming
	 */
	private void buildSmil() {
		List<CreatedFile> createdFiles = videoConversion.getCreatedFiles();
		List<CreatedVideo> createdVideos = new ArrayList<CreatedVideo>();
		for (CreatedFile createdFile: createdFiles) {
			if (createdFile instanceof CreatedVideo) {
				createdVideos.add((CreatedVideo) createdFile);
			}
		}
		buildSmil(createdVideos);
	}

	/**
	 * Builds SMIL file for adaptive streaming
	 * @param createdVideos
	 */
	private void buildSmil(List<CreatedVideo> createdVideos) {
		logger.info("Build a SMIL file for videoConversion with sourceId {}", videoConversion.getSourceId());
		// the SMIL file will be written to the same folder as the created videos
		String smilFullPath = FilenameUtils.getFullPath(videoConversion.getSourceFilePath());
		String smilFilename = FilenameUtils.getBaseName(videoConversion.getSourceFilename()) + ".smil";
		String smilFilePath = FilenameUtils.concat(smilFullPath, smilFilename);
		// delete smil file is exists
		try {
			FileHandler.deleteIfExists(smilFilePath);
		} catch (SecurityException e) {
			// no permission to delete
			persistVideoConversionStatus(VideoConversionStatus.ERROR_DELETING);
		}
		
		try {
			persistVideoConversionStatus(VideoConversionStatus.CREATING_SMIL);
			SmilBuilder.buildSmil(smilFilePath, createdVideos);
			// persist smil file as a createdFile object to database
			CreatedFile smilFile = new CreatedFile();
			smilFile.setFilePath(smilFilePath);
			smilFile.setVideoConversion(videoConversion);
			GenericDao.getInstance().save(smilFile);
		} catch (ParserConfigurationException | TransformerException e) {
			persistVideoConversionStatus(VideoConversionStatus.ERROR_CREATING_SMIL);
			e.printStackTrace();
		}
	}
	
	/**
	 * Runs the cleanup for the current videoConversion object
	 * @return 
	 */
	private boolean cleanup() {
		return cleanup(videoConversion);
	}
	
	/**
	 * Runs the cleanup (file and database deletion) for a given videoConversion object
	 * @return 
	 */
	private boolean cleanup(VideoConversion videoConversion) {
		// delete all created files from disk
		if (fileCleanup()){
			// delete from database
			GenericDao.getInstance().deleteById(VideoConversion.class, videoConversion.getSourceId());
			return true;
		} else {
			return false;
		}
	}
	

	/**
	 * This maps the resulted object of the events/{id}/media endpoint to the createdVideo object
	 * @param videos the videos from the oc endpoint
	 * @return a list of createdVideo
	 */
	private List<CreatedVideo> mapMediaToCreatedVideos(List<Video> videos) {
		List<CreatedVideo> createdVideos = new ArrayList<CreatedVideo>();
		for(Video video: videos) {
			CreatedVideo createdVideo = new CreatedVideo();
			// set reference to videoConversion object
			createdVideo.setVideoConversion(videoConversion);
			// map
			
			// videoBitrate
			int videoBitrate = video.getStreams().getVideo1().getBitrate().intValue();
			createdVideo.setBitrateVideo(video.getStreams().getVideo1().getBitrate().intValue());

			// there may be videos without sound
			int audioBitrate = 0;
			if (video.getStreams().getAudio1() != null) {
				audioBitrate = video.getStreams().getAudio1().getBitrate().intValue();
			}
			createdVideo.setBitrateAudio(audioBitrate);

			// the overall bitrate result from videoBitrate and audioBitrate
			createdVideo.setBitrate(videoBitrate + audioBitrate);
			
			createdVideo.setWidth(video.getStreams().getVideo1().getFramewidth().intValue());
			createdVideo.setHeight(video.getStreams().getVideo1().getFrameheight().intValue());
			createdVideo.setRemotePath(video.getUri());
			
			// persist created video
			GenericDao.getInstance().save(createdVideo);
			
			// add video to list of videos
			createdVideos.add(createdVideo);
		}
		return createdVideos;
	}

	/**
	 * This maps the resulted object of the events/{id}/publication endpoint to the createdVideo object
	 * @param publication the videos from the oc endpoint
	 * @return a list of createdVideo
	 */
	private List<CreatedVideo> mapPublicationToCreatedVideos(Publication publication) {
		List<CreatedVideo> createdVideos = new ArrayList<CreatedVideo>();
		for(Medium medium: publication.getMedia()) {
			CreatedVideo createdVideo = new CreatedVideo();
			// set reference to videoConversion object
			createdVideo.setVideoConversion(videoConversion);
			// map
			createdVideo.setBitrate(medium.getBitrate());
			createdVideo.setWidth(medium.getWidth());
			createdVideo.setHeight(medium.getHeight());
			createdVideo.setRemotePath(medium.getUrl());
			
			// add video to list of videos
			createdVideos.add(createdVideo);
		}
		return createdVideos;
	}
	
	/**
	 * This downloads a video from opencast to a temporary filename
	 * @param createdVideo the createdVideo which will be downloaded
	 */
	private CreatedVideo downloadVideo(CreatedVideo createdVideo) {
		String sourceFilePath = videoConversion.getSourceFilePath();

		persistVideoConversionStatus(VideoConversionStatus.COPYING_FROM_OC);
		// download the file with a temporary filename to avoid simulanteous writing to the same file  
		String suffix = "_oc_" + String.valueOf(createdVideo.getWidth());
		String targetFilePath = FilenameHandler.addToBasename(sourceFilePath, suffix);
		try {
			OpencastApiCall.downloadFile(createdVideo.getRemotePath(), targetFilePath);
		} catch (IOException e) {
			persistVideoConversionStatus(VideoConversionStatus.ERROR_COPYING_FROM_OC);
			return null;
		}
		
		createdVideo.setFilePath(targetFilePath);
		
		// persist the created video to database
		//GenericDao.getInstance().update(createdVideo);
		return createdVideo;
	}
	
	
	/**
	 * Renames the video 
	 * @param createdVideo
	 */
	private CreatedVideo renameVideo(CreatedVideo createdVideo) {
		// rename the file with an added width, example "originalname_1920.mp4"
		String filePath = FilenameHandler.addToBasename(videoConversion.getSourceFilePath(), "_" + createdVideo.getWidth());
		logger.info("Renaming Video for videoConversion with sourceId {} to {} (path: {})", videoConversion.getSourceId(), createdVideo.getFilename(), createdVideo.getFilePath());

		// rename
		persistVideoConversionStatus(VideoConversionStatus.RENAMING);
		
		try {
			// delete the old video if somehow existing
			FileHandler.deleteIfExists(filePath);
			FileHandler.rename(createdVideo.getFilePath(), filePath);
		} catch (IOException e) {
			persistVideoConversionStatus(VideoConversionStatus.ERROR_RENAMING);

			e.printStackTrace();
			return null;
		}
		createdVideo.setFilePath(filePath);
		
		return createdVideo;
		//CreatedVideo v = GenericDao.getInstance().update(createdVideo);
		
	}
}
