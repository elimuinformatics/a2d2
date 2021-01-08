package io.elimu.serviceapi.service;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.springframework.core.env.Environment;

public class AppContextUtils {

	private static final AppContextUtils INSTANCE = new AppContextUtils();

	private Environment environment;
	private String testEnvName = null; //for use on unit tests only
	
	private AppContextUtils() {
	}

	public static AppContextUtils getInstance() {
		return INSTANCE;
	}
	
	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
	
	public String getProfileName() {
		if (testEnvName != null) {
			return testEnvName;
		}
		List<String> profiles = new LinkedList<>(Arrays.asList(getEnvironment().getActiveProfiles()));
		profiles.removeIf(e -> "default".equals(e));
		profiles.removeIf(e -> "test".equals(e));
		return profiles.iterator().next();
	}

	//For use from unit tests only
	public void testSetEnvironmentName(String envName) {
		this.testEnvName = envName;
	}
}
