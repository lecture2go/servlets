package de.uhh.l2g.convert;

import java.io.IOException;

import de.uhh.l2g.beans.Video;
import de.uhh.l2g.dao.VideoDao;

public class Converter extends ConverterManager{
	private static final long serialVersionUID = 1L;

	public Converter(){}
	
	@SuppressWarnings("unchecked")
	public void convertFileToMp4 (String path, Video video, VideoDao videoDao) throws IOException, InterruptedException {
		String containerFormat = path.split("\\.")[path.split("\\.").length-1];	
		String aPath = path.split(containerFormat)[0] + "mp4"; 
		
		String command = ffmpegBin +" -i "+path +" "+ ffmpegConvertFileToMp4CommandParameter +" "+ aPath;
		String[] cmd = command.split(" ");
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.redirectErrorStream(true);
		
		Queue.videoQueue.addElement(path);
		final Process process = pb.start();
		//any output?
		Mp4Logger outputLogger = new Mp4Logger(process, path, video, videoDao);
		//start gobblers
		outputLogger.start();
	} 
	
	public void extractMp3FileFromMp4 (String path, Video video, VideoDao videoDao) throws IOException, InterruptedException {
		String containerFormat = path.split("\\.")[path.split("\\.").length-1];	
		String aPath = path.split(containerFormat)[0] + "mp3"; 
		
		String command = ffmpegBin +" -i "+path +" "+ ffmpegExtractMp3FileFromMp4 +" "+ aPath;
		String[] cmd = command.split(" ");
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.redirectErrorStream(true);
		
		final Process process = pb.start();
		//any output?
		Mp3Logger outputLogger = new Mp3Logger(process, path, video, videoDao);
		//start gobblers
		outputLogger.start();
	}
}