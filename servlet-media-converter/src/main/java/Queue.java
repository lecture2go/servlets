

import java.util.ArrayList;
import java.util.Vector;

public class Queue extends VideoThread{

	 	static int MAXQUEUE = 0;
	    
	 	public static int getMaxqueue() {
			return MAXQUEUE;
		}

		@SuppressWarnings("rawtypes")
		protected static Vector videoQueue = new Vector();
		@SuppressWarnings("rawtypes")
		protected static Vector audioQueue = new Vector();
		protected static ArrayList<String> videoListForConverting = new ArrayList<String>();
		protected static ArrayList<String> videoListForExtracting = new ArrayList<String>();
		
		public Queue(){}
		
		public Queue(String videoRepository, String ffmpegBin){
	    	super.videoRepository = videoRepository;   
	    	super.ffmpegBin = ffmpegBin; 
	    }
}
