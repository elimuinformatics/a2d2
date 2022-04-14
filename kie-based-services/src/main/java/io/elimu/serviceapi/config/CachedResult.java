package io.elimu.serviceapi.config;

import java.util.Map;

public class CachedResult {

	private final long timestamp;
	private final Map<String, Object> variables;
	
	public CachedResult(Map<String, Object> variables) {
		this.timestamp = System.currentTimeMillis();
		this.variables = variables;
	}
	
	public Map<String, Object> getVariables() {
		return variables;
	}
	
	public boolean isOld() {
		return System.currentTimeMillis() - timestamp > 900_000;
	}
}
