package com.hmkcode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.hmkcode.vo.FileMeta;

import de.uhh.l2g.util.Security;
import de.uhh.l2g.util.SyntaxManager;

public class MultipartRequestHandler {

	private static final Long MAX_SIZE = new Long("107374182400"); //100 GB

	public static List<FileMeta> uploadByJavaServletAPI(HttpServletRequest request) throws IOException, ServletException{
		
		List<FileMeta> files = 	 new LinkedList<FileMeta>();
		
		// 1. Get all parts
		Collection<Part> parts = request.getParts();
		
		// 2. Get paramter "twitter"
		//String twitter = request.getParameter("twitter");

		// 3. Go over each part
		FileMeta temp = null;
		for(Part part:parts){	

			// 3.1 if part is multiparts "file"
			if(part.getContentType() != null){
				
				// 3.2 Create a new FileMeta object
				temp = new FileMeta();
				temp.setFileName(getFilename(part));
				temp.setFileSize(part.getSize());
				temp.setFileType(part.getContentType());
				temp.setContent(part.getInputStream());
				
				// 3.3 Add created FileMeta object to List<FileMeta> files
				files.add(temp);

			}
		}
		return files;
	}
	
	public static List<FileMeta> uploadByApacheFileUpload(HttpServletRequest request) throws IOException, ServletException{
		List<FileMeta> files = new LinkedList<FileMeta>();
		
		// 1. Check request has multipart content
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		FileMeta temp = null;
		
		// 2. If yes (it has multipart "files")
		if(isMultipart){
			
			// prepare chunked upload
			String range = request.getHeader("Content-Range");
		    long fileFullLength = -1;
		    long chunkFrom = -1;
		    long chunkTo = -1;
		    if (range != null) {
		        if (!range.startsWith("bytes "))
		            throw new ServletException("Unexpected range format: " + range);
		        String[] fromToAndLength = range.substring(6).split(Pattern.quote("/"));
		        fileFullLength = Long.parseLong(fromToAndLength[1]);
		        String[] fromAndTo = fromToAndLength[0].split(Pattern.quote("-"));
		        chunkFrom = Long.parseLong(fromAndTo[0]);
		        chunkTo = Long.parseLong(fromAndTo[1]);
		    }
		    
		    // set default dir, may be overwritten later on
		    File tempDir = new File(System.getProperty("java.io.tmpdir"));  // Configure according project server environment.

			// 2.1 instantiate Apache FileUpload classes
			DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setSizeThreshold(0);//save all to disk
			
			// set the upload temp directory, should be set to the target folder to minimize file movement after upload
			// temporary directory needs to be transmitted by a custom "X-tempdir"-http-header
			if (request.getHeader("X-tempdir") != null) {
				String tempDirString = request.getHeader("X-tempdir");
				tempDir = new File(tempDirString);
				factory.setRepository(tempDir);
			}
		    File storageDir = tempDir;
			
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setSizeMax(MAX_SIZE);
			// 2.2 Parse the request
			try {
				// 2.3 Get all uploaded FileItem
				List<FileItem> items = upload.parseRequest(request);
				String repository = "";
				String openaccess = "";
				String lectureseriesNumber = "00.000";
				String fileName = "";
				String secureFileName = "";
				String l2gDateTime = "";
				String videoId = "";
				
				// 2.4 Go over each FileItem
				for(FileItem item:items){
					
					// 2.5 if FileItem is not of type "file"
				    if (item.isFormField()) {
				    	// 2.6 Search for parameter
				        if(item.getFieldName().equals("repository"))repository = item.getString();
				        if(item.getFieldName().equals("l2gDateTime"))l2gDateTime = item.getString();
				        if(item.getFieldName().equals("openaccess"))openaccess = item.getString();
				        if(item.getFieldName().equals("lectureseriesNumber") && item.getString().trim().length()>0)lectureseriesNumber = item.getString();
				        if(item.getFieldName().equals("fileName") && item.getString().trim().length()>0)fileName = item.getString();
				        if(item.getFieldName().equals("secureFileName") && item.getString().trim().length()>0)secureFileName = item.getString();
				        if(item.getFieldName().equals("videoId") && item.getString().trim().length()>0)videoId = item.getString();
				    } else {
				        String itemName=item.getName();
				        String container = itemName.split("\\.")[itemName.split("\\.").length-1].toLowerCase();//only container to lower case!
				        // 2.7 Create FileMeta object
				    	temp = new FileMeta();
				    	temp.setOpenAccess(openaccess);
						temp.setFileName(itemName);
						temp.setContent(item.getInputStream());
						temp.setFileType(item.getContentType());
						temp.setFileSize(item.getSize());
						//upload 
						File f = new File("");
						try {
							//there is already an uploaded media file
							String prefix = "";
							if(fileName.length()>0){
								prefix = fileName.split("."+fileName.split("\\.")[fileName.split("\\.").length-1])[0];
								itemName = prefix+"."+container;
								temp.setFileName(itemName);
								//for secure file name
								if(secureFileName.length()>0){
									prefix = secureFileName.split("."+secureFileName.split("\\.")[secureFileName.split("\\.").length-1])[0];
									temp.setSecureFileName(prefix+"."+container);
								} else {
									String sFN = Security.createSecureFileName()+"."+container;
									temp.setSecureFileName(sFN);	
								}
							}else{ 
								//or this is the first upload
								if(fileName.length()==0 && secureFileName.endsWith(".xx")){
									itemName = generateL2gFileName(lectureseriesNumber, container, l2gDateTime, videoId);
									temp.setSecureFileName(secureFileName.replace(".xx", "."+container));
									temp.setFileName(itemName);
								}
							}
							//////////// ---- //////////// ---- ////////////
							//new file -> item is lecture2go named file?
							if(!SyntaxManager.isL2gFileName(itemName)){
								itemName = generateL2gFileName(lectureseriesNumber, container, l2gDateTime, videoId);
								temp.setFileName(itemName);
							}
							//video isn't open access
							if(openaccess.equals("0")){
								//rename to secure string file name
								f = new File(repository+"/"+temp.getSecureFileName());
							}else{
								f = new File(repository+"/"+temp.getFileName());
							}
							prefix = itemName.split("."+container)[0];
							String[] parameter = prefix.split("\\_");
							temp.setGenerationDate(parameter[2]+"_"+parameter[3]);
							// 2.7 Add created FileMeta object to List<FileMeta> files
							
							if (fileFullLength < 0) {  // File is not chunked
			                    temp.setFileSize(item.getSize());
			                    item.write(f);
			                } else {  // File is chunked
			                    File assembledFile = null;
			                    byte[] bytes = item.get();
			                    if (chunkFrom + bytes.length != chunkTo + 1)
			                        throw new ServletException("Unexpected length of chunk: " + bytes.length + 
			                                " != " + (chunkTo + 1) + " - " + chunkFrom);
								
			                    saveChunk(storageDir, temp.getCurrentFileName(), chunkFrom, bytes, fileFullLength);
			                    TreeMap<Long, Long> chunkStartsToLengths = getChunkStartsToLengths(storageDir, temp.getCurrentFileName());
			                    long lengthSoFar = getCommonLength(chunkStartsToLengths);
			                    temp.setFileSize(lengthSoFar);

			                    if (lengthSoFar == fileFullLength) {
			                        assembledFile = assembleAndDeleteChunks(storageDir, temp.getCurrentFileName(), 
			                                new ArrayList<Long>(chunkStartsToLengths.keySet()));
			                    }
			                }

							files.add(temp);
						} catch (Exception e) {
							e.printStackTrace();
						}
				    }
				}
				
			} catch (FileUploadException e) {
				e.printStackTrace();
			}
		}
		
		return files;
	}

	private static String generateL2gFileName(String lectureseriesNumber, String container, String videoId){
		return generateL2gFileName(lectureseriesNumber, container,"", videoId);
	}

		
	private static String generateL2gFileName(String lectureseriesNumber, String container, String l2gDateTime, String videoId){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
		String newDate = format.format(new Date()).toString();
		if(l2gDateTime.length()>0)newDate=l2gDateTime;
		String ret = lectureseriesNumber+"_video-"+videoId+"_"+newDate+"."+container;
		return ret;
	}
	// this method is used to get file name out of request headers
	// 
	private static String getFilename(Part part) {	
	    for (String cd : part.getHeader("content-disposition").split(";")) {
	        if (cd.trim().startsWith("filename")) {
	            String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
	            return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1); // MSIE fix.
	        }
	    }
	    return null;
	}
	

	private static void saveChunk(File dir, String fileName, 
	        long from, byte[] bytes, long fileFullLength) throws IOException {
	    File target = new File(dir, fileName + "." + from + ".chunk");
	    OutputStream os = new FileOutputStream(target);
	    try {
	        os.write(bytes);
	    } finally {
	        os.close();
	    }
	}

	private static TreeMap<Long, Long> getChunkStartsToLengths(File dir, 
	        String fileName) throws IOException {
	    TreeMap<Long, Long> chunkStartsToLengths = new TreeMap<Long, Long>();
	    for (File f : dir.listFiles()) {
	        String chunkFileName = f.getName();
	        if (chunkFileName.startsWith(fileName + ".") && 
	                chunkFileName.endsWith(".chunk")) {
	            chunkStartsToLengths.put(Long.parseLong(chunkFileName.substring(
	                    fileName.length() + 1, chunkFileName.length() - 6)), f.length());
	        }
	    }
	    return chunkStartsToLengths;
	}

	private static long getCommonLength(TreeMap<Long, Long> chunkStartsToLengths) {
	    long ret = 0;
	    for (long len : chunkStartsToLengths.values())
	        ret += len;
	    return ret;
	}

	private static File assembleAndDeleteChunks(File dir, String fileName, 
	        List<Long> chunkStarts) throws IOException {
	    File assembledFile = new File(dir, fileName);
	    if (assembledFile.exists()) // if file already exists, overwrite
	        assembledFile.delete();
	    OutputStream assembledOs = new FileOutputStream(assembledFile);
	    byte[] buf = new byte[100000];
	    try {
	        for (long chunkFrom : chunkStarts) {
	            File chunkFile = new File(dir, fileName + "." + chunkFrom + ".chunk");
	            InputStream is = new FileInputStream(chunkFile);
	            try {
	                while (true) {
	                    int r = is.read(buf);
	                    if (r == -1)
	                        break;
	                    if (r > 0)
	                        assembledOs.write(buf, 0, r);
	                }
	            } finally {
	                is.close();
	            }
	            chunkFile.delete();
	        }
	    } finally {
	        assembledOs.close();
	    }
	    return assembledFile;
	}
}
