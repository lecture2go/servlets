package de.uhh.l2g.convert;

import java.util.ArrayList;
import java.util.Vector;

import de.uhh.l2g.beans.Video;
import de.uhh.l2g.dao.HostDao;
import de.uhh.l2g.dao.ProducerDao;
import de.uhh.l2g.dao.VideoDao;

public class Queue extends VideoThread{

	 	static int MAXQUEUE = 0;
	    
	 	public static int getMaxqueue() {
			return MAXQUEUE;
		}

		@SuppressWarnings("rawtypes")
		protected static Vector videoQueue = new Vector();
		@SuppressWarnings("rawtypes")
		protected static Vector audioQueue = new Vector();
		protected static ArrayList<Video> videoListForConverting = new ArrayList<Video>();
		protected static ArrayList<Video> videoListForExtracting = new ArrayList<Video>();
		
		public Queue(){}
		
		public Queue(VideoDao videoDao, ProducerDao producerDao, HostDao hostDao, String videoRepository, String ffmpegBin){
	    	super.videoDao=videoDao;
	    	super.producerDao = producerDao;
	    	super.hostDao = hostDao;
	    	super.videoRepository = videoRepository;   
	    	super.ffmpegBin = ffmpegBin; 
	    }
}
