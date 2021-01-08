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

package io.elimu.a2d2.web;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.elimu.a2d2.genericmodel.ServiceRequest;
import io.elimu.a2d2.genericmodel.ServiceResponse;

public class WebUtils {

	private static final Logger logger = LoggerFactory.getLogger(WebUtils.class);

	private WebUtils() {
		throw new IllegalStateException("Utility class");
	}
	
	public static HttpServletRequest log(HttpServletRequest request) {
		RereadableHttpServletRequestWrapper wrapper = new RereadableHttpServletRequestWrapper(request);
		String body = wrapper.getBody();
		String method = request.getMethod();
		String path = request.getRequestURL().toString();
		String from = request.getRemoteAddr();
		Map<String, List<String>> headers = new HashMap<String, List<String>>();
		Map<String, List<String>> params = new HashMap<String, List<String>>();
		for (Enumeration<String> names = request.getHeaderNames(); names.hasMoreElements(); ) {
			String key = names.nextElement();
			for (Enumeration<String> values = request.getHeaders(key); values.hasMoreElements(); ) {
				List<String> headerValues = headers.get(key);
				if (headerValues == null) {
					headerValues = new ArrayList<String>();
					headers.put(key, headerValues);
				}
				headerValues.add(values.nextElement());
			}
		}
		for (Enumeration<String> names = request.getParameterNames(); names.hasMoreElements(); ) {
			String key = names.nextElement();
			for (String value : request.getParameterValues(key)) {
				List<String> paramValues = params.get(key);
				if (paramValues == null) {
					paramValues = new ArrayList<String>();
					headers.put(key, paramValues);
				}
				paramValues.add(value);
			}
		}
		StringBuilder sb = new StringBuilder("HTTPRequest ").append(method).
				append(" from ").append(from).append(" to url ").append(path);
		if (headers != null) {
			sb.append(" with headers ").append(headers);
		}
		if (params != null) {
			sb.append(" with parameters ").append(params);
		}
		if (body != null) {
			sb.append("with body:\n").append(body);
		}
		logger.debug(sb.toString());
		return wrapper;
	}

	public static void populate(HttpServletResponse response, ServiceResponse p) throws IOException {
		if (p.getHeaders() != null) {
			for (String key : p.getHeaders().keySet()) {
				for (String value : p.getAllHeaderValues(key)) {
					response.setHeader(key, value);
				}
			}
		}
		if (p.getResponseCode() != null) {
			response.setStatus(p.getResponseCode());
		}
		printBody(p.getBody(), response);
	}
	
	private static void printBody(String body, HttpServletResponse response) throws IOException {
		if (body != null) {
			byte[] data = body.getBytes();
			OutputStream output = response.getOutputStream();
			output.write(data);
			output.flush();
			output.close();
		}
	}

	public static ServiceRequest buildRequest(HttpServletRequest request, String serviceId) throws IOException {
		ServiceRequest retval = new ServiceRequest();
		retval.setBody(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
		retval.setMethod(request.getMethod());
		for (Enumeration<String> names = request.getHeaderNames(); names.hasMoreElements(); ) {
			String key = names.nextElement();
			for (Enumeration<String> values = request.getHeaders(key); values.hasMoreElements(); ) {
				retval.addHeaderValue(key.toLowerCase(), values.nextElement());
			}
		}
		for (Enumeration<String> names = request.getParameterNames(); names.hasMoreElements(); ) {
			String key = names.nextElement();
			for (String value : request.getParameterValues(key)) {
				retval.addParamValue(key, value);
			}
		}
		return retval;
	}
}
