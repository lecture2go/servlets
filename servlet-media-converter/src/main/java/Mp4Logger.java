

import java.io.File;

public class Mp4Logger extends Logger {

	//log ffmpeg output
	protected File ffmpegOutputFile;
	//log converter status
	protected File converterOutputFile;
	
    public Mp4Logger(Process process, String path) {
        super.process = process;
        super.path = path;
        ffmpegOutputFile = new File(path+".ffmpeg.mp4.log");
        converterOutputFile = new File(path+".converter.mp4.log");
    }

	@Override
    public void run() {
		//mp4 converter logs
		super.writeLogs(ffmpegOutputFile, converterOutputFile);
		//converting is done (100 percent reached), so you can update the database and delete the source file
		if(super.percentDone==100 && !processIsRunning(process)){
			//finally delete the source file
			new File(path).delete();
			//delete also the log files
			ffmpegOutputFile.delete();
			converterOutputFile.delete();
		}
    }
}
