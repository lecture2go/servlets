

import java.io.File;
import java.io.IOException;

public class QueueManagerForExtracting extends QueueManager {
	
		static boolean extractionIsRunning = false;
	
		public QueueManagerForExtracting(String videoRepository){
	    	super.videoRepository = videoRepository;   
	    }
	
	    @Override
	    public void run() {
	        try {
	        	if(!extractionIsRunning)extractionIsRunning=true;
	        	updateQueue();
	        } catch (InterruptedException e) {
	        	e.printStackTrace();
	        	extractionIsRunning=false;
	        }
	    }
	    
	    protected void updateQueue() throws InterruptedException {
	        //get video list to convert
	        //iterate on to all videos and convert one after another  
			//initialize file name and path
			//go ahead with iteration 
			for(int i = 0; i<videoListForExtracting.size(); i++){
				String v = videoListForExtracting.get(i);
				File fileToExtract = new File(v);
				//mp4 converting process already done
				//add video to list, if not exists yet
				if(!fileToExtract.isFile()){
					//start to convert this video
					try {
						new Converter().extractMp3FileFromMp4(fileToExtract); 
					} catch (IOException e) { e.printStackTrace(); }
				}
				//wait, if queue is full
				while(audioQueue.size()==MAXQUEUE){
					sleep(5000);
				}
			}   
			//sleeping 
			sleep(15000);
			updateQueue();
	    } 	
	    
		public static boolean removeFileFromVideoListForExtracting(File fileToExtract){
			for(int a=0;a<videoListForExtracting.size();a++){
				if(videoListForExtracting.get(a).equals(fileToExtract.toString())){
					videoListForExtracting.remove(a);
				}
			}
			return true;
		}	    
}
