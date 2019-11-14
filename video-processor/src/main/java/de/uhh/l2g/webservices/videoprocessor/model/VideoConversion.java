package de.uhh.l2g.webservices.videoprocessor.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import javax.persistence.Table;
import javax.persistence.EnumType;
import javax.persistence.Transient;

import org.apache.commons.io.FilenameUtils;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
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

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	
	@Column(updatable = false)
	private Long sourceId;
	
	@Column(updatable = false)
	private String tenant;

	private String opencastId;

	private String sourceFilePath;
	
	private String targetDirectory;
	
	private String targetThumbnailDirectory;

	@Transient
	private String targetFilePath;
	
	@Transient
	private String filename;

	@Enumerated(EnumType.STRING)
	private VideoConversionStatus status;
	
	@Column(updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Europe/Berlin")
	private Date startTime;

	private String elapsedTime;
	
	private String workflow;
	
    @JsonIgnore
    @Transient
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
	
	private boolean createSmil = false;
	
	// cascade={CascadeType.ALL}
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "videoConversion", cascade={CascadeType.REMOVE} )
	//@OneToMany(fetch = FetchType.EAGER, mappedBy = "videoConversion", orphanRemoval = true)
	@JsonManagedReference
	private Set<CreatedFile> createdFiles;
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "videoConversion", cascade={CascadeType.REMOVE} )
	@JsonManagedReference
	private List<VideoConversionHistoryEntry> videoConversionHistoryEntries;
	
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
	 * @return the tenant
	 */
	public String getTenant() {
		return tenant;
	}

	/**
	 * @param tenant the tanant to set
	 */
	public void setTenant(String tenant) {
		this.tenant = tenant;
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
	 * @return the targetDirectory
	 */
	public String getTargetDirectory() {
		return targetDirectory;
	}

	/**
	 * @param targetDirectory the targetDirectory to set
	 */
	public void setTargetFilePath(String targetFilePath) {
		this.targetDirectory = FilenameUtils.getFullPath(targetFilePath);
	}
	
	/**
	 * @return the getTargetThumbnailDirectory
	 */
	public String getTargetThumbnailDirectory() {
		return targetThumbnailDirectory;
	}

	/**
	 * @param targetThumbnailDirectory the targetThumbnailDirectory to set
	 */
	public void setTargetThumbnailDirectory(String targetThumbnailDirectory) {
		this.targetThumbnailDirectory = targetThumbnailDirectory;
	}
	
	/**
	 * @return the target file path
	 */
	public String getTargetFilePath() {
		return FilenameUtils.concat(targetDirectory, getFilename());
	}


	/**
	 * @param targetDirectory the targetDirectory to set
	 */
	public void setTargetDirectory(String targetDirectory) {
		this.targetDirectory = targetDirectory;
	}


	/**
	 * @return the sourceFileName
	 */
	public String getFilename() {
		return FilenameUtils.getName(sourceFilePath);
	}

	/**
	 * @param sourceFileName the sourceFileName to set
	 */
	public void setFilename(String sourceFilename) {
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
	 * @return the workflow properties
	 */
	/*public HashMap<String, String> getWorkflowProperties() {
		return workflowProperties;
	}*/

	/**
	 * @param workflow the workflow properties to set
	 */
	/*public void setWorkflowProperties(HashMap<String, String> workflowProperties) {
		this.workflowProperties = workflowProperties;
	}*/



	/**
	 * @return the createSmil
	 */
	public boolean getCreateSmil() {
		return createSmil;
	}


	/**
	 * @param createSmil the createSmil to set
	 */
	public void setCreateSmil(boolean createSmil) {
		this.createSmil = createSmil;
	}


	/**
	 * @return the createdFiles
	 */
	public Set<CreatedFile> getCreatedFiles() {
		return createdFiles;
	}

	/**
	 * @param createdFiles the createdFiles to set
	 */
	/*public void setCreatedFiles(List<CreatedFile> createdFiles) {
		this.createdFiles = createdFiles;
	}*/
	
	public void addVideoConversionHistoryEntry(VideoConversionHistoryEntry historyEntry) {
	    this.videoConversionHistoryEntries.add(historyEntry);
	}
	
	
	public void removeVideoConversionHistoryEntry(VideoConversionHistoryEntry historyEntry) {
	    this.createdFiles.remove(historyEntry);
	}
	
	public void addCreatedFiles(Set<CreatedFile> createdFiles) {
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
	
	public void removeCreatedFiles()
	{
	    this.createdFiles.clear();
	}

	/**
	 * Gets the list of createdVideos from the createdFiles 
	 * (this seems necessary due to inheritance of createdVideo and the used persisting in one table -> but maybe there is a better way?!)
	 * @return the createdVideos
	 */
	@JsonIgnore
	public List<CreatedVideo> getCreatedVideos() {
		List<CreatedVideo> createdVideos = new ArrayList<CreatedVideo>();
		Set<CreatedFile> createdFiles = getCreatedFiles();
		for (CreatedFile createdFile: createdFiles) {
			if (createdFile instanceof CreatedVideo) {
				createdVideos.add((CreatedVideo) createdFile);
			}
		}
		return createdVideos;
	}
	
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
