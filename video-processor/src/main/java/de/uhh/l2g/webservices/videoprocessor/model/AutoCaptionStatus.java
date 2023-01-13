package de.uhh.l2g.webservices.videoprocessor.model;

/*
 * An Enumeration of possible statuses of the autocaption 
 *
 */
public enum AutoCaptionStatus {
	S2T_RUNNING,
	S2T_SUCCEEDED, 
	COPYING_FILE, 
	FINISHED, 
	ERROR_STARTING_S2T, 
	ERROR_COPYING_FILE,
	ERROR_S2T_FAILED, 
}
