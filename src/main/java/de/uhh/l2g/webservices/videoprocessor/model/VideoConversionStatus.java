package de.uhh.l2g.webservices.videoprocessor.model;

public enum VideoConversionStatus {
	COPYING_TO_OC, 
	OC_RUNNING,
	OC_SUCCEEDED, 
	COPYING_FROM_OC, 
	RENAMING, 
	FINISHED, 
	DELETED, 
	ERROR_COPYING_TO_OC, 
	ERROR_COPYING_TO_OC_BAD_REQUEST,
	ERROR_COPYING_FROM_OC,
	ERROR_OC_FAILED
}