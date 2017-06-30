

import java.io.File;
import java.io.IOException;

public class Converter extends ConverterManager{
	private static final long serialVersionUID = 1L;

	public Converter(){}
	
	@SuppressWarnings("unchecked")
	//Volumes/NONAME/95.2x302_Loop2x.mov 
	public void convertFileToMp4 (File fileToConvert) throws IOException, InterruptedException {
		String pathFileToConvert = fileToConvert.toString();
		String containerFormat = pathFileToConvert.split("\\.")[pathFileToConvert.split("\\.").length-1];	
		String aPath = pathFileToConvert.split(containerFormat)[0] + "mp4"; 
		
		String command = ffmpegBin +" -i "+pathFileToConvert +" "+ ffmpegConvertFileToMp4CommandParameter +" "+ aPath;
		String[] cmd = command.split(" ");
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.redirectErrorStream(true);

		Queue.videoQueue.addElement(pathFileToConvert);
		final Process process = pb.start();
		//remove from que for converting, 
		//because the process has started
		QueueManagerForConverting.removeFileFromVideoListForConverting(fileToConvert);
		
		//any output?
		Mp4Logger outputLogger = new Mp4Logger(process, pathFileToConvert);
		//start gobblers
		outputLogger.start();
	} 
	
	//Volumes/NONAME/95.2x302_Loop2x.mp4 
	public void extractMp3FileFromMp4 (File fileToConvert) throws IOException, InterruptedException {
		String pathFileToConvert = fileToConvert.toString();
		String containerFormat = pathFileToConvert.split("\\.")[pathFileToConvert.split("\\.").length-1];	
		String aPath = pathFileToConvert.split(containerFormat)[0] + "mp3"; 
		
		String command = ffmpegBin +" -i "+pathFileToConvert +" "+ ffmpegExtractMp3FileFromMp4 +" "+ aPath;
		String[] cmd = command.split(" ");
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.redirectErrorStream(true);
		
		final Process process = pb.start();
		//any output?
		Mp3Logger outputLogger = new Mp3Logger(process, pathFileToConvert);
		//start gobblers
		outputLogger.start();
	}
}