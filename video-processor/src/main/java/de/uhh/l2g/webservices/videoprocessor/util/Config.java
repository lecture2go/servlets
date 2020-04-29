package de.uhh.l2g.webservices.videoprocessor.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.mchange.v2.io.FileUtils;

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
                if (instance.getProperty("external.config") !=null) {
                	try {
                		String externalConfigFilepath = instance.getProperty("external.config");
                    	InputStream externalConfig= new FileInputStream(externalConfigFilepath);
                    	instance.load(externalConfig);
                	} catch (Exception e) {
                		// external config could not be loaded, proceed
                	}
                	
                }
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return instance;
    }
	
	/**
	 * Sets property and saves it to the external properties file among the existing properties
	 * @param property the property to set and save
	 * @param value the value of the property to set and save
	 */
	public void setAndSaveProperty(String property, String value) {
		getInstance().setProperty(property, value);
        if (getInstance().getProperty("external.config") !=null) {
        	try {
        		// build a independent config to not parse all properties from included properties file
        		Config config = new Config();
        		// load all properties from external config file
        		String externalConfigFilepath = getInstance().getProperty("external.config");
        		// try to create external properties file if not existing
        		if (!FileHandler.checkIfFileExists(externalConfigFilepath)) {
        			File externalConfigFile = new File(externalConfigFilepath);
        			FileUtils.createEmpty(externalConfigFile);
        		}
            	InputStream externalConfig= new FileInputStream(externalConfigFilepath);

            	config.load(externalConfig);
        		
            	// set the property for new config 
        		config.setProperty(property, value);
 
        		OutputStream output = new FileOutputStream(instance.getProperty("external.config"));
        		config.store(output, "");
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
	}
	
}
