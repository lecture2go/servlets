package de.uhh.l2g.webservices.videoprocessor.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.io.FilenameUtils;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * The CreatedFile model describes files which where created in the file system via the videoConversion
 */
@Entity
public class CreatedFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false, nullable = false)
	protected Long id;
	
	protected String filePath;
	
	@Transient
	private String filename;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Europe/Berlin")
	protected Date creationTime;

	@JsonBackReference
	@ManyToOne
	protected VideoConversion videoConversion;
	
	/**
	 * Sets the creationTime to the current date
	 * This method is called when a CreatedFile object is about to be persisted to the database
	 */
	@PrePersist
	protected void onCreate() {
		creationTime = new Date();
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
	 * Only return the filename of the filepath
	 * @return the filename
	 */
	public String getFilename() {
		return FilenameUtils.getName(filePath);
	}

	/**
	 * Setting the filename will reset the filepath with the new filename
	 * @param filename the sourceFileName to set
	 */
	public void setFilename(String filename) {
		String fullPath = FilenameUtils.getFullPath(filePath);
		this.filePath = FilenameUtils.concat(fullPath, filename);
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
