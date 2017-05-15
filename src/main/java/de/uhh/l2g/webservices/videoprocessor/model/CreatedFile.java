package de.uhh.l2g.webservices.videoprocessor.model;

import java.util.Date;

public class CreatedFile {

	private String filePath;

	private Date creationTime;

	private VideoConversion videoConversion;
	
	/**
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * @param filePath the filePath to set
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * @return the creationTime
	 */
	public Date getCreationTime() {
		return creationTime;
	}

	/**
	 * @param creationTime the creationTime to set
	 */
	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	/**
	 * @return the videoConversion
	 */
	public VideoConversion getVideoConversion() {
		return videoConversion;
	}

	/**
	 * @param videoConversion the videoConversion to set
	 */
	public void setVideoConversion(VideoConversion videoConversion) {
		this.videoConversion = videoConversion;
	}
}
