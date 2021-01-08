// Copyright 2018-2020 Elimu Informatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.elimu.a2d2.serviceapi.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogEntry {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogEntry.class);

	public enum LogLevel {
		TRACE, DEBUG, INFO, WARN, ERROR;
	}

	private LogLevel level = LogLevel.INFO;
	private String message = null;
	private Throwable error = null;

	public LogEntry(LogLevel level, String message) {
		this.level = level;
		this.message = message;
	}

	public LogEntry(LogLevel level, String message, Throwable error) {
		this(level, message);
		this.error = error;
	}

	public void execute() {
		switch (level) {
		case TRACE:
			if (LOGGER.isTraceEnabled()) {
				logTrace();
			}
			break;
		case DEBUG:
			if (LOGGER.isDebugEnabled()) {
				logDebug();
			}
			break;
		case INFO:
			if (LOGGER.isInfoEnabled()) {
				logInfo();
			}
			break;
		case WARN:
			if (LOGGER.isWarnEnabled()) {
				logWarn();
			}
			break;
		case ERROR:
			if (LOGGER.isErrorEnabled()) {
				logError();
			}
			break;
		default:
			message += " (NULL ERROR LEVEL)";
			logError();
		}
	}

	private void logTrace() {
		if (error == null) {
			LOGGER.trace(message);
		} else {
			LOGGER.trace(message, error);
		}
	}

	private void logDebug() {
		if (error == null) {
			LOGGER.debug(message);
		} else {
			LOGGER.debug(message, error);
		}
	}

	private void logInfo() {
		if (error == null) {
			LOGGER.info(message);
		} else {
			LOGGER.info(message, error);
		}
	}

	private void logError() {
		if (error == null) {
			LOGGER.error(message);
		} else {
			LOGGER.error(message, error);
		}
	}

	private void logWarn() {
		if (error == null) {
			LOGGER.warn(message);
		} else {
			LOGGER.warn(message, error);
		}
	}
}