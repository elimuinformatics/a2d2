package io.elimu.a2d2.cds.fhir.helper;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Future} extension that will be used for async calls to the FHIR server.
 * When the owner of this object will call {@link #get()} the thread will lock until the response
 * is returned from the FHIR server.
 * 
 * @param <T> the return type of the Future
 */
public class FhirFuture<T> {

	private static final Logger logger = LoggerFactory.getLogger(FhirFuture.class);
	
	private final Future<T> wrap;
	private final String name;

	public FhirFuture(String name, Future<T> wrap) {
		this.name = name;
		this.wrap = wrap;
	}

	/**
	 * The name provided as last parameter when calling {@link QueryingServerHelperBase#queryResourcesAsync(String, String, String, String, String)}
	 * @return the name provided
	 */
	public String getName() {
		return name;
	}

	/**
	 * It the Future is not completed, this call will lock the thead until the Future call is completed, then return its result.
	 * @return the result of the Future call
	 */
	public T get() {
		try {
			return wrap.get();
		} catch (Exception e) {
			logger.warn("Captured exception getting future", e);
			return null;
		}
	}
}
