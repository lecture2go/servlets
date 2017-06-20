package de.uhh.l2g.webservices.videoprocessor.model;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.EnumType;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.persistence.TemporalType;

import org.apache.commons.io.FilenameUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;


@Entity
@Table(name = "videoconversion")
public class VideoConversion {

	//@Id
	//@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@Column(name = "id", updatable = false, nullable = false)
	//private Long id;
	
	@Id
	@Column(updatable = false)
	private Long sourceId;

	private String opencastId;

	private String sourceFilePath;
	
	@Transient
	private String sourceFilename;

	@Enumerated(EnumType.STRING)
	private VideoConversionStatus status;
	
	@Column(updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Europe/Berlin")
	private Date startTime;

	private String elapsedTime;
	
	@OneToMany(mappedBy = "videoConversion", cascade={CascadeType.ALL})
	@JsonManagedReference
	private List<CreatedFile> createdFiles;
	
	//private CreatedFile[] createdFiles;

	/**
	 * Sets the startTime to the current date
	 * This method is called when a videoConversion object is about to be persisted to the database
	 */
	@PrePersist
	protected void onCreate() {
		startTime = new Date();
	}
	
	/**
	 * Sets the elapsed time since the start date in a easily readable string: "hh:mm:ss"
	 * This method is called when a videoConversion object is about to be updated in the database
	 */
	@PreUpdate
	protected void onUpdate() {
		Date now = new Date();
		Long milliSeconds = now.getTime() - startTime.getTime();
		// see here: http://stackoverflow.com/questions/9027317/how-to-convert-milliseconds-to-hhmmss-format
		elapsedTime = String.format("%02d:%02d:%02d", 
			    TimeUnit.MILLISECONDS.toHours(milliSeconds),
			    TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - 
			    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
			    TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - 
			    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
	}
	
	/**
	 * @return the id
	 */
	/*public Long getId() {
		return id;
	}*/

	/**
	 * @param id the id to set
	 */
	/*public void setId(Long id) {
		this.id = id;
	}*/
	
	/**
	 * @return the sourceId
	 */
	public Long getSourceId() {
		return sourceId;
	}

	/**
	 * @param sourceId the sourceId to set
	 */
	public void setSourceId(Long sourceId) {
		this.sourceId = sourceId;
	}

	/**
	 * @return the opencastId
	 */
	public String getOpencastId() {
		return opencastId;
	}

	/**
	 * @param opencastId the opencastId to set
	 */
	public void setOpencastId(String opencastId) {
		this.opencastId = opencastId;
	}

	/**
	 * @return the sourceFilePath
	 */
	public String getSourceFilePath() {
		return sourceFilePath;
	}

	/**
	 * @param sourceFilePath the sourceFilePath to set
	 */
	public void setSourceFilePath(String sourceFilePath) {
		this.sourceFilePath = sourceFilePath;
	}

	/**
	 * @return the sourceFileName
	 */
	public String getSourceFilename() {
		return FilenameUtils.getName(sourceFilePath);
	}

	/**
	 * @param sourceFileName the sourceFileName to set
	 */
	public void setSourceFilename(String sourceFilename) {
		String fullPath = FilenameUtils.getFullPath(sourceFilePath);
		this.sourceFilePath = FilenameUtils.concat(fullPath, sourceFilename);
	}

	/**
	 * @return the status
	 */
	public VideoConversionStatus getStatus() {
		return status;
	}

	/**
	 * @param ocRunning the status to set
	 */
	public void setStatus(VideoConversionStatus ocRunning) {
		this.status = ocRunning;
	}

	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the elapsedTime
	 */
	public String getElapsedTime() {
		return elapsedTime;
	}

	/**
	 * @param elapsedTime the elapsedTime to set
	 */
	public void setElapsedTime(String elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	/**
	 * @return the createdFiles
	 */
	public List<CreatedFile> getCreatedFiles() {
		return createdFiles;
	}

	/**
	 * @param createdFiles the createdFiles to set
	 */
	public void setCreatedFiles(List<CreatedFile> createdFiles) {
		this.createdFiles = createdFiles;
	}
}
