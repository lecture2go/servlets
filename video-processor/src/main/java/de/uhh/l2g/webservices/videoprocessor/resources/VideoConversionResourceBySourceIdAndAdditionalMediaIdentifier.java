package de.uhh.l2g.webservices.videoprocessor.resources;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.NotFoundException;

import de.uhh.l2g.webservices.videoprocessor.dao.GenericDao;
import de.uhh.l2g.webservices.videoprocessor.filter.LoggingFilter.Logged;
import de.uhh.l2g.webservices.videoprocessor.model.VideoConversion;

/**
 * A videoConversion as a resource identified by its source-id and and additional media identifier
 * 
 * This is used to allow multiple video conversion per source-Id identified by an additionalMediaIdentifier
 *
 * All requests to this resource will be logged
 */
@Logged
//@Secured
public class VideoConversionResourceBySourceIdAndAdditionalMediaIdentifier extends VideoConversionResource {

	public VideoConversionResourceBySourceIdAndAdditionalMediaIdentifier(Long sourceId, String additionalMediaIdentifier, String tenant) {
		this.tenant = tenant;
		// get video conversion by source ID, tenant and additionalMediaIdentifier
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("sourceId", sourceId);
		map.put("additionalMediaIdentifier", additionalMediaIdentifier);
		map.put("tenant", tenant);
		videoConversion = GenericDao.getInstance().getFirstByMultipleFieldsValuesOrderedDesc(VideoConversion.class, map, "startTime");
		if (videoConversion == null) {
            throw new NotFoundException();
		}
	}
}
