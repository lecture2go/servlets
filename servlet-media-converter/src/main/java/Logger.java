

import hirondelle.date4j.DateTime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class Logger extends Thread {
	protected Process process;
	protected String path;
	protected String converterLog;
	protected String videoDuration="";
	protected String currentTime="";
	protected float percentDone=0;	

    public Logger() {}
    
    public Logger(Process process, String path) {
        this.process = process;
        this.path = path;
    }
    
	protected static boolean processIsRunning(Process process) {
    	try {
    		process.exitValue();
    		return false;
    	}catch(IllegalThreadStateException e) {
    		return true;
    	}
    }	
	
	protected void writeLogs(File ffmpegOutputFile, File outputFile) {
		InputStreamReader isr = new InputStreamReader(process.getInputStream());
		BufferedReader br = new BufferedReader(isr);
		String line = null;
		  
		try {
		  //log ffmpeg output
		  BufferedWriter ffmpegOutput = new BufferedWriter(new FileWriter(ffmpegOutputFile));
		  
		  //write lines to file
		  while ((line = br.readLine()) != null){
			  ffmpegOutput.write(line+" \n");
			  //get duration
			  try{ 
				  if(videoDuration.isEmpty())videoDuration = line.split("Duration:")[1].split(",")[0]; 
			  }catch(ArrayIndexOutOfBoundsException e){}
			  
			  //get current time
			  try{currentTime = line.split("time=")[1].split("bitrate")[0];}catch(ArrayIndexOutOfBoundsException e){}
			  if(!currentTime.isEmpty() && !videoDuration.isEmpty()){
				  DateTime dur = new DateTime(videoDuration);
				  DateTime ct = new DateTime(currentTime);
				  
				  //calculate percents done
				  try{
					  if(dur.gt(ct)){ //duration greater than current time
						  float durSec = dur.getHour()*60*60+dur.getMinute()*60+dur.getSecond();
						  float ctSec = ct.getHour()*60*60+ct.getMinute()*60+ct.getSecond();
						  percentDone = (float)(ctSec/durSec*100);
					  }
					  else { percentDone = 100; }						  
				  }catch(Exception e){}
				  
				  //log percent done value
				  converterLog=percentDone+"%"+" running: "+processIsRunning(process);
				  log(outputFile);
			  }
			 
			  //log
			  converterLog=percentDone+"%"+" running: "+processIsRunning(process);
			  log(outputFile);
		  }
		  ffmpegOutput.close();
		} 
		catch ( IOException e ) {
			e.printStackTrace();
			converterLog=percentDone+"%"+" running: "+processIsRunning(process);
			log(outputFile);
		}
		
		//update queue after process is done
		if(outputFile.getPath().contains(".mp3.log")){
			QueueManager.audioQueue.removeElement(path);
			QueueManager.videoListForExtracting.remove(path);				
		}
		if(outputFile.getPath().contains(".mp4.log")){
			QueueManager.videoQueue.removeElement(path);
			QueueManager.videoListForConverting.remove(path);			
		}

		//interrupt this process and destroy
		interrupt();
		process.destroy();
		
		while(processIsRunning(process)){
			try {
				wait();
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		//log
		converterLog=percentDone+"%"+" running: "+processIsRunning(process);
		log(outputFile);
	}	
	
	private final void log(File converterOutputFile){
		  BufferedWriter converterOutput;
		try {
			converterOutput = new BufferedWriter(new FileWriter(converterOutputFile));
			converterOutput.write(converterLog);
			converterOutput.close();			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
