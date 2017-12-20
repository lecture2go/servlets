package de.uhh.l2g.webservices.videoprocessor.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
public class VideoConversionHistoryEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	
	@Column(updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Europe/Berlin")
	private Date time;

	@Enumerated(EnumType.STRING)
	private VideoConversionStatus status;
	
	@JsonBackReference
	@ManyToOne
	private VideoConversion videoConversion;
	

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
	 * @return the time
	 */
	public Date getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(Date time) {
		this.time = time;
	}

	/**
	 * @return the status
	 */
	public VideoConversionStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(VideoConversionStatus status) {
		this.status = status;
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
