package de.uhh.l2g.webservices.videoprocessor.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
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

import de.uhh.l2g.webservices.videoprocessor.model.opencast.publication.Publication;
import de.uhh.l2g.webservices.videoprocessor.model.opencast.Attachment;
import de.uhh.l2g.webservices.videoprocessor.model.opencast.Medium;
import de.uhh.l2g.webservices.videoprocessor.util.Config;



/**
 * Used for making calls to the opencast api
 */
public class OpencastApiCall {
	
	public static String eventEndpoint = "events";
	
	// the application configuration
	private static Config config = Config.getInstance();
	private static String user = config.getProperty("opencast.user");
	private static String password = config.getProperty("opencast.pass");


	/**
	 * Sends a post request to the opencast API events endpoint.
	 * This creates a new event on the opencast system with the given video file
	 * @param filepath the path of the file which will be converted
	 * @param title the title which will be used in the opencast system
	 * @param workflow the opencast workflow to run
	 * @param id the id
	 * @return the response from the opencast API
	 */
	static String postNewEventRequest(String filepath, String title, Long id, String workflow, Map<String, Object> additionalProperties) {
		// prepare the api call
		WebTarget target = prepareApiCall(eventEndpoint);
		
		// multipart bodyreader must be initiated
        target.register(MultiPartFeature.class);
				
		// create the parts necessary for the the request
		String acl = createAclJson();
		String metadata = createMetadataJson(title, id);
		String processing = createProcessingJson(id, workflow, additionalProperties);

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
	
	/**
	 * Gets a list of media for an opencast-Id from the opencast event endpoint
	 * @param opencastEventId the opencastEventId whose media are extracted
	 * @return a list of media objects
	 */
	public static List<Medium> getMedia(String opencastEventId) {
		String mediaEndpoint = eventEndpoint + "/" + opencastEventId + "/media";
		WebTarget target = prepareApiCall(mediaEndpoint);
		
		List<Medium> media = target.request(MediaType.APPLICATION_JSON_TYPE).get(new GenericType<List<Medium>>() {});
		
		return media;
	}
	
	/**
	 * Gets a list of ments for an opencast-Id from the opencast event endpoint
	 * @param opencastEventId the opencastEventId whose attachments are extracted
	 * @return a list of attachment objects
	 */
	public static List<Attachment> getAttachments(String opencastEventId) {
		String attachmentEndpoint = eventEndpoint + "/" + opencastEventId + "/asset/attachment/attachments.json";
		WebTarget target = prepareApiCall(attachmentEndpoint);
		
		List<Attachment> attachments = target.request(MediaType.APPLICATION_JSON_TYPE).get(new GenericType<List<Attachment>>() {});
		
		return attachments;
	}
	
	
	/**
	 * Get the publication object which all media included for an opencast-Id from the opencast event endpoint
	 * @param opencastEventId the opencastEventId whose publication is extracted 
	 * @param publicationChannel the publication channel name
	 * @return returns the publications with all media included
	 */
	public static Publication getPublication(String opencastEventId, String publicationChannel) {
		String publicationsEndpoint = eventEndpoint + "/" + opencastEventId + "/publications";
		WebTarget target = prepareApiCall(publicationsEndpoint);
		
		// saves a list of ob publications to a publications object
		List<Publication>publications = target.request(MediaType.APPLICATION_JSON_TYPE).get(new GenericType<List<Publication>>() {});
		
		// check for the publication with the given channel name
		Publication publicationForChannel = null;
		for (Publication publication: publications) {
			if (publication.getChannel().equalsIgnoreCase(publicationChannel)) {
				publicationForChannel = publication;
			}
		}
		
		return publicationForChannel;
	}
	
	
	/**
	 * Downloads a file from opencast using authentification
	 * @param remoteFilePath the url on the oc server
	 * @param targetFilePath the filepath on the own server
	 * @throws IOException
	 */
	public static void downloadFile(String remoteFilePath, String targetFilePath) throws IOException {
		WebTarget target = OpencastApiCall.prepareOcCall(remoteFilePath);
		
		// saves the file
		Response response = target.request().get();
		if(response.getStatus() == Response.Status.OK.getStatusCode()) {
			InputStream is = response.readEntity(InputStream.class);

			FileUtils.copyInputStreamToFile(is, new File(targetFilePath));

		}
	}
	
	public static boolean deleteEvent(String opencastEventId) throws NotFoundException, WebApplicationException {
		String eventEndpointForId = eventEndpoint + "/" + opencastEventId;
		WebTarget target = prepareApiCall(eventEndpointForId);
		
		Response response = target.request().accept(MediaType.APPLICATION_JSON).delete();
		switch (response.getStatus()) {
		case 204:
			// deletion was successfull
			return true;
		case 404:
			// event id not found, this may happen if the event was deleted within opencast itself
			return false;
		default:
			// other general error
			throw new WebApplicationException("deletion went wrong, http status from OC: " + response.getStatus());
		}
	}
	
	/**
	 * Prepares the API call
	 * @param endpoint the endpoint which should be called
	 * @return a web target to connect to
	 */
	private static WebTarget prepareApiCall(String endpoint) {
		String url = config.getProperty("opencast.url.api");
		
		WebTarget target = prepareOcCall(url);
		return target.path(endpoint);
	}
	
	
	/**
	 * Sets the authentication and client configuration for an call to the opencast server 
	 * @param url to connect to
	 * @return a WebTarget object which will be used for the connection
	 */
	private static WebTarget prepareOcCall(String url) {

		// authentication
		HttpAuthenticationFeature authentication = HttpAuthenticationFeature.basic(user,password);
		
		// create and configure REST consuming client
		Client client = ClientBuilder.newClient();
		client.register(authentication);
		client.property(ClientProperties.CHUNKED_ENCODING_SIZE, 1024);
		client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
				
		WebTarget target = client.target(url);
		
		return target;
	}
	
	
	/**
	 * Creates an opencast-compatible acl json string (used for a new event request)
	 * @return the processing info as as json string
	 */
	private static String createAclJson() {
		// default acl
		ArrayList<Map<String,String>> acl = new ArrayList<Map<String,String>>();
		// default acl
		Map<String,String> acl1 = new HashMap<String,String>();
		acl1.put("action", "write");
		acl1.put("role", config.getProperty("opencast.conversion.acl.write"));
		
		Map<String,String> acl2 = new HashMap<String,String>();
		acl2.put("action", "read");
		acl2.put("role", config.getProperty("opencast.conversion.acl.read"));
		
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
	 * Creates an opencast-compatible processing json string (used for a new event request)
	 * @param workflow 
	 * @return the processing info as as json string
	 */
	private static String createProcessingJson(Long id, String workflow, Map<String, Object> additionalProperties) {
		Map<String, Object> processing = new HashMap<String,Object>();
		processing.put("workflow", workflow);
		Map<String, String> configuration = new HashMap<String,String>();
		configuration.put("id",id.toString());
		// send url of this video-processor instance for callback
		configuration.put("url", config.getProperty("url.videoconversion"));
		
		// add additional properties if there are any
		if (!additionalProperties.isEmpty()) {
			for (Map.Entry<String, Object> additionalProperty : additionalProperties.entrySet()) {
				configuration.put(additionalProperty.getKey(), additionalProperty.getValue().toString());
			}
		}
	
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
	 * title and id to opencast  (used for a new event request)
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
