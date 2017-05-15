package de.uhh.l2g.webservices.videoprocessor.model;

public class CreatedVideo extends CreatedFile {

	private String filePath;

	private int bitrate;

	private int bitrateVideo;

	private int bitrateAudio;

	private int width;

	private int height;

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
