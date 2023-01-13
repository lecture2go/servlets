package de.uhh.l2g.webservices.videoprocessor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;

import de.uhh.l2g.webservices.videoprocessor.model.opencast.Medium;
import de.uhh.l2g.webservices.videoprocessor.util.Config;

public class Subtitle2GoApiCall {
	// the application configuration
	private static Config config = Config.getInstance();
	
	public static String startEndpoint = "start";

	
	/**
	 * Sends a post request to the subtitle2go API.
	 * This creates a new auto captioning with the given video file
	 * @param filepath the path of the file which will be converted
	 * @param language the language of the video
	 * @param id the id
	 * @param additionalProperties additional properties
	 * @return the response from the opencast API
	 */
	static String postAutoCaptionRequest(String filePath, Long id, String language, Map<String, Object> additionalProperties) {
		// prepare the api call
		WebTarget target = prepareApiCall(startEndpoint);
			
		// create the payload necessary for the the request
		String jsonPayload = createStartCaptionJson(id, filePath, language, additionalProperties);

		// the file is sent as a stream to the API
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(filePath);
			
//			multiPart.bodyPart(new StreamDataBodyPart("presenter",fileInputStream,FilenameUtils.getName(filepath)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// send the post request
		Response response = null;
		try {
			response = target.request().post(Entity.entity(jsonPayload, MediaType.APPLICATION_JSON));
		} catch (Exception e) {
			// on failure close the inputStream
			IOUtils.closeQuietly(fileInputStream);
			throw new WebApplicationException();
		}

		// after sending the request, close the inputStream
		IOUtils.closeQuietly(fileInputStream);
		
		//TODO correct responses and how to obtain ID of subtitle2go
		switch(response.getStatus()) {
			case 200:
				// everything is fine
			    String subtitle2goId = response.readEntity(String.class);
			    
				return subtitle2goId;
			case 400:
				// API call has errors
				throw new BadRequestException();
			default:
				// other general error
				throw new WebApplicationException();
		}
	}
	
	
	/**
	 * Creates an subtitle2go-compatible processing json string (used for a new auto captioning request)
	 * @param workflow 
	 * @return the processing info as as json string
	 */
	private static String createStartCaptionJson(Long id, String filename, String language, Map<String, Object> additionalProperties) {
		Map<String, String> configuration = new HashMap<String,String>();
		configuration.put("id",id.toString());
		configuration.put("filename",filename.toString());
		configuration.put("language",language);

		// send url of this subtitle2go instance for callback
		configuration.put("url", config.getProperty("url.autocaption"));
		
		// add additional properties if there are any
		if (!additionalProperties.isEmpty()) {
			for (Map.Entry<String, Object> additionalProperty : additionalProperties.entrySet()) {
				configuration.put(additionalProperty.getKey(), additionalProperty.getValue().toString());
			}
		}
	
		
		String configurationAsJson = null;
		try {
			configurationAsJson = new ObjectMapper().writeValueAsString(configuration);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		return configurationAsJson;
	}
	/**
	 * Prepares the API call
	 * @param endpoint the endpoint which should be called
	 * @return a web target to connect to
	 */
	private static WebTarget prepareApiCall(String endpoint) {
		String url = config.getProperty("subtitle2go.url.api");
		
		WebTarget target = prepareSubtitle2GoCall(url);
		return target.path(endpoint);
	}
	
	
	/**
	 * Sets the client configuration for an call to the subtitle2go server 
	 * @param url to connect to
	 * @return a WebTarget object which will be used for the connection
	 */
	private static WebTarget prepareSubtitle2GoCall(String url) {

		// authentication
//		HttpAuthenticationFeature authentication = HttpAuthenticationFeature.basic(user,password);
		
		// create and configure REST consuming client
		Client client = ClientBuilder.newClient();
//		client.register(authentication);
//		client.property(ClientProperties.CHUNKED_ENCODING_SIZE, 1024);
//		client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
				
		WebTarget target = client.target(url);
		
		return target;
	}
	

}
