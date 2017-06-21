package de.uhh.l2g.webservices.videoprocessor.util;

import java.io.InputStream;
import java.util.Properties;

public class Config extends Properties {
	
    private static Config instance = null;

	private Config() {
	}
	
    public static Config getInstance() {
        if (instance == null) {
            try {
                instance = new Config();
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                InputStream input = classLoader.getResourceAsStream("config.properties");
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
