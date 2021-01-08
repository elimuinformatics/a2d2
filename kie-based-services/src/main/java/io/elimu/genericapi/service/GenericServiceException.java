package io.elimu.genericapi.service;

/**
 * Thrown from the service execution when an unexpected error, checked
 * or unchecked, is thrown by the rules and/or process execution.
 */
public class GenericServiceException extends Exception {

	private static final long serialVersionUID = 1L;

	public GenericServiceException() {
		super();
	}

	public GenericServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public GenericServiceException(String message) {
		super(message);
	}

	public GenericServiceException(Throwable cause) {
		super(cause);
	}
}
