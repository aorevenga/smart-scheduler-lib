package com.revenga.rits.smartscheduler.lib.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.exception.ExceptionUtils;

import lombok.extern.log4j.Log4j2;

/**

 * 
 * Get the value of the property from file and property key
 * Files with properties are mapped on first use
 * 
 * @author aborrajo
 *
 */
@Log4j2
public class ConfigProperty {

	private static ConfigProperty instance;
	private Map<String, Properties> properties = new HashMap<>();
	
	public static final Boolean TRUE_TO_BOOL = true;
	public static final Boolean FALSE_TO_BOOL = true;
	
	public static final String TRUE_TO_STRING = "true";
	public static final String FALSE_TO_STRING = "false";
	
	public static final Integer TRUE_TO_INTEGER = 1;
	public static final Integer FALSE_TO_INTEGER = 0;

	public static ConfigProperty getInstance() {

		if (instance == null) {
			instance = new ConfigProperty();
		}

		return instance;
	}

	private ConfigProperty() {

		super();
	}

	/**
	 * 
	 * Get the value of property in file
	 * 
	 * @param file
	 * @param section
	 * @param property
	 * 
	 * @return String value of property
	 * 
	 * @throws IOException 
	 */
	public String getProperty(String file, String key) {

		String propValue = null;
		Properties prop = null;

		try {

			prop = properties.get(file);

			if (prop == null) {
				
				InputStream is = null;
					
				prop = new Properties();
				is = this.getClass().getClassLoader().getResourceAsStream(file);
				
				if (is == null) {
				
					is = ResourcesUtil.getResource(file);
				}
				
				if (is != null) {
					
					prop.load(is);
				
					properties.put(file, prop);
				}					
			}

			propValue = prop.getProperty(key);
			
		} catch (IOException e) {

			log.error(e.getMessage());
			log.debug(ExceptionUtils.getStackTrace(e));
		}

		return propValue;
	}

	public Map<String, String> getProperties(String file) {
		
		String key = null;
		String value = null;
		Properties prop = null;
		Map<String, String> hash = new HashMap<>();
		

		try {

			prop = properties.get(file);
			
			if (prop == null) {
				
				InputStream is = null;
					
				prop = new Properties();
				is = this.getClass().getClassLoader().getResourceAsStream(file);
				
				if (is == null) {
				
					is = ResourcesUtil.getResource(file);
				}
				
				if (is != null) {
					
					prop.load(is);
				
					properties.put(file, prop);
				}					
			}

			Enumeration<Object> em = prop.keys();
			
			while (em.hasMoreElements()) {

				key = (String) em.nextElement();
				value = prop.getProperty(key);
				hash.put(key, value);
			}

		} catch (Exception e) {

			log.error(e.getMessage());
			log.debug(ExceptionUtils.getStackTrace(e));
		}
		
		return hash;
	}
}
