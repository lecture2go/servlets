package de.uhh.l2g.webservices.videoprocessor.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;

/**
 * Util class to handle files (downloading, renaming, deleting,..)
 */
public class FileHandler {
	
	/**
	 * Downloads the file at the given URL to the given filepath
	 * @param sourceUrlString the URL from where the file will be downloaded
	 * @param targetPath the path where the file will be saved
	 * @throws IOException
	 */
	public static void download(String sourceUrlString, String targetPath) throws IOException {
		File target = new File(targetPath);
		URL sourceUrl = new URL(sourceUrlString);
		FileUtils.copyURLToFile(sourceUrl, target);
	}
	
	/**
	 * Renames the file
	 * @param sourcePath the current path
	 * @param targetPath the future path
	 * @throws IOException
	 */
	public static void rename(String sourcePath, String targetPath) throws IOException {
		FileUtils.moveFile(FileUtils.getFile(sourcePath), FileUtils.getFile(targetPath));
	}
	
	/**
	 * Deletes the file
	 * @param filePath the filePath of the file to delete
	 * @throws SecurityException
	 */
	public static void deleteIfExists(String filePath) throws SecurityException {
		FileUtils.getFile(filePath).delete();
	}

	/**
	 * Check if a file exists in the file system
	 * @param filePath the path to the file
	 * @return true if the file exists, false if not
	 */
	public static boolean checkIfFileExists(String filePath) {
		return (new File(filePath).isFile());
	}
	
	/**
	 * Copies the file
	 * @param sourcePath the current path
	 * @param targetPath the target path
	 * @throws IOException
	 */
	public static void copy(String sourcePath, String targetPath) throws IOException {
		FileUtils.copyFile(FileUtils.getFile(sourcePath), FileUtils.getFile(targetPath));
	}
	
	/**
	 * Creates a symlink from source file path to target file path
	 * @param sourceFilePath the source file path
	 * @param targetFilePath the target path
	 * @throws IOException
	 */
	public static void createSymlink(String sourceFilePath, String targetFilePath) throws IOException {

		File sourceFile = new File(sourceFilePath);
		File targetFile = new File(targetFilePath);

		if (targetFile.exists()) {
			Files.createSymbolicLink(sourceFile.toPath(), targetFile.toPath());
		}
	}
}
