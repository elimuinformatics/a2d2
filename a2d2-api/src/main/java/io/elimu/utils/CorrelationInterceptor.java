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


package io.elimu.utils;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.owasp.encoder.Encode;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

@Component
public class CorrelationInterceptor implements AsyncHandlerInterceptor {

	private static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-ID";
	private static final String CORRELATION_ID_LOG_VAR_NAME = "correlationId";

	@Override
	public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler)
			throws Exception {
		final String correlationId = Encode.forJava(getCorrelationIdFromHeader(request));
		MDC.put(CORRELATION_ID_LOG_VAR_NAME, correlationId);
		response.setHeader(CORRELATION_ID_HEADER_NAME, correlationId);
		return true;
	}

	@Override
	public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response,
			final Object handler, final Exception ex) throws Exception {
		MDC.remove(CORRELATION_ID_LOG_VAR_NAME);
	}
	
	private String getCorrelationIdFromHeader(final HttpServletRequest request) {
		String correlationId = request.getHeader(CORRELATION_ID_HEADER_NAME);
		if (StringUtils.isBlank(correlationId)) {
			correlationId = generateUniqueCorrelationId();
		}
		return correlationId;
	}

	private String generateUniqueCorrelationId() {
		return UUID.randomUUID().toString();
	}

}
