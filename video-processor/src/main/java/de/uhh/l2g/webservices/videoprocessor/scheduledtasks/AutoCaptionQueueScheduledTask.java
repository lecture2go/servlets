package de.uhh.l2g.webservices.videoprocessor.scheduledtasks;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import de.uhh.l2g.webservices.videoprocessor.dao.GenericDao;
import de.uhh.l2g.webservices.videoprocessor.model.AutoCaption;
import de.uhh.l2g.webservices.videoprocessor.model.AutoCaptionStatus;
import de.uhh.l2g.webservices.videoprocessor.service.AutoCaptionService;
import de.uhh.l2g.webservices.videoprocessor.service.Subtitle2GoApiCall;

public class AutoCaptionQueueScheduledTask implements Job {
	
	private static final Logger logger = LogManager.getLogger(AutoCaptionQueueScheduledTask.class);

	
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
    	// retrieve queued jobs ordered by startTime oldest first
    	List<AutoCaption> autoCaptions = GenericDao.getInstance().getByFieldValueOrderedAsc(AutoCaption.class, "status", AutoCaptionStatus.S2T_QUEUED, "startTime");
    	if (!autoCaptions.isEmpty()) {
    		logger.info("There are {} queued jobs.", autoCaptions.size());
    		// we have auto captions in queue
    		for (AutoCaption autoCaption: autoCaptions) {
    			if (Subtitle2GoApiCall.takesJob()) {
        			// subtitle2go has free resources start the auto caption
    				AutoCaptionService ac = new AutoCaptionService(autoCaption);
    				ac.runQueuedAutoCaptioning();
        		}
    		}
    	}
    }
}
