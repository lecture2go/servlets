package de.uhh.l2g.webservices.videoprocessor.util;

import org.apache.commons.io.FilenameUtils;

public class FilenameHandler {
	public static String switchBasename(String filePath, String newBasename) {
		String basePath = FilenameUtils.getFullPath(filePath);
		String fullFilename = newBasename + "." + FilenameUtils.getExtension(filePath);
		return FilenameUtils.concat(basePath, fullFilename);
	}
	
	public static String addToBasename(String filePath, String stringToAdd) {
		String basename = FilenameUtils.getBaseName(filePath);
		String newBasename = basename + stringToAdd;
		return switchBasename(filePath, newBasename);
	}
}
