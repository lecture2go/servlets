

import java.io.File;
import java.util.List;
import java.util.ListIterator;

public abstract class QueueManager extends Queue {
	
	public QueueManager() {}
	
	public QueueManager( String videoRepository){
    	super.videoRepository = videoRepository;   
    }
	
    protected abstract void updateQueue() throws InterruptedException;
    
	public void addVideofilesForConvertingToQueue(List<String> vList) {
		ListIterator<String> vIt = vList.listIterator();
		while (vIt.hasNext()) {
			String v = vIt.next();
			// add if not exist
			if (!contanisVideofileForConverting(v))
				videoListForConverting.add(v);
		}
	}

	public void addMp4videosForMp3ExtractionToQueue(List<String> vList) {
		ListIterator<String> vIt = vList.listIterator();
		while (vIt.hasNext()) {
			String v = vIt.next();
			// add if not exist
			if (!contanisMp4videosForMp3Extraction(v))
				videoListForExtracting.add(v);
		}
	}

	private boolean contanisVideofileForConverting(String v) {
		boolean bool = false;
		//mp4 exists
		String pathFileToConvert = v;
		String containerFormat = pathFileToConvert.split("\\.")[pathFileToConvert.split("\\.").length-1];	
		String aPath = pathFileToConvert.split(containerFormat)[0] + "mp4"; 
		File vF = new File(aPath);
		if (vF.isFile())
			bool = true;// video is being converted or has been converted
		else {
			ListIterator<String> vIt = videoListForConverting.listIterator();
			while (vIt.hasNext()) {
				// video is all ready in the first queue (fist queue is the
				// queue with all video files to convert)
				if (v.equals(vIt.next()))
					bool = true;
			}
		}
		return bool;
	}

	private boolean contanisMp4videosForMp3Extraction(String v) {
		boolean bool = false;
		File vF = new File(v);
		if (vF.isFile())
			bool = true;// audio is being extracted from video or has been
						// extracted
		else {
			ListIterator<String> vIt = videoListForExtracting.listIterator();
			while (vIt.hasNext()) {
				// video is all ready in the first queue (fist queue is the
				// queue with all video files for extracting)
				if (v.equals(vIt.next()))
					bool = true;
			}
		}
		return bool;
	}

}
