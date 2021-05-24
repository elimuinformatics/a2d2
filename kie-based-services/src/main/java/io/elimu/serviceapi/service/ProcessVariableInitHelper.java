package io.elimu.serviceapi.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.elimu.a2d2.cdsmodel.Dependency;
import io.elimu.a2d2.process.ServiceUtils;

public class ProcessVariableInitHelper {

	private static final Logger log = LoggerFactory.getLogger(ProcessVariableInitHelper.class);

	public Map<String, Object> initVariables(Dependency dep) {
		Map<String, Object> serviceProperty = new HashMap<>();
		try {
			serviceProperty.putAll(readServiceProperties(dep));
			String envPrefix = System.getProperty("proc.envvar.prefix");
			if (envPrefix != null) {
				String prefix = envPrefix.toLowerCase() + "." + dep.getArtifactId().toLowerCase();
				String defaultPrefix = envPrefix.toLowerCase() + ".allservices";
				String keySet = System.getProperties().keySet().toString();
				log.trace("System properties available for loading are {}", keySet);
				System.getProperties().keySet().stream().
					filter(k -> k.toString().toLowerCase().startsWith(defaultPrefix)).
					forEach(k -> setServiceProperty(
						serviceProperty,
						k.toString().substring(defaultPrefix.length() + 1),
						System.getProperty(k.toString())
					));
				System.getProperties().keySet().stream().
					filter(k -> k.toString().toLowerCase().startsWith(prefix)).
					forEach(k -> setServiceProperty(
						serviceProperty, 
						k.toString().substring(prefix.length() +1), 
						System.getProperty(k.toString())
					));
			}
		} catch (Exception e) {
			log.error("Error reading properties for " + dep.getArtifactId(),e);
		}

		log.info("init Variable done for service ::" + dep.getArtifactId());
		return serviceProperty;
	}

	private void setServiceProperty(Map<String, Object> serviceProperty, String key, String value) {
		//value shouldn't be in the logs since it is possibly a secret
		log.info("setting env var {}", key); 
		serviceProperty.put(key, value);
	}

	private Map<String, Object> readServiceProperties(Dependency dep) throws ParseException, IOException {
		Map<String, Object> serviceProperty=new HashMap<>();

		URL jarPath = ServiceUtils.toJarPath(dep);
		Properties cds = new Properties();
		InputStream entry = ServiceUtils.readEntry(jarPath, "service.dsl");

		cds.load(entry);
		scanEntries(cds, serviceProperty, null);
		
		cds = new Properties();
		String envName = AppContextUtils.getInstance().getProfileName();
		InputStream entry2 = ServiceUtils.readEntry(jarPath, "service-" + envName + ".dsl");
		if (entry2 != null) {
			cds.load(entry2);
			scanEntries(cds, serviceProperty, envName);
		}
		return serviceProperty;
	}
	
	private void scanEntries(Properties cds, Map<String, Object> serviceProperty, String envName) throws ParseException {
		for (String key : cds.stringPropertyNames()) {
			if(key.toLowerCase().contains("proc.var") &&
					key.toLowerCase().endsWith("value")) {
				String value = cds.getProperty(key);
				Object object = castValueforKeyType(cds, key, value);
				String[] keys = key.split("\\.");
				if(keys.length == 4) {
					serviceProperty.put(keys[2], object);
					logMsg(envName, key);
				} else {
					logError(envName, key);
				}
			}
		}
	}

    private void logMsg(String envName, String key) {
    	if(!StringUtils.isEmpty(envName)) {
			log.debug("Populated {} properties for {}", envName, key);
		} else {
			log.debug("Populated properties for {}", key);
		}
    }
    
    private void logError(String envName, String key) {
    	if(!StringUtils.isEmpty(envName)) {
			log.error("Unexpected key entries {} for {} environment file", key, envName);
		} else {
			log.error("Unexpected key entries {}", key);
		}
    }

	private Object castValueforKeyType(Properties cds, String key, String value) throws ParseException{

		String searchString = key.replace(".value", ".type");

		for (String iterKey : cds.stringPropertyNames()) {
			if(iterKey.toLowerCase().contains(searchString)) {

				String valueType = cds.getProperty(iterKey);

				if(valueType.equalsIgnoreCase("int")) {
					return Integer.parseInt(value);
				}else if(valueType.equalsIgnoreCase("float")) {
					return Float.parseFloat(value);
				}else if(valueType.equalsIgnoreCase("Long")) {
					return Long.parseLong(value);
				}else if(valueType.equalsIgnoreCase("double")) {
					return Double.parseDouble(value);
				}else if(valueType.equalsIgnoreCase("date")) {
					DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					return format.parse(value);
				}else if(valueType.equalsIgnoreCase("bool") || valueType.equalsIgnoreCase("boolean")) {
					return Boolean.valueOf(value.toLowerCase());
				}else if(valueType.equalsIgnoreCase("string")){
					return value;
				}else {
					log.error("Unexpected type for key::{}", key);
				}
			}
		}

		return value;
	}
}
