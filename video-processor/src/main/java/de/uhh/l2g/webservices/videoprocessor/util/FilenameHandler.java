package de.uhh.l2g.webservices.videoprocessor.util;

import org.apache.commons.io.FilenameUtils;

/**
 * Util class to handle filename handling
 *
 */
public class FilenameHandler {
	
	/**
	 * Switch the basename of a file
	 * e.g. /test/test123.mp4 has the basename test123, this part will be replaced by the new basename
	 * 
	 * @param filePath the file path of the file
	 * @param newBasename the new basename of the file
	 * @return the file path with the new basename
	 */
	public static String switchBasename(String filePath, String newBasename) {
		String basePath = FilenameUtils.getFullPath(filePath);
		String fullFilename = newBasename + "." + FilenameUtils.getExtension(filePath);
		return FilenameUtils.concat(basePath, fullFilename);
	}
	
	/**
	 * Add a suffix to the basename of a filePath
	 * @param filePath the file path of the file
	 * @param stringToAdd the string which will be added to the basename
	 * @return the file path with the basename including the added string
	 */
	public static String addToBasename(String filePath, String stringToAdd) {
		String basename = FilenameUtils.getBaseName(filePath);
		String newBasename = basename + stringToAdd;
		return switchBasename(filePath, newBasename);
	}
	
	/**
	 * Switch extension of a given filepath
	 * @param filePath the file path of the file
	 * @param extension the new extension
	 * @return the file path with the filepath and the new extension
	 */
	public static String switchExtension(String filePath, String extension) {
		String basePath = FilenameUtils.getFullPath(filePath);
		String basename = FilenameUtils.getBaseName(filePath);
		String fullFilename = basename + "." + extension;
		return FilenameUtils.concat(basePath, fullFilename);
	}
}
