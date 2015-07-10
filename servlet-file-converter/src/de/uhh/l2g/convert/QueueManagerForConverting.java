package de.uhh.l2g.convert;

import java.io.File;
import java.io.IOException;
import de.uhh.l2g.beans.Video;
import de.uhh.l2g.dao.HostDao;
import de.uhh.l2g.dao.ProducerDao;
import de.uhh.l2g.dao.VideoDao;

public class QueueManagerForConverting extends QueueManager {
	
		static boolean convertionIsRunning = false;
	
		public QueueManagerForConverting(VideoDao videoDao, ProducerDao producerDao, HostDao hostDao, String videoRepository){
	    	super.videoDao=videoDao;
	    	super.producerDao = producerDao;
	    	super.hostDao = hostDao;
	    	super.videoRepository = videoRepository;   
	    }
	
	    @Override
	    public void run() {
	        try {
	        	if(!convertionIsRunning)convertionIsRunning=true;
	        	updateQueue();
	        } catch (InterruptedException e) {
	        	e.printStackTrace();
	        	convertionIsRunning=false;
	        }
	    }
	    
		protected void updateQueue() throws InterruptedException {
	        //get video list to convert
	        //iterate on to all videos and convert one after another  

			//initialize file name and path
			String path = null;
			//go ahead with iteration 
			for(int i = 0; i<videoListForConverting.size(); i++){
				Video v = videoListForConverting.get(i);
				File mp4F = getFile(v, "mp4");
				path = getSourcePath(v);
				//mp4 converting process already done
				//add video to list, if not exists yet
				if(!mp4F.isFile()){
					//start to convert this video
					try {
						new Converter().convertFileToMp4(path, v, videoDao); 
					} catch (IOException e) { e.printStackTrace(); }
				}
				//wait, if queue is full
				while(videoQueue.size()==MAXQUEUE){
					sleep(5000);
				}
			}   
			//sleeping 
			sleep(15000);
			updateQueue();
	    } 	
}
