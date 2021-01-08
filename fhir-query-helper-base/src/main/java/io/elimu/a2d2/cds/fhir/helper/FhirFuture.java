package io.elimu.a2d2.cds.fhir.helper;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FhirFuture<T> {

	private static final Logger logger = LoggerFactory.getLogger(FhirFuture.class);
	
	private final Future<T> wrap;
	private final String name;

	public FhirFuture(String name, Future<T> wrap) {
		this.name = name;
		this.wrap = wrap;
	}

	public String getName() {
		return name;
	}

	public T get() {
		try {
			return wrap.get();
		} catch (Exception e) {
			logger.warn("Captured exception getting future", e);
			return null;
		}
	}
}
