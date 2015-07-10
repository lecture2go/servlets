package de.uhh.l2g.convert;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import de.uhh.l2g.beans.Video;
import de.uhh.l2g.dao.FacilityDao;
import de.uhh.l2g.dao.HostDao;
import de.uhh.l2g.dao.LectureseriesDao;
import de.uhh.l2g.dao.ProducerDao;
import de.uhh.l2g.dao.VideoDao;

public class ConverterManager extends HttpServlet {

	private static final long serialVersionUID = 1L;
	//Lecture2Go database objects 
	private VideoDao videoDao = new VideoDao();
	private ProducerDao producerDao = new ProducerDao();
	private HostDao hostDao = new HostDao();
	private FacilityDao facilityDao = new FacilityDao();
	private LectureseriesDao lectureseriesDao = new LectureseriesDao();
	private final DriverManagerDataSource dmds = new DriverManagerDataSource();
	private String videoRepository;
			
	protected static String ffmpegConvertFileToMp4CommandParameter;
	protected static String ffmpegExtractMp3FileFromMp4;
	protected static String ffmpegBin;
	
	@Override
	public void init() throws ServletException {
    	//MySQL connection
    	dmds.setDriverClassName(getServletConfig().getInitParameter("driverClassName"));
    	dmds.setUrl(getServletConfig().getInitParameter("driverUrl"));
    	dmds.setUsername(getServletConfig().getInitParameter("dbUsername"));
    	dmds.setPassword(getServletConfig().getInitParameter("dbPassword"));    	
    	
    	//Set dao objects
    	hostDao.setDataSource(dmds);
    	lectureseriesDao.setDataSource(dmds);
    	facilityDao.setDataSource(dmds);
    	facilityDao.setLectureseriesDao(lectureseriesDao);
    	facilityDao.setHostDao(hostDao);
    	producerDao.setDataSource(dmds);
    	producerDao.setFacilityDao(facilityDao);
     	videoDao.setDataSource(dmds);
     	
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
		QueueManager QMC = new QueueManagerForConverting(videoDao, producerDao, hostDao, videoRepository);
		QueueManager QME = new QueueManagerForExtracting(videoDao, producerDao, hostDao, videoRepository);
		//action convert by Id
		String action = new String(request.getParameter("action")); 
		//id
		int id = new Integer(request.getParameter("id")); 
		//videoDao.getAllVideosForConvertion() (example to convert all videos in database)
		List<Video> vList = videoDao.getByIdForConvertion(id);
		
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