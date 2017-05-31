package de.uhh.l2g.webservices.videoprocessor.util;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.uhh.l2g.webservices.videoprocessor.model.CreatedVideo;

public class SmilBuilder {
	// starting XML DOM
	public static void buildSmil(String filePath, List<CreatedVideo> videos) throws ParserConfigurationException, TransformerException {
		// sort the video list with width descending for a better organization of the smil file (with an anonymous class)
		Collections.sort(videos, new Comparator<CreatedVideo>() {
		    @Override
		    public int compare(CreatedVideo v1, CreatedVideo v2) {
		        return Integer.valueOf(v2.getWidth()).compareTo(Integer.valueOf(v1.getWidth()));
		    }
		});
		
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
		Document doc = documentBuilder.newDocument();
		// create root element
		Element rootElement = doc.createElement("smil");
		doc.appendChild(rootElement);
		
		// head node
		Element headsElement = doc.createElement("head");
		// the head node is empty
		rootElement.appendChild(headsElement);
		
		// body node
		Element bodyElement = doc.createElement("body");
		rootElement.appendChild(bodyElement);

		// switch node
		Element switchElement = doc.createElement("switch");
		bodyElement.appendChild(switchElement);
		
		// video elements
		for (CreatedVideo video: videos) {
			Element videoElement = doc.createElement("switch");
			videoElement.setAttribute("src", video.getFilename());
			videoElement.setAttribute("height", String.valueOf(video.getHeight()));
			videoElement.setAttribute("width", String.valueOf(video.getWidth()));
			// if there a seperate video and audio-bitrate is given use those
			if ((video.getBitrateAudio() > 0) && (video.getBitrateVideo()) > 0) {
				Element videoParamElement = SmilBuilder.createParam("videoBitrate",video.getBitrateVideo(),doc);
				videoElement.appendChild(videoParamElement);
				Element audioParamElement = SmilBuilder.createParam("audioBitrate",video.getBitrateAudio(),doc);
				videoElement.appendChild(audioParamElement);
			} else {
				// otherwise use the simple version with a overall bitrate
				videoElement.setAttribute("system-bitrate", String.valueOf(video.getBitrate()));
			}
			// add the video element to the switch node
			switchElement.appendChild(videoElement);
		}

		// prepare writing of xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(filePath));
		// indent
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		
		// write the file
		transformer.transform(source, result);
	}

	private static Element createParam(String name, int bitrate, Document doc) {
		Element paramElement = doc.createElement("param");
		paramElement.setAttribute("name", name);
		paramElement.setAttribute("value", String.valueOf(bitrate));
		paramElement.setAttribute("valuetype", "data");
		return paramElement;
	}
}