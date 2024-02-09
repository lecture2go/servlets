package de.uhh.l2g.webservices.videoprocessor;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import de.uhh.l2g.webservices.videoprocessor.scheduledtasks.AutoCaptionQueueScheduledTask;

/**
 * Jersey/Jax-RS specific entry point for the webservice
 * (avoid the need of a web.xml file)
 */
@ApplicationPath("/")
public class VideoProcessor extends ResourceConfig {
	
	private int SCHEDULER_INTERVAL_SECONDS = 300; // every 5 minutes
	
	public VideoProcessor() {
		// Register resources and providers using package-scanning.
        packages("de.uhh.l2g.webservices.videoprocessor");
        
        // scheduler
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        try {
			Scheduler scheduler = schedulerFactory.getScheduler();
			
			JobDetail job = JobBuilder.newJob(AutoCaptionQueueScheduledTask.class)
					  .build();
			
			Trigger trigger = TriggerBuilder.newTrigger()
					  .startNow()
					  .withSchedule(SimpleScheduleBuilder.simpleSchedule()
					  .withIntervalInSeconds(SCHEDULER_INTERVAL_SECONDS)
					  .repeatForever())
					  .build();
			
			scheduler.scheduleJob(job, trigger);
			
			scheduler.start();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
}
