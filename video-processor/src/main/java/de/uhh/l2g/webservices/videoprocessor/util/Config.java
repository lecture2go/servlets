package de.uhh.l2g.webservices.videoprocessor.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * This class reads and handles the config properties from the config file
 */
public class Config extends Properties {
	
    private static Config instance = null;
    private static String configFilename = "config.properties";

	private Config() {
	}
	
	/**
	 * This config is a singleton to avoid reading from the config file every time 
	 * @return the single instance of the Config class
	 */
	public static Config getInstance() {
        if (instance == null) {
            try {
                instance = new Config();
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                InputStream input = classLoader.getResourceAsStream(configFilename);
                instance.load(input);
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return instance;
    }
}
