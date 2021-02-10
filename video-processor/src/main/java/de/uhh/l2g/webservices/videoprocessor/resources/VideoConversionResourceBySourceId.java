package de.uhh.l2g.webservices.videoprocessor.resources;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

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
//@Secured
public class VideoConversionResourceBySourceId extends VideoConversionResource {
	protected Long sourceId;
	
	public VideoConversionResourceBySourceId(Long sourceId, String tenant) {
		this.tenant = tenant;
		this.sourceId = sourceId;
		// get video conversion by source ID and tenant
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("sourceId", sourceId);
		map.put("additionalMediaIdentifier", null);
		map.put("tenant", tenant);
		videoConversion = GenericDao.getInstance().getFirstByMultipleFieldsValuesOrderedDesc(VideoConversion.class, map, "startTime");
		if (videoConversion == null) {
            throw new NotFoundException();
		}
	}
	
    /**
     * Delegates calls to a specific additionalmedia-identifier to the VideoConversionResourceBySourceIdAndAdditionalMediaIdentifier class
     *
     * @param sourceId the specific videoConversion via the source id
     * @return the single videoConversion resource
     */
	@Path("additionalmedia/{identifier}")
	public VideoConversionResourceBySourceIdAndAdditionalMediaIdentifier getVideoConversionBySourceId(@PathParam("identifier") String additionalMediaIdentifier) {
		// the additionalMediaIdentifier is used combined with the sourceId as an identifier
		return new VideoConversionResourceBySourceIdAndAdditionalMediaIdentifier(sourceId, additionalMediaIdentifier, tenant);
	}
	
	
}
