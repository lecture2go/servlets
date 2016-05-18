package de.uhh.l2g.convert;

import java.io.File;
import java.io.IOException;
import de.uhh.l2g.beans.Video;
import de.uhh.l2g.dao.HostDao;
import de.uhh.l2g.dao.ProducerDao;
import de.uhh.l2g.dao.VideoDao;

public class QueueManagerForExtracting extends QueueManager {
	
		static boolean extractionIsRunning = false;
	
		public QueueManagerForExtracting(VideoDao videoDao, ProducerDao producerDao, HostDao hostDao, String videoRepository){
	    	super.videoDao=videoDao;
	    	super.producerDao = producerDao;
	    	super.hostDao = hostDao;
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
			String path = null;
			//go ahead with iteration 
			for(int i = 0; i<videoListForExtracting.size(); i++){
				Video v = videoListForExtracting.get(i);
				File mp3F = getFile(v, "mp3");
				path = getSourcePath(v);
				//mp4 converting process already done
				//add video to list, if not exists yet
				if(!mp3F.isFile()){
					//start to convert this video
					try {
						new Converter().extractMp3FileFromMp4(path, v, videoDao); 
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
}
