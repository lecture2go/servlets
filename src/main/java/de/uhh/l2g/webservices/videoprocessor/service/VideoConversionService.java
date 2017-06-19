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

public class VideoConversionService {
	
	private VideoConversion videoConversion;
	
	public VideoConversionService(VideoConversion videoConversion) {
		this.videoConversion = videoConversion;
	}
	
	public void persistVideoConversionStatus(VideoConversionStatus status) {
		//TODO: this should not be necessary if entity is managed by JPA/Hibernate
		GenericDao.getInstance().get(VideoConversion.class, videoConversion.getSourceId());
		videoConversion.setStatus(status);
		GenericDao.getInstance().update(videoConversion);
	}
	
	public void runVideoConversion() {		
		// save metadata to database (id / path)
		
		// there is no autoincrement id, as the source id is used. a new videoconversion process results in the cleanup 
		// of a current or finished videoconversion with the same id
		VideoConversion videoConversionDb = GenericDao.getInstance().get(VideoConversion.class, videoConversion.getSourceId());
		if (videoConversionDb != null) {
			// delete old files
			fileCleanup();
			GenericDao.getInstance().deleteById(VideoConversion.class, videoConversionDb.getSourceId());
			//genericDao.save(videoConversion);
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
			return;
		} catch(WebApplicationException e) {
			persistVideoConversionStatus(VideoConversionStatus.ERROR_COPYING_TO_OC);
			return;
		}
	}
	
	/**
	 * Deletes all files in the filesystem for an VideoConversion object
	 */
	private void fileCleanup() {
		List<CreatedFile> createdFiles = videoConversion.getCreatedFiles();
		if (createdFiles != null) {
			for(CreatedFile createdFile: createdFiles) {
				try {
					FileHandler.deleteIfExists(createdFile.getFilePath());
				} catch (Exception e) {
					persistVideoConversionStatus(VideoConversionStatus.ERROR_DELETING);
					// TODO: log
					e.printStackTrace();
				}
			}
		}
	}

	public void handleOpencastResponse(Boolean success) {
		// get the corresponding videoconversion object
		GenericDao genericDao = GenericDao.getInstance();
		//VideoConversion videoConversion = genericDao.get(VideoConversion.class, id);
		
		if (success) {
			// the opencast workflow was successful
			
			// update the status of the video conversion
			persistVideoConversionStatus(VideoConversionStatus.OC_SUCCEEDED);
			
			// test without API call:
			/*
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
			*/
			
			// get the event details
			//TODO: remove hardcoded publication channel
			/*
			Publication publication = OpencastApiCall.getPublication(videoConversion.getOpencastId(), "l2go");
			List<CreatedVideo> createdVideos = mapPublicationToCreatedVideos(publication, videoConversion);
			*/
			
			List<Video> videos = OpencastApiCall.getVideos(videoConversion.getOpencastId());
			List<CreatedVideo> createdVideos = mapMediaToCreatedVideos(videos);
			
			for(CreatedVideo createdVideo: createdVideos) {
				downloadVideo(createdVideo);
				renameVideo(createdVideo);
			}
		
			// build SMIL file

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
			
			// delete event (and files) in opencast
			try {
				OpencastApiCall.deleteEvent(videoConversion.getOpencastId());
			} catch(Exception e) {
				persistVideoConversionStatus(VideoConversionStatus.ERROR_DELETING_FROM_OC);
				return;
			}
	
			// the process is finished
			persistVideoConversionStatus(VideoConversionStatus.FINISHED);
		} else {
			// the opencast workflow failed
			
			// delete event (and files) in opencast
			try {
				OpencastApiCall.deleteEvent(videoConversion.getOpencastId());
			} catch(Exception e) {
				persistVideoConversionStatus(VideoConversionStatus.ERROR_DELETING_FROM_OC);
				return;
			}
			
			// update the status of the video conversion
			persistVideoConversionStatus(VideoConversionStatus.ERROR_OC_FAILED);
		}
		
	}

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
			
			// add video to list of videos
			createdVideos.add(createdVideo);
		}
		return createdVideos;
	}

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
	
	private void downloadVideo(CreatedVideo createdVideo) {
		String sourceFilePath = videoConversion.getSourceFilePath();

		persistVideoConversionStatus(VideoConversionStatus.COPYING_FROM_OC);
		// download the file with a temporary filename to avoid simulanteous writing to the same file  
		String suffix = "_oc_" + String.valueOf(createdVideo.getWidth());
		String targetFilePath = FilenameHandler.addToBasename(sourceFilePath, suffix);
		try {
			OpencastApiCall.downloadFile(createdVideo.getRemotePath(), targetFilePath);
		} catch (IOException e) {
			persistVideoConversionStatus(VideoConversionStatus.ERROR_COPYING_FROM_OC);
			return;
		}
		
		createdVideo.setFilePath(targetFilePath);
		
		// persist the created video to database
		GenericDao.getInstance().save(createdVideo);
	}
	
	private void renameVideo(CreatedVideo createdVideo) {
		// while downloading the files there may have been a filename change, reload the object
		videoConversion = GenericDao.getInstance().get(VideoConversion.class, videoConversion.getSourceId());
		// rename the file with an added width, example "originalname_1920.mp4"
		String filePath = FilenameHandler.addToBasename(videoConversion.getSourceFilePath(), "_" + createdVideo.getWidth());
		
		// rename
		persistVideoConversionStatus(VideoConversionStatus.RENAMING);
		
		try {
			FileHandler.rename(createdVideo.getFilePath(), filePath);
		} catch (IOException e) {
			persistVideoConversionStatus(VideoConversionStatus.ERROR_RENAMING);

			e.printStackTrace();
			return;
		}
		createdVideo.setFilePath(filePath);
		GenericDao.getInstance().update(createdVideo);
	}

	public void renameFiles(String filename) {
		//VideoConversion videoConversion = GenericDao.getInstance().get(VideoConversion.class, id);
		
		// this is the old filename without extension, which provides the foundation for the renaming
		String oldBaseFilename = FilenameUtils.getBaseName(videoConversion.getSourceFilename());
		
		// the new filename without extension
		String newBaseFilename = FilenameUtils.getBaseName(filename);

		
		// persist the new filename to the database for the given videoConversion id
		videoConversion.setSourceFilename(filename);
		GenericDao.getInstance().update(videoConversion);
		System.out.println("check");

		// if there already exist files for the videoConversion we need to rename them
		List<CreatedFile> createdFiles = videoConversion.getCreatedFiles();
		if (createdFiles != null) {
			System.out.println("Created file exist");
			for (CreatedFile createdFile: createdFiles) {
				String oldFilePath = createdFile.getFilePath();
				String newFilename = createdFile.getFilename().replace(oldBaseFilename, newBaseFilename);

				createdFile.setFilename(newFilename);
				GenericDao.getInstance().update(createdFile);
				String newFilePath = createdFile.getFilePath();
				try {
					FileHandler.rename(oldFilePath, newFilePath);
					persistVideoConversionStatus(VideoConversionStatus.RENAMED);
				} catch (IOException e) {
					persistVideoConversionStatus(VideoConversionStatus.ERROR_RENAMING);
					// if one file can not be renamed, stop the renaming process 
					break;
				}
			}
		}
	}

	public void delete(Long id) {
		
		// delete all created files from disk
		
		// delete from database
		
		
	}
}
