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

/*@MappedSuperclass*/
@Entity
/*@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)*/
public class CreatedFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false, nullable = false)
	protected Long id;
	
	protected String filePath;

	protected Date creationTime;

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
