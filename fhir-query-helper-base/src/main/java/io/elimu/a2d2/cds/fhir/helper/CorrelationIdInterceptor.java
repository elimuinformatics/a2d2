package io.elimu.a2d2.cds.fhir.helper;

import org.slf4j.MDC;

import ca.uhn.fhir.rest.client.interceptor.SimpleRequestHeaderInterceptor;

public class CorrelationIdInterceptor extends SimpleRequestHeaderInterceptor {

	private static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-ID";
	private static final String CORRELATION_ID_LOG_VAR_NAME = "correlationId";

	public CorrelationIdInterceptor() {
		super(CORRELATION_ID_HEADER_NAME, MDC.get(CORRELATION_ID_LOG_VAR_NAME));
	}

}
