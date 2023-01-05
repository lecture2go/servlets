package de.uhh.l2g.webservices.videoprocessor.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.io.FilenameUtils;

/**
 * The VideoConversion model describes a conversion of a video
 * This will be persisted
 */
@Entity
@Table(name = "autocaption")
public class AutoCaption {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	
	@Column(updatable = false)
	private Long sourceId;
	
	@Column(updatable = false)
	private String tenant;

	private String speech2TextId;

	private String sourceFilePath;

	private String targetDirectory;
	
	@Transient
	private String targetFilePath;
	
	@Transient
	private String filename;

	@Enumerated(EnumType.STRING)
	private AutoCaptionStatus status;
	
	@Column(updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Europe/Berlin")
	private Date startTime;

	private String elapsedTime;
	
	private String language;
	
    @JsonIgnore
    @Transient
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "autoCaption", cascade={CascadeType.REMOVE} )
	@JsonManagedReference
	private List<AutoCaptionHistoryEntry> autoCaptionHistoryEntries;

	/**
	 * Sets the startTime to the current date
	 * This method is called when a videoConversion object is about to be persisted to the database
	 */
	@PrePersist
	protected void onCreate() {
		startTime = new Date();
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
	 * @param tenant the tenant to set
	 */
	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	/**
	 * @return the speech2TextId
	 */
	public String getSpeech2TextId() {
		return speech2TextId;
	}

	/**
	 * @param speech2TextId the speech2TextId to set
	 */
	public void setSpeech2TextId(String speech2TextId) {
		this.speech2TextId = speech2TextId;
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
	public void setTargetDirectory(String targetDirectory) {
		this.targetDirectory = targetDirectory;
	}

	/**
	 * @return the targetFilePath
	 */
	public String getTargetFilePath() {
		return targetFilePath;
	}

	/**
	 * @param targetFilePath the targetFilePath to set
	 */
	public void setTargetFilePath(String targetFilePath) {
		this.targetFilePath = targetFilePath;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return FilenameUtils.getName(sourceFilePath);
	}

	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * @return the status
	 */
	public AutoCaptionStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(AutoCaptionStatus status) {
		this.status = status;
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
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}
	
	/**
	 * @param autoCaptionHistoryEntries the autoCaptionHistoryEntries to add
	 */
	public void addAutoCaptionHistoryEntry(AutoCaptionHistoryEntry historyEntry) {
	    this.autoCaptionHistoryEntries.add(historyEntry);
	}
	
	/**
	 * @param autoCaptionHistoryEntries the autoCaptionHistoryEntries to remove
	 */
	public void removeAutoCaptionHistoryEntry(AutoCaptionHistoryEntry historyEntry) {
	    this.autoCaptionHistoryEntries.remove(historyEntry);
	}
	
	/**
	 * @return the additionalProperties
	 */
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

	/**
	 * @param additionalProperties the additionalProperties to set
	 */
    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
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
}
