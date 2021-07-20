package io.elimu.serviceapi.service;

import java.io.Serializable;

public class ResponseEvent<T> implements Serializable {

	private static final long serialVersionUID = 4585202206069885183L;

	private final T value;
	private final long timeout;

	public ResponseEvent() {
		this(null, VaultVariableInitHelper.RESPONSE_EVENT_TIMEOUT);
	}
	
	public ResponseEvent(T value) {
		this(value, VaultVariableInitHelper.RESPONSE_EVENT_TIMEOUT);
	}

	public ResponseEvent(T value, long timeout) {
		this.value = value;
		this.timeout = System.currentTimeMillis() + timeout;
	}

	public T getValue() {
		return value;
	}

	public long getTimeout() {
		return timeout;
	}

	public boolean isNew() {
		return System.currentTimeMillis() < timeout;
	}

}
