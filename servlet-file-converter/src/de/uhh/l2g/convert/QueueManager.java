package de.uhh.l2g.convert;

import java.io.File;
import java.util.List;
import java.util.ListIterator;

import de.uhh.l2g.beans.Host;
import de.uhh.l2g.beans.Producer;
import de.uhh.l2g.beans.Video;
import de.uhh.l2g.dao.HostDao;
import de.uhh.l2g.dao.ProducerDao;
import de.uhh.l2g.dao.VideoDao;

public abstract class QueueManager extends Queue {
	
	public QueueManager() {}
	
	public QueueManager(VideoDao videoDao, ProducerDao producerDao, HostDao hostDao, String videoRepository){
    	super.videoDao=videoDao;
    	super.producerDao = producerDao;
    	super.hostDao = hostDao;
    	super.videoRepository = videoRepository;   
    }
	
    	protected abstract void updateQueue() throws InterruptedException;
    
		protected String getSourcePath(Video video){
			//initialize file name and path
			String fileName=null;
			String path = null;

			Producer p = producerDao.getById(video.getProducerId()).iterator().next(); 
			Host h = hostDao.getById(video.getHostId()).iterator().next();
				
			//file name
			if(video.isOpenaccess())fileName=video.getFilename();
			else fileName = video.getSecureFilename();
			//path to file
			path = videoRepository+"/"+h.getName()+"/"+p.getHomeDir()+"/"+fileName;

			return path;
		}

		private String getFilePath(Video video, String type){
			String path = getSourcePath(video);
			String containerFormat = path.split("\\.")[path.split("\\.").length-1];	
			String aPath = path.split(containerFormat)[0] + type; 
			return aPath;
		}
		
		protected File getFile(Video video, String type){
			return new File(getFilePath(video, type));
		}

	    public void addVideofilesForConvertingToQueue(List<Video> vList){
	    	ListIterator<Video> vIt = vList.listIterator();
	    	while(vIt.hasNext()){
	    		Video v = vIt.next();
	    		//add if not exist
	    		if(!contanisVideofileForConverting(v))videoListForConverting.add(v);
	    	}
	    }

	    public void addMp4videosForMp3ExtractionToQueue(List<Video> vList){
	    	ListIterator<Video> vIt = vList.listIterator();
	    	while(vIt.hasNext()){
	    		Video v = vIt.next();
	    		//add if not exist
	    		if(!contanisMp4videosForMp3Extraction(v))videoListForExtracting.add(v);
	    	}
	    }
	    
	    private boolean contanisVideofileForConverting(Video v){
	    	boolean bool=false;
	    	File vF = getFile(v, "mp4");
	    	if (vF.isFile())bool = true;//video is being converted or has been converted
	    	else{
		    	ListIterator<Video> vIt = videoListForConverting.listIterator();
		    	while(vIt.hasNext()){
		    		//video is all ready in the first queue (fist queue is the queue with all video files to convert)
		    		if(v.getId()==vIt.next().getId()){
		    			bool=true;
		    		}
		    	}	    		
	    	}
	    	return bool;
	    }
	    
	    private boolean contanisMp4videosForMp3Extraction(Video v){
	    	boolean bool=false;
	    	File vF = getFile(v, "mp3");
	    	if (vF.isFile())bool = true;//audio is being extracted from video or has been extracted
	    	else{
		    	ListIterator<Video> vIt = videoListForExtracting.listIterator();
		    	while(vIt.hasNext()){
		    		//video is all ready in the first queue (fist queue is the queue with all video files for extracting)
		    		if(v.getId()==vIt.next().getId()){
		    			bool=true;
		    		}
		    	}	    		
	    	}
	    	return bool;
	    }
	    
}
