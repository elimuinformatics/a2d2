package io.elimu.a2d2.helpers;

import java.util.HashMap;
import java.util.Map;

public class SingletonHolder {

	private static final Map<String, Object> INSTANCES = new HashMap<>();
	
	public static Object getInstance(String key) {
		return INSTANCES.get(key);
	}
	
	public static synchronized boolean register(String key, Object instance) {
		if (INSTANCES.containsKey(key)) {
			return false;
		}
		INSTANCES.put(key, instance);
		return true;
	}
}
