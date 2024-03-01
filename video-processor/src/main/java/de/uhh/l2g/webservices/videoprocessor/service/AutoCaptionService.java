package de.uhh.l2g.webservices.videoprocessor.service;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uhh.l2g.webservices.videoprocessor.dao.GenericDao;
import de.uhh.l2g.webservices.videoprocessor.model.AutoCaption;
import de.uhh.l2g.webservices.videoprocessor.model.AutoCaptionHistoryEntry;
import de.uhh.l2g.webservices.videoprocessor.model.AutoCaptionStatus;
import de.uhh.l2g.webservices.videoprocessor.util.FileHandler;

public class AutoCaptionService {
	private static final Logger logger = LogManager.getLogger(AutoCaptionService.class);
	private AutoCaption autoCaption;
	
	public AutoCaptionService(AutoCaption autoCaption) {
		this.autoCaption = autoCaption;
	}
	
	/**
	 * This runs the actual auto caption
	 * A new request is sent to the speech2text engine to do the heavy lifting
	 */
	public AutoCaption runAutoCaptioning() {

		logger.info("A new autoCaptioning is started for the sourceId {} and tenant {}", autoCaption.getSourceId(), autoCaption.getTenant());
		logger.info("Additional properties are set {}", autoCaption.getAdditionalProperties().toString());
		
		// delete older auto captionsfor this sourceid and same tenant as current autocaption
		deleteOlderAutoCaptions(false);
		
		// persist a new autocaption object
		autoCaption = GenericDao.getInstance().save(autoCaption);
		
		// copy media file from source to target directory
		persistAutoCaptionStatus(autoCaption, AutoCaptionStatus.COPYING_FILE);
		
		// build filename, for example 34156_en-US.mp4
//		String targetFilename = autoCaption.getSourceId().toString() 
//				+ "_" 
//				+ autoCaption.getLanguage() 
//				+ "." 
//				+ FilenameUtils.getExtension(autoCaption.getSourceFilePath());
		
		// build filename, for example 34156.mp4
		String targetFilename = autoCaption.getSourceId().toString() 
				+ "." 
				+ FilenameUtils.getExtension(autoCaption.getSourceFilePath());
		
		String targetFilePath = FilenameUtils.concat(autoCaption.getTargetDirectory(), targetFilename);
		autoCaption.setTargetFilePath(targetFilePath);
		autoCaption = GenericDao.getInstance().update(autoCaption);
		
		try {
			FileHandler.copy(autoCaption.getSourceFilePath(), autoCaption.getTargetFilePath());
		} catch (IOException e1) {
			persistAutoCaptionStatus(autoCaption, AutoCaptionStatus.ERROR_COPYING_FILE);
			e1.printStackTrace();
			return null;
		}

		logger.info("Check if Speech2Text engine accepts job...");
		// check if subtitle2go takes job
		if (Subtitle2GoApiCall.takesJob()) {
			if (GenericDao.getInstance().getByFieldValue(AutoCaption.class, "status", AutoCaptionStatus.S2T_QUEUED).size()>0) {
				logger.info("Speech2Text engine does currently accept jobs, but other jobs are queued, add autocaption with id: {} / sourceId: {} to queue.", autoCaption.getId(), autoCaption.getSourceId());
				persistAutoCaptionStatus(autoCaption, AutoCaptionStatus.S2T_QUEUED, true);
			} else {
				logger.info("Speech2Text engine accepts jobs, start the automatic captioning process");
				// create a new auto captioning via the speech2text API
				try {
					// post request to speech2text API
					String speech2TextId = Subtitle2GoApiCall.postAutoCaptionRequest(autoCaption.getTargetFilePath(), autoCaption.getId(), autoCaption.getLanguage(), autoCaption.getAdditionalProperties());
					
					// save the speech2text id
					autoCaption.setSpeech2TextId(speech2TextId);
					GenericDao.getInstance().update(autoCaption);
			
					persistAutoCaptionStatus(autoCaption, AutoCaptionStatus.S2T_RUNNING, true);
				} catch(BadRequestException e) {
					persistAutoCaptionStatus(autoCaption, AutoCaptionStatus.ERROR_STARTING_S2T);
					e.printStackTrace();
					return null;
				} catch(WebApplicationException e) {
					persistAutoCaptionStatus(autoCaption, AutoCaptionStatus.ERROR_STARTING_S2T);
					e.printStackTrace();
					return null;
				}
			}
		} else {
			logger.info("Speech2Text engine does currently not accept jobs, add the autocaption with id: {} / sourceId: {} to queue.", autoCaption.getId(), autoCaption.getSourceId());
			// if not set status to QUEUED
			persistAutoCaptionStatus(autoCaption, AutoCaptionStatus.S2T_QUEUED, true);
		}
		
		return autoCaption;
	}
	
	/**
	 * This runs the auto caption for queued jobs
	 * A new request is sent to the speech2text engine to do the heavy lifting
	 */
	public AutoCaption runQueuedAutoCaptioning() {
		logger.info("A queued autoCaptioning is started for the sourceId {} and tenant {}", autoCaption.getSourceId(), autoCaption.getTenant());
		logger.info("Additional properties are set {}", autoCaption.getAdditionalProperties().toString());
		
		// create a new auto captioning via the speech2text API
		try {
			// post request to speech2text API
			String speech2TextId = Subtitle2GoApiCall.postAutoCaptionRequest(autoCaption.getTargetFilePath(), autoCaption.getId(), autoCaption.getLanguage(), autoCaption.getAdditionalProperties());
			
			// save the speech2text id
			autoCaption.setSpeech2TextId(speech2TextId);
			GenericDao.getInstance().update(autoCaption);
	
			persistAutoCaptionStatus(autoCaption, AutoCaptionStatus.S2T_RUNNING, true);
		} catch(BadRequestException e) {
			persistAutoCaptionStatus(autoCaption, AutoCaptionStatus.ERROR_STARTING_S2T);
			e.printStackTrace();
			return null;
		} catch(WebApplicationException e) {
			persistAutoCaptionStatus(autoCaption, AutoCaptionStatus.ERROR_STARTING_S2T);
			e.printStackTrace();
			return null;
		}
		return autoCaption;
	}
	
	/**
	 * The speech-to-text workflow may a send a callback, this is how it is handled.
	 * 
	 * @param success true if autocaption has succeeded, false if there was an error
	 */
	public void handleS2TResponse(Boolean success) {
		logger.info("Speech2Text Engine has sent a message for autoCaption with id: {} / sourceId: {} with the result: {}", autoCaption.getId(), autoCaption.getSourceId(), Boolean.toString(success));
		if (success) {
			// the autocaption was successful
			persistAutoCaptionStatus(autoCaption, AutoCaptionStatus.S2T_SUCCEEDED);
			
			// delete the working copy
			logger.info("Delete the temporary working file {} from id: {} / sourceId: {}.", autoCaption.getTargetFilePath(), autoCaption.getId(), autoCaption.getSourceId());
			FileHandler.deleteIfExists(autoCaption.getTargetFilePath());

			// the process is finished
			// this status change count towards the elapsed time
			persistAutoCaptionStatus(autoCaption, AutoCaptionStatus.FINISHED, true);
		} else {
			// the auto caption failed 
			// this status change count towards the elapsed time
			persistAutoCaptionStatus(autoCaption, AutoCaptionStatus.ERROR_S2T_FAILED, true);
		}
	}
	
	/**
	 * The current autoCaption object is deleted
	 */
	public void delete() {
		logger.info("Delete everything for autocaption with id: {} / sourceId: {} / tenant: {}", autoCaption.getId(), autoCaption.getSourceId(), autoCaption.getTenant());
		
		// stop current subtitle2go process
		try {
			if (autoCaption.getStatus().equals(AutoCaptionStatus.S2T_RUNNING) && autoCaption.getSpeech2TextId() != null) {
				logger.info("There is a subtitle process running, try to stop it. for id: {} / sourceId: {} / tenant: {}", autoCaption.getId(), autoCaption.getSourceId(), autoCaption.getTenant());
				persistAutoCaptionStatus(autoCaption, AutoCaptionStatus.S2T_STOPPING);
				Subtitle2GoApiCall.stopAutoCaptionRequest(autoCaption.getSpeech2TextId());
			}
		}
		catch(WebApplicationException e) {
			persistAutoCaptionStatus(autoCaption, AutoCaptionStatus.ERROR_S2T_STOPPING);
		} 
		
		// delete working copy of file
		FileHandler.deleteIfExists(autoCaption.getTargetFilePath());
		
		persistAutoCaptionStatus(autoCaption, AutoCaptionStatus.DELETED);
	}
	
	
	/**
	 * Persists a given status of a auto caption
	 * @param status the status to persist (as given in the AutoCaptionStatus enum)
	 */
	private void persistAutoCaptionStatus(AutoCaption autoCaption, AutoCaptionStatus status) {
		persistAutoCaptionStatus(autoCaption, status, false);
	}
	
	/**
	 * Persists a given status of a auto captioning and use the current timestamp to calculate the elapsedTime field of the autocaption
	 * @param status the status to persist (as given in the AutoCaptionStatus enum)
	 */
	private void persistAutoCaptionStatus(AutoCaption autoCaption, AutoCaptionStatus status, boolean hasRelevanceForElapsedTime) {
		GenericDao.getInstance().get(AutoCaption.class, autoCaption.getId());
		autoCaption.setStatus(status);
		if (hasRelevanceForElapsedTime) {
			autoCaption.updateElapsedTime();
		}
		// save a history entry for this video conversion
		AutoCaptionHistoryEntry history = new AutoCaptionHistoryEntry();
		history.setStatus(status);
		history.setTime(new Date());
		history.setAutoCaption(autoCaption);
		GenericDao.getInstance().save(history);
		
		GenericDao.getInstance().update(autoCaption);
		logger.info("The new status of the autoCaption with id: {} / source id: {} is {}", autoCaption.getId(), autoCaption.getSourceId(), status);
	}
	
	/**
	 * Deletes alls older auto captions for the same sourceId and tenant as the current autocaption
	 * @param exceptNewest exclude the newest from deleting
	 */
	private void deleteOlderAutoCaptions(boolean exceptNewest) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("sourceId", autoCaption.getSourceId());
		map.put("tenant", autoCaption.getTenant());
		
		List<AutoCaption> autoCaptions = GenericDao.getInstance().getByMultipleFieldsValuesOrderedDesc(AutoCaption.class, map, "startTime");
		// remove the current videoconversion from the list, we don't need to delete anything from this
		if (exceptNewest)
			autoCaptions.remove(0);
		for (AutoCaption olderAutoCaption: autoCaptions) {
			if (!olderAutoCaption.getStatus().equals(AutoCaptionStatus.DELETED)) {
				AutoCaptionService ac = new AutoCaptionService(olderAutoCaption);
				ac.delete();
			}
			
		}
	}
}
