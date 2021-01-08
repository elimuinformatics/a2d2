package io.elimu.genericapi.service;

/**
 * Encapsulates errors when the initialization of a service fails.
 */
public class GenericServiceConfigException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GenericServiceConfigException() {
		super();
	}

	public GenericServiceConfigException(String message, Throwable cause) {
		super(message, cause);
	}

	public GenericServiceConfigException(String message) {
		super(message);
	}

	public GenericServiceConfigException(Throwable cause) {
		super(cause);
	}
}
