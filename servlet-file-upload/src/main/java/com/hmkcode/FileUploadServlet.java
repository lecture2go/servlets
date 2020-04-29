package com.hmkcode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmkcode.vo.FileMeta;

import de.uhh.l2g.util.Security;
    
//this to be used with Java Servlet 3.0 API
@MultipartConfig 
public class FileUploadServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	// this will store uploaded files
	private List<FileMeta> files = new LinkedList<FileMeta>();
	/***************************************************
	 * URL: /upload
	 * doPost(): upload the files and other parameters
	 ****************************************************/
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		
		// validate if the request has the correct token
		String transmittedToken = request.getHeader("X-token");
		String expirationTime = request.getHeader("X-expiration");
		String videoId = request.getHeader("X-videoId");
				
		String correctToken = null;
		try {
			correctToken = Security.getSignatureKey(Security.getSignatureKey(expirationTime, videoId));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (transmittedToken == null || 
				correctToken == null || 
				!transmittedToken.equals(correctToken) ||
				System.currentTimeMillis()>Long.valueOf(expirationTime)) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
	    	response.getWriter().print("");
		}
		
		// 1. Upload File Using Java Servlet API
		//files.addAll(MultipartRequestHandler.uploadByJavaServletAPI(request));			
	//	List<FileMeta> files = new ArrayList<FileMeta>();	

		// 1. Upload File Using Apache FileUpload
//		files = MultipartRequestHandler.uploadByApacheFileUpload(request);
		
		// 2. Set response type to json
		response.setContentType("application/json");
		response.addHeader("Access-Control-Allow-Origin", "*");
		
		// 3. Convert List<FileMeta> into JSON format
    	ObjectMapper mapper = new ObjectMapper();
    	JSONArray jsonA = new JSONArray();
    	List<FileMeta>  lf = MultipartRequestHandler.uploadByApacheFileUpload(request);
    	ListIterator<FileMeta> iLf = lf.listIterator();
    	while(iLf.hasNext()){
    		JSONObject jsonO = new JSONObject();
			FileMeta fm = iLf.next();
    		try {
    			String name = "";
    			if(fm.getOpenAccess().trim().equals("1"))name=fm.getFileName();
    			else name = fm.getSecureFileName();
    			
    			jsonO.put("name", name);
				jsonO.put("fileName", fm.getFileName());
				jsonO.put("secureFileName", fm.getSecureFileName());
				jsonO.put("id", name.replace(".", ""));
				jsonO.put("size", fm.getFileSize());
				jsonO.put("type", fm.getFileType());
				jsonO.put("openAccess", fm.getOpenAccess());
				jsonO.put("generationDate", fm.getGenerationDate());
				jsonA.put(jsonO);
			} catch (JSONException e) {
				e.printStackTrace();
			}
    		
    	}
		// 4. Send resutl to client
    	// Get the printwriter object from response to write the required json object to the output stream      
    	PrintWriter out = response.getWriter();
    	// Assuming your json object is **jsonObject**, perform the following, it will return your json object  
    	out.print(jsonA);
    	out.flush();        
	}
	
	/***************************************************
	 * URL: /upload?f=value
	 * doGet(): get file of index "f" from List<FileMeta> as an attachment
	 ****************************************************/
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		
		 // 1. Get f from URL upload?f="?"
		 String value = request.getParameter("f");
		 
		 // 2. Get the file of index "f" from the list "files"
		 FileMeta getFile = files.get(Integer.parseInt(value));
		 
		 try {		
			 	// 3. Set the response content type = file content type 
			 	response.setContentType(getFile.getFileType());
			 	
			 	// 4. Set header Content-disposition
			 	response.setHeader("Content-disposition", "attachment; filename=\""+getFile.getFileName()+"\"");
			 	
			 	// 5. Copy file inputstream to response outputstream
		        InputStream input = getFile.getContent();
		        OutputStream output = response.getOutputStream();
		        byte[] buffer = new byte[1024*10];
		        
		        for (int length = 0; (length = input.read(buffer)) > 0;) {
		            output.write(buffer, 0, length);
		        }
		        
		        output.close();
		        input.close();
		 }catch (IOException e) {
				e.printStackTrace();
		 }
		
	}
}
