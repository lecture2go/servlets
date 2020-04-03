package com.hmkcode.vo;

import java.io.InputStream;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties({"content"})
public class FileMeta {
	
	private String openAccess;
	public String getOpenAccess() {
		return openAccess;
	}
	public void setOpenAccess(String openAccess) {
		this.openAccess = openAccess;
	}
	private String generationDate;
	private String containerFormat;
	private String secureFileName;
	private String fileName;
	private long fileSize;
	
	private String fileType;
	private InputStream content;

	public String getGenerationDate() {
		return generationDate;
	}
	public void setGenerationDate(String generationDate) {
		this.generationDate = generationDate;
	}

	public String getContainerFormat() {
		return containerFormat;
	}
	public void setContainerFormat(String containerFormat) {
		this.containerFormat = containerFormat;
	}

	public String getSecureFileName() {
		return secureFileName;
	}
	public void setSecureFileName(String secureFileName) {
		this.secureFileName = secureFileName;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public InputStream getContent(){
		return this.content;
	}
	public void setContent(InputStream content){
		this.content = content;
	}
	@Override
	public String toString() {
		return "FileMeta [fileName=" + fileName + ", fileSize=" + fileSize
				+ ", fileType=" + fileType + "]";
	}
	public String getCurrentFileName() {
		if (openAccess.equals("1")) {
			return fileName;
		} else {
			return secureFileName;
		}
	}
	
}
