package de.uhh.l2g.webservices.videoprocessor.resources;

import de.uhh.l2g.webservices.videoprocessor.dao.GenericDao;
import de.uhh.l2g.webservices.videoprocessor.filter.BasicAuthenticationFilter.Secured;
import de.uhh.l2g.webservices.videoprocessor.filter.LoggingFilter.Logged;
import de.uhh.l2g.webservices.videoprocessor.model.VideoConversion;

/**
 * A videoConversion as a resource identified by its source-id
 *
 * All requests to this resource will be logged
 */
@Logged
@Secured
public class VideoConversionResourceBySourceId extends VideoConversionResource {

	public VideoConversionResourceBySourceId(Long sourceId) {
		id = sourceId;
		videoConversion = GenericDao.getInstance().getFirstByFieldValue(VideoConversion.class, "sourceId",  sourceId);
	}
}
