package de.uhh.l2g.convert;

import de.uhh.l2g.dao.HostDao;
import de.uhh.l2g.dao.ProducerDao;
import de.uhh.l2g.dao.VideoDao;

public class VideoThread extends Thread {

	protected VideoDao videoDao;
    protected ProducerDao producerDao;
    public ProducerDao getProducerDao() {
		return producerDao;
	}

	public void setProducerDao(ProducerDao producerDao) {
		this.producerDao = producerDao;
	}

	public HostDao getHostDao() {
		return hostDao;
	}

	public void setHostDao(HostDao hostDao) {
		this.hostDao = hostDao;
	}

	public String getVideoRepository() {
		return videoRepository;
	}

	public void setVideoRepository(String videoRepository) {
		this.videoRepository = videoRepository;
	}

	public void setVideoDao(VideoDao videoDao) {
		this.videoDao = videoDao;
	}

	protected HostDao hostDao;
    protected String videoRepository;
	public String ffmpegBin = "";
	
    public VideoDao getVideoDao() {
		return videoDao;
	}

	public void setVideoDao(VideoDao videoDao, ProducerDao producerDao, HostDao hostDao, String videoRepository) {
		this.videoDao = videoDao;
	}
	
	public VideoThread(){}
	
	public VideoThread(VideoDao videoDao, ProducerDao producerDao, HostDao hostDao, String videoRepository, String ffmpegBin){
    	this.videoDao=videoDao;
    	this.producerDao = producerDao;
    	this.hostDao = hostDao;
    	this.videoRepository = videoRepository;   
    	this.ffmpegBin = ffmpegBin;  
    }
}
