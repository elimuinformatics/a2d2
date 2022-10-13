package io.elimu.a2d2.cql;

import java.util.HashMap;
import java.util.Map;

public class TimeoutMap<K, V> extends HashMap<K, V> {

	private static final long serialVersionUID = 1L;
	
	private final Map<K, Long> timeouts = new HashMap<>();
	private final long maxAgeCache;

	public TimeoutMap(long maxAgeCache) {
		this.maxAgeCache = maxAgeCache;
	}

	@Override
	public V get(Object key) {
		Long startTime = timeouts.get(key);
		if (startTime != null && System.currentTimeMillis() - startTime.longValue() > maxAgeCache) {
			remove(key);
			return null;
		}
		return super.get(key);
	}
	
	@Override
	public V put(K key, V value) {
		timeouts.put(key, System.currentTimeMillis());
		return super.put(key, value);
	}
	
	@Override
	public V putIfAbsent(K key, V value) {
		timeouts.putIfAbsent(key, System.currentTimeMillis());
		return super.putIfAbsent(key, value);
	}
	
	@Override
	public V getOrDefault(Object key, V defaultValue) {
		Long startTime = timeouts.get(key);
		if (startTime != null && System.currentTimeMillis() - startTime.longValue() > maxAgeCache) {
			remove(key);
			return defaultValue;
		}
		return super.getOrDefault(key, defaultValue);
	}
	
	@Override
	public V remove(Object key) {
		timeouts.remove(key);
		return super.remove(key);
	}
}
