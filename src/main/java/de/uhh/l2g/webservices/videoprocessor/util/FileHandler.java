package de.uhh.l2g.webservices.videoprocessor.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

public class FileHandler {
	public static void download(String sourceUrlString, String targetPath) throws IOException {
		File target = new File(targetPath);
		URL sourceUrl = new URL(sourceUrlString);
		FileUtils.copyURLToFile(sourceUrl, target);
	}
	
	public static void rename(String sourcePath, String targetPath) throws IOException {
		FileUtils.moveFile(FileUtils.getFile(sourcePath), FileUtils.getFile(targetPath));
	}
	
	public static void delete(String filePath) {
		if (!FileUtils.deleteQuietly(FileUtils.getFile(filePath))) {
			// TODO delete gone wrong
		}
	}
}
