package de.uhh.l2g.convert;

import java.io.File;
import de.uhh.l2g.beans.Video;
import de.uhh.l2g.dao.VideoDao;

public class Mp4Logger extends Logger {

	//log ffmpeg output
	protected File ffmpegOutputFile;
	//log converter status
	protected File converterOutputFile;
	
    public Mp4Logger(Process process, String path, Video video, VideoDao videoDao) {
        super.process = process;
        super.path = path;
        super.video = video;
        super.videoDao = videoDao;
        
        ffmpegOutputFile = new File(path+".ffmpeg.mp4.log");
        converterOutputFile = new File(path+".converter.mp4.log");
    }

	@Override
    public void run() {
		//mp4 converter logs
		super.writeLogs(ffmpegOutputFile,converterOutputFile);
		//converting is done (100 percent reached), so you can update the database and delete the source file
		if(super.percentDone==100 && !processIsRunning(process)){
			Video v = videoDao.getByIdForConvertion(video.getId()).iterator().next();
			//update all new parameters
			//change container format
			v.setContainerFormat("mp4");
			//change file name
			v.setFilename(v.getPreffix()+".mp4");
			//change secure file name
			v.setSecureFilename(v.getSPreffix()+".mp4");
			//update database
			videoDao.updateForConverting(v.getId(), v.getFilename(), v.getSecureFilename(), v.getContainerFormat(), new File(path.split("\\.")[path.split("\\.").length-2]+".mp4").length());		
			//finally delete the source file
			new File(path).delete();
			//delete also the log files
			ffmpegOutputFile.delete();
			converterOutputFile.delete();
		}
    }
}
