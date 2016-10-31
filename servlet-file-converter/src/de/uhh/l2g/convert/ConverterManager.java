package de.uhh.l2g.convert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class ConverterManager extends HttpServlet {

	private static final long serialVersionUID = 1L;
	//Lecture2Go database objects 
	private final DriverManagerDataSource dmds = new DriverManagerDataSource();
	private String videoRepository;
			
	protected static String ffmpegConvertFileToMp4CommandParameter;
	protected static String ffmpegExtractMp3FileFromMp4;
	protected static String ffmpegBin;
	
	@Override
	public void init() throws ServletException {
     	//required
		videoRepository = getServletConfig().getInitParameter("vRep");
		//initialize max. number of simultaneous processes 
     	Queue.MAXQUEUE = new Integer (getServletConfig().getInitParameter("maxQueue"));
     	//ffmpegBin
     	ffmpegBin = getServletConfig().getInitParameter("ffmpegBin");
     	//ffmpeg command
     	ffmpegConvertFileToMp4CommandParameter = getServletConfig().getInitParameter("ffmpegConvertFileToMp4CommandParameter");
     	ffmpegExtractMp3FileFromMp4 = getServletConfig().getInitParameter("ffmpegExtractMp3FileFromMp4");
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//define queue manager
		QueueManager QMC = new QueueManagerForConverting(videoRepository);
		QueueManager QME = new QueueManagerForExtracting(videoRepository);
		//action convert by Id
		String action = new String(request.getParameter("action")); 
		//id
		int id = new Integer(request.getParameter("id")); 
		//videoDao.getAllVideosForConvertion() (example to convert all videos in database)
		List<String> vList = new ArrayList<String>();//INITIALIZE VIDEO LIST
		
		//convert file to mp4
		if(action.contains("convertFileToMp4")){
			//add video file to queue for converting if not yet done
			QMC.addVideofilesForConvertingToQueue(vList);
		}
		//extract mp3 from mp4
		if(action.contains("extractMp3FromMp4")){
			//add audio file to queue for converting if not yet done
			QME.addMp4videosForMp3ExtractionToQueue(vList);			
		}
		//start queue manager, if not running yet
		if(!QueueManagerForConverting.convertionIsRunning) QMC.start();
		if(!QueueManagerForExtracting.extractionIsRunning) QME.start();
	}
}