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

package io.elimu.a2d2.genericmodel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Wrapper for a Service REST response that the process can access without any extra dependencies.
 */
public class ServiceResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The HTTP body of the response to be sent, as a String
	 */
	private String body;
	/**
	 * The HTTP headers to be sent in the response.
	 */
	private Map<String, List<String>> headers = new HashMap<>();
	/**
	 * THe HTTP status code of the response to be sent. Default value is 200 (OK)
	 */
	private Integer responseCode = 200; //unless provided with other value, response code is OK

	/**
	 * Default Constructor
	 */
	public ServiceResponse() {
	}

	/**
	 * Constructor in case of error
	 * @param errorMessage the error message to send
	 * @param code the HTTP status to send
	 */
	public ServiceResponse(String errorMessage, int code) {
		setBody(errorMessage);
		setResponseCode(code);
	}

	/**
	 * @return the body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * @param body the body to set
	 */
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * @return the headers
	 */
	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	/**
	 * Returns a specific previously set HTTP header by name. If more than one is set, it will return just the first value
	 * @param key the header to return
	 * @return the value of the HTTP header with the given key
	 */
	public String getHeader(String key) {
		List<String> header = getAllHeaderValues(key);
		if (header == null) {
			return null;
		}
		return header.get(0);
	}

	/**
	 * Returns all HTTP headers set with the given key, as a {@link List}
	 * @param key the name of the header to return
	 * @return a list of all the values for the given key. If only one value is set, it will return a list of one element.
	 */
	public List<String> getAllHeaderValues(String key) {
		return headers.get(key);
	}

	/**
	 * Adds an HTTP header
	 * @param key the name of the HTTP header
	 * @param value the value of the HTTP header
	 */
	public synchronized void addHeaderValue(String key, String value) {
		List<String> header = headers.get(key);
		if (header == null) {
			List<String> headerValsList = new ArrayList<String>();
			headerValsList.add(value);
			headers.put(key, headerValsList);
		} else {
			//modified in case header is an UnmodifiableList
			List<String> headerValsList = new ArrayList<String>(header);
			headerValsList.add(value);
			headers.put(key, headerValsList);
		}
	}

	/**
	 * @param headers the headers to set
	 */
	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}

	/**
	 * @return the responseCode
	 */
	public Integer getResponseCode() {
		return responseCode;
	}

	/**
	 * @param responseCode the responseCode to set.
	 */
	public void setResponseCode(Integer responseCode) {
		this.responseCode = responseCode;
	}
}
