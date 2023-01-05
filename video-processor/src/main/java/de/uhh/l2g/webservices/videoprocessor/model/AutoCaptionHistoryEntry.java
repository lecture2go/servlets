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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
public class AutoCaptionHistoryEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	
	@Column(updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Europe/Berlin")
	private Date time;

	@Enumerated(EnumType.STRING)
	private AutoCaptionStatus status;
	
	@JsonBackReference
	@ManyToOne
	private AutoCaption autoCaption;
	

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
	 * @return the autoCaption
	 */
	public AutoCaption getautoCaption() {
		return autoCaption;
	}

	/**
	 * @param autoCaption the autoCaption to set
	 */
	public void setAutoCaption(AutoCaption autoCaption) {
		this.autoCaption = autoCaption;
	}
}
