package de.uhh.l2g.webservices.videoprocessor.service;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uhh.l2g.webservices.videoprocessor.model.opencast.Publication;


public class OpencastApiCall {
	
	public static String eventEndpoint = "events";

	
	/**
	 * Sends an request to the opencast API
	 */
	static Response sendRequest(String endpoint, String requestType) {
		/*
		
		// TODO: change hardcoded to properties
		String url = "http://opencast1.rrz.uni-hamburg.de:8080/api/";
		String user = "admin";
		String password = "opencast";
		
		// authentication
		HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(user,password);
		
		Client client = ClientBuilder.newClient().register(feature);
		WebTarget target = client.target(url).path(endpoint);
		
		target.request(MediaType.APPLICATION_JSON_TYPE);
		
		*/
		return null;
		
	}
	
	/**
	 * Sends a post request to the opencast API events endpoint.
	 * This creates a new event on the opencast system with the given video file
	 * @param filepath the path of the file which will be converted
	 * @param title the title which will be used in the opencast system
	 * @param sourceid the source id
	 * @return the response from the opencast API
	 */
	static String postNewEventRequest(String filepath, String title, Long sourceId) {
		// TODO: change hardcoded to properties
		//String eventEndpoint = "events";
		
		// prepare the api call
		WebTarget target = prepareApiCall(eventEndpoint);
		
		// multipart bodyreader must be initiated
        target.register(MultiPartFeature.class);
				
		// create the parts necessary for the the request
		String acl = createAclJson();
		String metadata = createMetadataJson(title, sourceId);
		String processing = createProcessingJson(sourceId);

		// create the multipart form data
		FormDataMultiPart multiPart = new FormDataMultiPart();
		multiPart.bodyPart(new FormDataBodyPart("acl",acl));
		multiPart.bodyPart(new FormDataBodyPart("processing",processing));
		multiPart.bodyPart(new FormDataBodyPart("metadata",metadata));
		
		// the file is sent as a stream to the API to guarantee delivery of even huge files
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(filepath);
			multiPart.bodyPart(new StreamDataBodyPart("presenter",fileInputStream,FilenameUtils.getName(filepath)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// send the post request
		Response response = null;
		try {
			response = target.request().post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE));
		} catch (Exception e) {
			// on failure close the inputStream
			IOUtils.closeQuietly(fileInputStream);
			throw new WebApplicationException();
		}

		// after sending the request, close the inputStream
		IOUtils.closeQuietly(fileInputStream);
		
		switch(response.getStatus()) {
			case 201:
				// everything is fine 
				String opencastUrl = response.getHeaderString("location");
				// return the opencast-id from the opencast URL
				return FilenameUtils.getName(opencastUrl);
			case 400:
				// API call has errors
				throw new BadRequestException();
			default:
				// other general error
				throw new WebApplicationException();
		}
		
		
		
	}
	
	public static Publication getPublication(String opencastId, String publicationChannel) {
		String publicationsEndpoint = eventEndpoint + "/" + opencastId + "/publications";
		WebTarget target = prepareApiCall(publicationsEndpoint);
		
		// saves a list of ob publications to a publications object
		List<Publication>publications = target.request(MediaType.APPLICATION_JSON_TYPE).get(new GenericType<List<Publication>>() {});
		
		// check for correct publication
		Publication correctPublication = null;
		for (Publication publication: publications) {
			if (publication.getChannel().equalsIgnoreCase(publicationChannel)) {
				correctPublication = publication;
			}
		}
		
		return correctPublication;
	}
	
	
	/**
	 * @param filepath the filepath of a file which will be send to opencast
	 * @param opencastId the opencast-event-id to which the file is added
	 */
	/*static void postFileToEvent(String filepath,String opencastId) {
		// TODO: change hardcoded to properties
		String eventEndpoint = OpencastApiCall.eventEndpoint + "/" + opencastId;
		
		// prepare the api call
		WebTarget target = prepareApiCall(eventEndpoint);
		
		// multipart bodyreader must be initiated
        target.register(MultiPartFeature.class);
				
		// create the multipart form data
		FormDataMultiPart multiPart = new FormDataMultiPart();
		
		// create the parts necessary for the the request
		String processing = createProcessingJson();
		multiPart.bodyPart(new FormDataBodyPart("processing",processing));

		// the file is sent as a stream to the API to guarantee delivery of even huge files
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(filepath);
			multiPart.bodyPart(new StreamDataBodyPart("presenter",fileInputStream));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// send the post request
		Response response = target.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE));
		
		// TODO: error handling if (response.getHeader) ....
		
		// after sending the request, close the inputStream
		IOUtils.closeQuietly(fileInputStream);
	}*/

	
	/**
	 * Prepares the API call with authentication
	 * @param endpoint the endpoint which should be called
	 * @return a web target to connect to
	 */
	private static WebTarget prepareApiCall(String endpoint) {
		// TODO: change hardcoded to properties
		String url = "http://opencast1.rrz.uni-hamburg.de:8080/api/";
		String user = "admin";
		String password = "opencast";
		
		// authentication
		HttpAuthenticationFeature authentication = HttpAuthenticationFeature.basic(user,password);
		
		// create and configure REST consuming client
		Client client = ClientBuilder.newClient();
		client.register(authentication);
		client.property(ClientProperties.CHUNKED_ENCODING_SIZE, 1024);
		client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
				
		WebTarget target = client.target(url).path(endpoint);
		
		return target;
	}
	
	
	/**
	 * Creates an opencast-compatible acl json string
	 * @return the processing info as as json string
	 */
	private static String createAclJson() {
		// default acl
		ArrayList<Map<String,String>> acl = new ArrayList<Map<String,String>>();
		// default acl
		Map<String,String> acl1 = new HashMap<String,String>();
		acl1.put("action", "write");
		acl1.put("role", "ROLE_ADMIN");
		
		Map<String,String> acl2 = new HashMap<String,String>();
		acl2.put("action", "read");
		acl2.put("role", "ROLE_USER");
		
		acl.add(acl1);
		acl.add(acl2);
		
		String aclAsJson = null;
		try {
			aclAsJson = new ObjectMapper().writeValueAsString(acl);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return aclAsJson;
	}
	
	
	/**
	 * Creates an opencast-compatible processing json string
	 * @return the processing info as as json string
	 */
	private static String createProcessingJson(Long id) {
		// TODO: do not use hardcoded values (properties?)
		Map<String, Object> processing = new HashMap<String,Object>();
		processing.put("workflow","l2go-adaptive-publish");
		Map<String, String> configuration = new HashMap<String,String>();
		configuration.put("sourceId",id.toString());
		processing.put("configuration",configuration);
		
		String processingAsJson = null;
		try {
			processingAsJson = new ObjectMapper().writeValueAsString(processing);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		return processingAsJson;
	}
	
	/**
	 * Creates a simplied version of the opencast dublincore metadata to transfer
	 * title and id to opencast
	 * @param title the title of the video
	 * @param id the id of the video
	 * @return a serialized json object of the metadata
	 */
	private static String createMetadataJson(String title, Long id) {
		ArrayList<Map<String,Object>> metadataList = new ArrayList<Map<String,Object>>();
		
		Map<String, Object> metadata = new HashMap<String,Object>();
		metadata.put("flavor","dublincore/episode");
		List<Map<String,String>> fields = new ArrayList<Map<String,String>>();
		Map<String,String> field1 = new HashMap<String,String>();
		field1.put("id","title");
		field1.put("value",title);
		Map<String,String> field2 = new HashMap<String,String>();
		field2.put("id","identifier");
		field2.put("value",String.valueOf(id));

		fields.add(field1);
		fields.add(field2);
		
		metadata.put("fields", fields);
		
		metadataList.add(metadata);

		String metadataListAsJson = null;
		try {
			metadataListAsJson = new ObjectMapper().writeValueAsString(metadataList);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return metadataListAsJson;
	}
	
}
