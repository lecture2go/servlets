


public class VideoThread extends Thread {




	public String getVideoRepository() {
		return videoRepository;
	}

	public void setVideoRepository(String videoRepository) {
		this.videoRepository = videoRepository;
	}

    protected String videoRepository;
	public String ffmpegBin = "";
	
	
	public VideoThread(){}
	
	public VideoThread(String videoRepository, String ffmpegBin){
    	this.videoRepository = videoRepository;   
    	this.ffmpegBin = ffmpegBin;  
    }
}
