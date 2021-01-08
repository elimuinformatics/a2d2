package io.elimu.serviceapi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.elimu.a2d2.cdsmodel.CDSLogger;

public class CDSLoggerImpl implements CDSLogger {

	private final Logger logger;
	
	public CDSLoggerImpl(String serviceId) {
		logger = LoggerFactory.getLogger(serviceId);
	}
	
	@Override
	public void info(String message) {
		logger.info(message);
	}

	@Override
	public void warn(String message) {
		logger.warn(message);
	}

	@Override
	public void error(String message) {
		logger.error(message);
	}

}
