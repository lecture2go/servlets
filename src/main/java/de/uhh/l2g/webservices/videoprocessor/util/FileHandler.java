package de.uhh.l2g.webservices.videoprocessor.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

public class FileHandler {
	public static void download(String sourceUrlString, String targetPath) {
		File target = new File(targetPath);
		try {
			URL sourceUrl = new URL(sourceUrlString);
			FileUtils.copyURLToFile(sourceUrl, target);
		} catch (MalformedURLException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void rename(String sourcePath, String targetPath) {
		try {
			FileUtils.moveFile(FileUtils.getFile(sourcePath), FileUtils.getFile(targetPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void delete(String filePath) {
		if (!FileUtils.deleteQuietly(FileUtils.getFile(filePath))) {
			// TODO delete gone wrong
		}
	}
}
