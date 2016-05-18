package de.uhh.l2g.convert;

import java.io.File;

import de.uhh.l2g.beans.Video;
import de.uhh.l2g.dao.VideoDao;
import de.uhh.l2g.util.ProzessManager;

public class Mp3Logger extends Logger {

	//log ffmpeg output
	protected File ffmpegOutputFile;
	//log converter status
	protected File extractorOutputFile;
	
    public Mp3Logger(Process process, String path, Video video, VideoDao videoDao) {
        super.process = process;
        super.path = path;
        super.video = video;
        super.videoDao = videoDao;
        
        ffmpegOutputFile = new File(path+".ffmpeg.mp3.log");
        extractorOutputFile = new File(path+".extractor.mp3.log");
    }

	@Override
    public void run() {
		//mp3 converter logs
		super.writeLogs(ffmpegOutputFile,extractorOutputFile);
		//converting is done (100 percent reached), so you can update the database and delete the source file
		if(super.percentDone==100 && !processIsRunning(process)){
			//delete the log files
			ffmpegOutputFile.delete();
			extractorOutputFile.delete();
			//update RSS for video
			ProzessManager pm = new ProzessManager();
			pm.updateMp3RSS(video, videoDao);
		}else{
			//TODO notify that something goes wrong
		}
    }
}
