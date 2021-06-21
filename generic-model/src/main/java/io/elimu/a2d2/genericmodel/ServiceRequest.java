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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for a Service REST request that the process can access without any extra dependencies.
 */
public class ServiceRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The path of the request beyond the base URL of the service. i.e. if the service name is "service-a", and 
	 * the URL used to invoke the service ends with "/service-a/1234?param1=a", this path will contain "1234" in it
	 */
	private String path;
	/**
	 * The HTTP header fields of the request as key value pairs
	 */
	private Map<String, List<String>> headers = new HashMap<>();
	/**
	 * The URL parameters from the request as key value pairs
	 */
	private Map<String, List<String>> params = new HashMap<>();
	/**
	 * User obtained from the authentication.
	 */
	private String user;
	/**
	 * The body of the request, as a String
	 */
	private String body;
	/**
	 * possible values are all the HTTP methods available, but most commons will be GET, PUT, POST, DELETE
	 */
	private String method;

	/**
	 * @return the headers
	 */
	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	/**
	 * @param headers the headers to set
	 */
	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
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
			header = new ArrayList<String>();
			header.add(value);
			headers.put(key, header);
		} else {
			//modified in case header is an UnmodifiableList
			header = new ArrayList<String>(header);
			header.add(value);
			headers.put(key, header);
		}
	}

	/**
	 * @return the params
	 */
	public Map<String, List<String>> getParams() {
		return params;
	}

	/**
	 * Returns a specific previously set URL parameter by name. If more than one is set, it will return just the first value
	 * @param key the parameter to return
	 * @return the value of the URL parameter with the given key
	 */
	public String getParam(String key) {
		List<String> param = getAllParamValues(key);
		if (param == null) {
			return null;
		}
		return param.get(0);
	}

	/**
	 * Returns all URL parameters set with the given key, as a {@link List}
	 * @param key the name of the parameters to return
	 * @return a list of all the values for the given key. If only one value is set, it will return a list of one element.
	 */
	public List<String> getAllParamValues(String key) {
		return params.get(key);
	}

	/**
	 * Adds an URL parameter
	 * @param key the name of the URL parameter
	 * @param value the value of the URL parameter
	 */
	public synchronized void addParamValue(String key, String value) {
		List<String> param = params.get(key);
		if (param == null) {
			param = new ArrayList<String>();
			param.add(value);
			params.put(key, param);
		} else {
			param = new ArrayList<String>(param);
			param.add(value);
			params.put(key, param);
		}
	}

	/**
	 * @param params the params to set
	 */
	public void setParams(Map<String, List<String>> params) {
		this.params = params;
	}

	/**
	 * @deprecated it is meant to take the user from the HTTP principal of the HttpServletReqeust, but is not being set with OAuth or x-api-key. Do not use
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @deprecated it is meant to take the user from the HTTP principal of the HttpServletReqeust, but is not being set with OAuth or x-api-key. Do not use
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
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
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @param method the method to set
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}
}
