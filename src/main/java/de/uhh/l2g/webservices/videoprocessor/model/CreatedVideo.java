package de.uhh.l2g.webservices.videoprocessor.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.io.FilenameUtils;

@Entity
@Table(name = "createdvideo")
public class CreatedVideo extends CreatedFile {


	
	//private String filePath;

	@Transient
	private String filename;

	private int bitrateVideo = 0;

	private int bitrateAudio = 0;

	private int width;

	private int height;

	private int bitrate;
	

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the filePath
	 */
	/*public String getFilePath() {
		return filePath;
	}*/

	/**
	 * @param filePath the filePath to set
	 */
	/*public void setFilePath(String filePath) {
		this.filePath = filePath;
	}*/
	

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return FilenameUtils.getName(filePath);
	}

	/**
	 * @param filename the sourceFileName to set
	 */
	public void setFilename(String filename) {
		String fullPath = FilenameUtils.getFullPath(filePath);
		this.filePath = FilenameUtils.concat(fullPath, filename);
	}

	/**
	 * @return the bitrate
	 */
	public int getBitrate() {
		return bitrate;
	}

	/**
	 * @param bitrate the bitrate to set
	 */
	public void setBitrate(int bitrate) {
		this.bitrate = bitrate;
	}

	/**
	 * @return the bitrateVideo
	 */
	public int getBitrateVideo() {
		return bitrateVideo;
	}

	/**
	 * @param bitrateVideo the bitrateVideo to set
	 */
	public void setBitrateVideo(int bitrateVideo) {
		this.bitrateVideo = bitrateVideo;
	}

	/**
	 * @return the bitrateAudio
	 */
	public int getBitrateAudio() {
		return bitrateAudio;
	}

	/**
	 * @param bitrateAudio the bitrateAudio to set
	 */
	public void setBitrateAudio(int bitrateAudio) {
		this.bitrateAudio = bitrateAudio;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	
}
