package de.uhh.l2g.webservices.videoprocessor.resources;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.NotFoundException;

import de.uhh.l2g.webservices.videoprocessor.dao.GenericDao;
import de.uhh.l2g.webservices.videoprocessor.filter.LoggingFilter.Logged;
import de.uhh.l2g.webservices.videoprocessor.model.AutoCaption;

/**
 * A autoCaption as a resource identified by its source-id
 *
 * All requests to this resource will be logged
 */
@Logged
//@Secured
public class AutoCaptionResourceBySourceId extends AutoCaptionResource {
	protected Long sourceId;
	
	public AutoCaptionResourceBySourceId(Long sourceId, String tenant) {
		this.tenant = tenant;
		this.sourceId = sourceId;
		// get video conversion by source ID and tenant
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("sourceId", sourceId);
		map.put("tenant", tenant);
		autoCaption = GenericDao.getInstance().getFirstByMultipleFieldsValuesOrderedDesc(AutoCaption.class, map, "startTime");
		if (autoCaption == null) {
            throw new NotFoundException();
		}
	}
}
