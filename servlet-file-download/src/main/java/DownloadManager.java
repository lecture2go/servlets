


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class DownloadManager
 */
public class DownloadManager extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String repositoryRoot="";
	private String downloadServerName="";
	
	@Override
	public void init() throws ServletException {
    	//init param
		repositoryRoot=getServletConfig().getInitParameter("repositoryRoot");
		downloadServerName=getServletConfig().getInitParameter("downloadServerName");
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unused")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String downloadPath = repositoryRoot+request.getParameter("downloadPath");
		String downloadAllowed = request.getParameter("downloadAllowed");
		String dsn = request.getServerName();
		try{
			//download video if allowed
			if(downloadAllowed.equals("1") && dsn.equals(downloadServerName)){
				//build html site for this download
				File f = new File (downloadPath);
				String fileName=f.getName();
				String fileLenght=f.length()+"";
				
				//set the content type(can be excel/word/powerpoint etc..)
				response.setContentType ("application/"+fileName.split("\\.")[1]+"");
				
				//set the header and also the Name by which user will be prompted to save
				response.setHeader ("Content-Disposition", "attachment; filename=\""+fileName+"\"");
				response.setHeader ("Content-Description", "Lecture2go Download"); 
				response.setHeader ("Content-Type", "application/force-download"); 
				response.setHeader ("Content-Length", fileLenght); 	
				
				//Open an input stream to the file and post the file contents thru the 
				//servlet output stream to the client m/c

			    try {
			    	InputStream in = new FileInputStream(f);
					ServletOutputStream outs = response.getOutputStream();
				 
					byte[] buffer = new byte[4096];
				         
					int bytesRead;
			        while ((bytesRead = in.read(buffer)) > 0) {
			        	outs.write(buffer, 0, bytesRead);
			        }
				    outs.flush();
				    outs.close();
				    in.close();	
			    } catch(IOException ioe) {
			    	ioe.printStackTrace(System.out);
			    }
			}
		}catch(Exception e){}	
	}
}