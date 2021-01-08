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

public class ServiceRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The path of the request beyond the base URL of the service
	 */
	private String path;
	/**
	 * The header fields of the request as key value pairs
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
	 * The body of the request
	 */
	private String body;
	/**
	 * GET, PUT, POST, DELETE
	 */
	private String method;

	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}

	public String getHeader(String key) {
		List<String> header = getAllHeaderValues(key);
		if (header == null) {
			return null;
		}
		return header.get(0);
	}

	public List<String> getAllHeaderValues(String key) {
		return headers.get(key);
	}

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

	public Map<String, List<String>> getParams() {
		return params;
	}

	public String getParam(String key) {
		List<String> param = getAllParamValues(key);
		if (param == null) {
			return null;
		}
		return param.get(0);
	}

	public List<String> getAllParamValues(String key) {
		return params.get(key);
	}

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

	public void setParams(Map<String, List<String>> params) {
		this.params = params;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
