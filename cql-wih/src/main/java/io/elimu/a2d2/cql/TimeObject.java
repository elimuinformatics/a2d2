package io.elimu.a2d2.cql;

public class TimeObject<T> {

	private final long timestamp;
	private final T value;

	public TimeObject(T value) {
		this.timestamp = System.currentTimeMillis();
		this.value = value;
	}
	
	public long getAgeInMillis() {
		return System.currentTimeMillis() - timestamp;
	}
	
	public boolean olderThan(long millis) {
		return getAgeInMillis() > millis; 
	}
	
	public T getValue() {
		return value;
	}
}
