package de.uhh.l2g.webservices.videoprocessor.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
import de.uhh.l2g.webservices.videoprocessor.model.VideoConversionHistoryEntry;
import de.uhh.l2g.webservices.videoprocessor.model.VideoConversionStatus;
import de.uhh.l2g.webservices.videoprocessor.model.opencast.Medium;
import de.uhh.l2g.webservices.videoprocessor.service.OpencastApiCall;
import de.uhh.l2g.webservices.videoprocessor.util.Config;
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
	 * This runs the actual video conversion
	 * A new event request with the file is sent to opencast to do the heavy lifting
	 */
	public VideoConversion runVideoConversion() {		

		logger.info("A new videoConversion is started for the sourceId {}", videoConversion.getSourceId());
		
		// delete the last videoconversion with this sourceId
		VideoConversion videoConversionDb = GenericDao.getInstance().getFirstByFieldValueOrderedDesc(VideoConversion.class, "sourceId", videoConversion.getSourceId(), "startTime");
		if (videoConversionDb != null) {
			// the last video conversion for this source id might still be running, delete it
			OpencastApiCall.deleteEvent(videoConversionDb.getOpencastId());
			// delete old files
			cleanup(videoConversionDb);
		}
		
		// persist a new videoConversion object
		videoConversion = GenericDao.getInstance().save(videoConversion);
		
		persistVideoConversionStatus(videoConversion, VideoConversionStatus.COPYING_TO_OC);
		
		// if no workflow is given use the default workflow
		if (videoConversion.getWorkflow() == null) {
			videoConversion.setWorkflow(Config.getInstance().getProperty("opencast.conversion.workflow"));
			GenericDao.getInstance().update(videoConversion);
		}
		
		// create a new opencast event via the opencast API
		try {
			// post event to opencast, this may take some time as the original video is transfered with this call
			String opencastId = OpencastApiCall.postNewEventRequest(videoConversion.getSourceFilePath(), videoConversion.getFilename(), videoConversion.getId(), videoConversion.getWorkflow());
			
			// reload the videoConversion object to retrieve possible changes (filename or even deletion) while copying to opencast
			videoConversion = GenericDao.getInstance().get(VideoConversion.class, videoConversion.getId());
			
			// save the opencast id
			videoConversion.setOpencastId(opencastId);
			GenericDao.getInstance().update(videoConversion);
			
			// check if original video was deleted in the meantime, if so stop the processing
			if (videoConversion.getStatus() == VideoConversionStatus.DELETED) {
				logger.info("The original video of the videoConversion with sourceId {} was deleted in the meantime", videoConversion.getSourceId());
				delete();
			} else {
				// this status change count towards the elapsed time
				persistVideoConversionStatus(videoConversion, VideoConversionStatus.OC_RUNNING, true);
			}
		} catch(BadRequestException e) {
			persistVideoConversionStatus(videoConversion, VideoConversionStatus.ERROR_COPYING_TO_OC_BAD_REQUEST);
			return null;
		} catch(WebApplicationException e) {
			persistVideoConversionStatus(videoConversion, VideoConversionStatus.ERROR_COPYING_TO_OC);
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
		logger.info("Opencast has sent a http-notify for videoConversion with id: {} / sourceId: {} with the result: {}", videoConversion.getId(), videoConversion.getSourceId(), Boolean.toString(success));
		if (success) {
			// the opencast workflow was successful
			persistVideoConversionStatus(videoConversion, VideoConversionStatus.OC_SUCCEEDED);
			
			// get the event details and map them to createdVideo objects
			List<Medium> media = OpencastApiCall.getMedia(videoConversion.getOpencastId());
			if (media.isEmpty()) {
				// this status change count towards the elapsed time
				persistVideoConversionStatus(videoConversion, VideoConversionStatus.ERROR_RETRIEVING_VIDEO_METADATA_FROM_OC, true);
				return;
			}
		
			// the fetched data is mapped to the model (CreatedFile or its children (CreatedVideo))
			List<CreatedFile> createdFiles = mapMediaToCreatedFiles(media);

			// download files
			for(CreatedFile createdFile: createdFiles) {
				// download to a temporary filename first
				downloadFile(createdFile);
				GenericDao.getInstance().update(createdFile);
			}
			
			// reload the videoConversion object to retrieve possible filename changes while downloading
			videoConversion = GenericDao.getInstance().get(VideoConversion.class, videoConversion.getId());
			// as getCreatedFiles returns a set covert it to a list
			createdFiles = new ArrayList<CreatedFile>(videoConversion.getCreatedFiles());
			
			for(CreatedFile createdFile: createdFiles) {
				// rename the file to the final finalname
				createdFile = renameFilesToFinalFilename(createdFile);
				GenericDao.getInstance().update(createdFile);
			}
			
			if (videoConversion.getCreateSmil()) {
				// only videos are relevant for the smil file, check list to only use 
				List<CreatedVideo> createdVideos = new ArrayList<CreatedVideo>();
				for(CreatedFile createdFile: createdFiles) {
					if (createdFile instanceof CreatedVideo) {
						createdVideos.add((CreatedVideo) createdFile);
					}
				}
				// build SMIL file
				buildSmil(createdVideos);
			}
			
			// delete event (and files) in opencast
			try {
				OpencastApiCall.deleteEvent(videoConversion.getOpencastId());
			} catch(Exception e) {
				persistVideoConversionStatus(videoConversion, VideoConversionStatus.ERROR_DELETING_FROM_OC);
				return;
			}
	
			// the process is finished
			// this status change count towards the elapsed time
			persistVideoConversionStatus(videoConversion, VideoConversionStatus.FINISHED, true);
		} else {
			// the opencast workflow failed
			// this status change count towards the elapsed time
			persistVideoConversionStatus(videoConversion, VideoConversionStatus.ERROR_OC_FAILED, true);
		}
	}

	/**
	 * Renames the file
	 * If the rename happens before any files are created, it will only be set in the database, so the current name can be fetched after downloading
	 * If there are created files, they will be renamed.
	 * @param filename
	 */
	public boolean handleRenameRequest(String filename) {
		logger.info("Renaming Files for videoConversion with id: {} / sourceId: {} to {}", videoConversion.getId(), videoConversion.getSourceId(), filename);

		// this is the old filename without extension, which provides the foundation for the renaming
		String oldBaseFilename = FilenameUtils.getBaseName(videoConversion.getFilename());
		
		// the new filename without extension
		String newBaseFilename = FilenameUtils.getBaseName(filename);
		
		// persist the new filename to the database for the given videoConversion id
		videoConversion.setFilename(filename);
		GenericDao.getInstance().update(videoConversion);
		//videoConversion.getCreatedVideos();

		// if there already exist files for the videoConversion we need to rename them
		Set<CreatedFile> createdFiles = videoConversion.getCreatedFiles();
		// keep track of the created video files, as they are used for the smil creation
		List<CreatedVideo> createdVideos = new ArrayList<CreatedVideo>();
		if (!createdFiles.isEmpty()) {			
			for (CreatedFile createdFile: createdFiles) {
				// the old SMIL file will be deleted as it is now outdated
				if (createdFile.getFilename().toLowerCase().endsWith("smil")) {
					logger.info("Delete old SMIL file for videoConversion with id: {} / sourceId: {} and id of createdFile {}", videoConversion.getId(), videoConversion.getSourceId(), createdFile.getId());
					// delete file
					FileHandler.deleteIfExists(createdFile.getFilePath());
					// delete from database
					GenericDao.getInstance().deleteById(CreatedFile.class, createdFile.getId());
					// reload owning videoConversion class after delete
					videoConversion = GenericDao.getInstance().get(VideoConversion.class, videoConversion.getId());
				} else {
					String oldFilePath = createdFile.getFilePath();
					String newFilename = createdFile.getFilename().replace(oldBaseFilename, newBaseFilename);
					
					createdFile.setFilename(newFilename);
					GenericDao.getInstance().update(createdFile);
					if (createdFile instanceof CreatedVideo) {
						createdVideos.add((CreatedVideo) createdFile);
					}
					String newFilePath = createdFile.getFilePath();
					try {
						// there may be cases where other services have already renamed the file (e.g. l2go handles the audio-file), check for those cases
						if(FileHandler.checkIfFileExists(oldFilePath)) {
							// the file exists, rename it!
							FileHandler.rename(oldFilePath, newFilePath);
							persistVideoConversionStatus(videoConversion, VideoConversionStatus.RENAMED);
						}
					} catch (IOException e) {
						persistVideoConversionStatus(videoConversion, VideoConversionStatus.ERROR_RENAMING);
						// if one file can not be renamed, stop the renaming process 
						return false;
					}
				}
			}
			
			if (videoConversion.getCreateSmil()) {
				// build SMIL file with renamed files
				buildSmil(createdVideos);
			}
			persistVideoConversionStatus(videoConversion, VideoConversionStatus.FINISHED);
		}
		return true;
	}
	

	/**
	 * The current videoConversion object is deleted (including files and database entries)
	 * @return 
	 */
	public boolean delete() {
		logger.info("Delete Everything for videoConversion with id: {} / sourceId: {}", videoConversion.getId(), videoConversion.getSourceId());
		// delete event (and files) in opencast
		try {
			OpencastApiCall.deleteEvent(videoConversion.getOpencastId());
		}
		catch(NotFoundException e) {
			// this simply means there is no event at opencast, this is default after a video encoding process is finished
		} catch(WebApplicationException e) {
			persistVideoConversionStatus(videoConversion, VideoConversionStatus.ERROR_DELETING_FROM_OC);
		} 
		return cleanup();
	}

	/**
	 * Deletes all files in the filesystem for the VideoConversion object
	 * @param videoConversion 
	 * @return 
	 */
	private boolean fileCleanup(VideoConversion videoConversion) {
		Set<CreatedFile> createdFiles = videoConversion.getCreatedFiles();
		if (createdFiles != null) {
			for(CreatedFile createdFile: createdFiles) {
				try {
					FileHandler.deleteIfExists(createdFile.getFilePath());
				} catch (Exception e) {
					persistVideoConversionStatus(videoConversion, VideoConversionStatus.ERROR_DELETING);
					e.printStackTrace();
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Builds SMIL file for adaptive streaming
	 * @param createdVideos
	 */
	private void buildSmil(List<CreatedVideo> createdVideos) {
		logger.info("Build a SMIL file for videoConversion with id: {} / sourceId: {}", videoConversion.getId(), videoConversion.getSourceId());
		// the SMIL file will be written to the same folder as the created videos
		//String smilFullPath = FilenameUtils.getFullPath(videoConversion.getSourceFilePath());
		String smilFullPath = videoConversion.getTargetDirectory();

		String smilFilename = FilenameUtils.getBaseName(videoConversion.getFilename()) + ".smil";
		String smilFilePath = FilenameUtils.concat(smilFullPath, smilFilename);
		// delete smil file is exists
		try {
			FileHandler.deleteIfExists(smilFilePath);
		} catch (SecurityException e) {
			// no permission to delete
			persistVideoConversionStatus(videoConversion, VideoConversionStatus.ERROR_DELETING);
		}
		
		try {
			persistVideoConversionStatus(videoConversion, VideoConversionStatus.CREATING_SMIL);
			SmilBuilder.buildSmil(smilFilePath, createdVideos);
			// persist smil file as a createdFile object to database
			CreatedFile smilFile = new CreatedFile();
			smilFile.setFilePath(smilFilePath);
			smilFile.setVideoConversion(videoConversion);
			GenericDao.getInstance().save(smilFile);
		} catch (ParserConfigurationException | TransformerException e) {
			persistVideoConversionStatus(videoConversion, VideoConversionStatus.ERROR_CREATING_SMIL);
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
		if (fileCleanup(videoConversion)){
			// delete all created files for the current videoconversion from database 
			GenericDao.getInstance().deleteByFieldValue(CreatedFile.class, "videoConversion", videoConversion.getId());
			// refresh the videoConversion object
			videoConversion = GenericDao.getInstance().get(VideoConversion.class, videoConversion.getId());
			persistVideoConversionStatus(videoConversion, VideoConversionStatus.DELETED);
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
	/*
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
	*/
	
	/**
	 * This maps the resulted object of the events/{id}/media endpoint to the createdVideo object
	 * @param media the media from the oc endpoint
	 * @return a list of createdVideo
	 */
	private List<CreatedFile> mapMediaToCreatedFiles(List<Medium> media) {
		List<CreatedFile> createdFiles = new ArrayList<CreatedFile>();
		for(Medium video: media) {
			// map
			if (video.getIdentifier().toLowerCase().startsWith("video")) {
				CreatedVideo createdVideo = new CreatedVideo();
				// set reference to videoConversion object
				createdVideo.setVideoConversion(videoConversion);
				// its a video, map all relevant data
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
				createdFiles.add(createdVideo);
			} else {
				CreatedFile createdFile = new CreatedFile();
				// set reference to videoConversion object
				createdFile.setVideoConversion(videoConversion);
				// map
				createdFile.setRemotePath(video.getUri());
				
				// persist created video
				GenericDao.getInstance().save(createdFile);
				
				// add video to list of videos
				createdFiles.add(createdFile);
			}

		}
		return createdFiles;
	}
	
	/**
	 * This maps the resulted object of the events/{id}/publication endpoint to the createdVideo object
	 * @param publication the videos from the oc endpoint
	 * @return a list of createdVideo
	 */
	/*
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
	*/
	
	/**
	 * This downloads a video from opencast to a temporary filename
	 * @param createdVideo the createdVideo which will be downloaded
	 */
	/*private CreatedVideo downloadVideo(CreatedVideo createdVideo) {
		return downloadVideo(createdVideo, null);
	}*/
	
	
	/**
	 * This downloads a file from opencast to a temporary filename
	 * @param createdFile the createdFile which will be downloaded
	 */
	private CreatedFile downloadFile(CreatedFile createdFile) {
		String sourceFilePath = videoConversion.getSourceFilePath();

		persistVideoConversionStatus(videoConversion, VideoConversionStatus.COPYING_FROM_OC);

		// if no target-directory is specified, save the source-path as the target-directory
		String targetFilePath;
		if (videoConversion.getTargetDirectory() == null) {
			videoConversion.setTargetDirectory(FilenameUtils.getFullPath(sourceFilePath));
			GenericDao.getInstance().update(videoConversion);
		}
		
		// the full path where the file will be written (will be modified in the next steps)
		targetFilePath = videoConversion.getTargetFilePath();
		
		// replace with original extension
		String remoteFileExtension = FilenameUtils.getExtension(createdFile.getRemotePath());
		targetFilePath = FilenameHandler.switchExtension(targetFilePath, remoteFileExtension);
		
		// download the file with a temporary filename to avoid simultaneous writing to the same file
		targetFilePath = getTemporaryTargetFilePath(targetFilePath);
		try {
			OpencastApiCall.downloadFile(createdFile.getRemotePath(), targetFilePath);
		} catch (IOException e) {
			persistVideoConversionStatus(videoConversion, VideoConversionStatus.ERROR_COPYING_FROM_OC);
			return null;
		}
		createdFile.setFilePath(targetFilePath);
		
		return createdFile;
	}
	
	/**
	 * Returns a temporary target file path
	 * @param targetFilePath
	 * @return
	 */
	private String getTemporaryTargetFilePath(String targetFilePath) {
		String suffix = "_" + UUID.randomUUID().toString();
		return FilenameHandler.addToBasename(targetFilePath, suffix);
	}
	
	/**
	 * Renames the file in the file system and database
	 * @param createdFile
	 */
	private CreatedFile renameFilesToFinalFilename(CreatedFile createdFile) {
		// check if file is a video
		String targetFilePath;
		if (createdFile instanceof CreatedVideo) {
			targetFilePath = FilenameHandler.addToBasename(videoConversion.getTargetFilePath(), "_" + ((CreatedVideo) createdFile).getWidth());
		} else {
			targetFilePath = videoConversion.getTargetFilePath();
			targetFilePath = FilenameHandler.switchExtension(targetFilePath, FilenameUtils.getExtension(createdFile.getFilename()));
		}
		logger.info("Renaming File for videoConversion with sourceId {} from {} to {} (path: {})", videoConversion.getSourceId(), createdFile.getFilename(), FilenameUtils.getName(targetFilePath), targetFilePath);

		// rename
		persistVideoConversionStatus(videoConversion, VideoConversionStatus.RENAMING);
		
		try {
			// delete a file if it has the same name as the targetfilepath
			FileHandler.deleteIfExists(targetFilePath);
			FileHandler.rename(createdFile.getFilePath(), targetFilePath);
		} catch (IOException e) {
			persistVideoConversionStatus(videoConversion, VideoConversionStatus.ERROR_RENAMING);

			e.printStackTrace();
			return null;
		}
		createdFile.setFilePath(targetFilePath);
		
		return createdFile;
	}
		
	
	/**
	 * Persists a given status of a video conversion
	 * @param status the status to persist (as given in the VideoConversionStatus enum)
	 */
	private void persistVideoConversionStatus(VideoConversion videoConversion, VideoConversionStatus status) {
		persistVideoConversionStatus(videoConversion, status, false);
	}
	
	/**
	 * Persists a given status of a video conversion and use the current timestamp to calculate the elapsedTime field of the videoconversion
	 * @param status the status to persist (as given in the VideoConversionStatus enum)
	 */
	private void persistVideoConversionStatus(VideoConversion videoConversion, VideoConversionStatus status, boolean hasRelevanceForElapsedTime) {
		//TODO: this should not be necessary if entity is managed by JPA/Hibernate
		GenericDao.getInstance().get(VideoConversion.class, videoConversion.getId());
		videoConversion.setStatus(status);
		if (hasRelevanceForElapsedTime) {
			videoConversion.updateElapsedTime();
		}
		// save a history entry for this video conversion
		VideoConversionHistoryEntry history = new VideoConversionHistoryEntry();
		history.setStatus(status);
		history.setTime(new Date());
		history.setVideoConversion(videoConversion);
		GenericDao.getInstance().save(history);
		
		GenericDao.getInstance().update(videoConversion);
		logger.info("The new status of the videoConversion with id: {} / source id: {} is {}", videoConversion.getId(), videoConversion.getSourceId(), status);
	}
}
