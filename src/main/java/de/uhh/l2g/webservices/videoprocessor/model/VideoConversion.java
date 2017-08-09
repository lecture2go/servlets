package de.uhh.l2g.webservices.videoprocessor.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;


/**
 * The VideoConversion model describes a conversion of a video
 * This will be persisted
 */
@Entity
@Table(name = "videoconversion")
public class VideoConversion {

	/* no autoincrement id is used anymore, instead the sourceId is used as the identifier */
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
	
	private String workflow;
	
	// cascade={CascadeType.ALL}
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "videoConversion", cascade={CascadeType.REMOVE} )
	@JsonManagedReference
	private List<CreatedFile> createdFiles;
	
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
	 */
	public void updateElapsedTime() {
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
	 * @return the workflow
	 */
	public String getWorkflow() {
		return workflow;
	}


	/**
	 * @param workflow the workflow to set
	 */
	public void setWorkflow(String workflow) {
		this.workflow = workflow;
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
	/*public void setCreatedFiles(List<CreatedFile> createdFiles) {
		this.createdFiles = createdFiles;
	}*/
	
	public void addCreatedFiles(List<CreatedFile> createdFiles) {
	    this.createdFiles.addAll(createdFiles);
	}
	
	public void addCreatedFile(CreatedFile createdFile)
	{
	    this.createdFiles.add(createdFile);
	}
	
	public void removeCreatedFile(CreatedFile createdFile)
	{
	    this.createdFiles.remove(createdFile);
	}

	/**
	 * Gets the list of createdVideos from the createdFiles 
	 * (this seems necessary due to inheritance of createdVideo and the used persisting in one table -> but maybe there is a better way?!)
	 * @return the createdVideos
	 */
	@JsonIgnore
	public List<CreatedVideo> getCreatedVideos() {
		List<CreatedVideo> createdVideos = new ArrayList<CreatedVideo>();
		List<CreatedFile> createdFiles = getCreatedFiles();
		for (CreatedFile createdFile: createdFiles) {
			if (createdFile instanceof CreatedVideo) {
				createdVideos.add((CreatedVideo) createdFile);
			}
		}
		return createdVideos;
	}
}
