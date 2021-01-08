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

public class ServiceResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private String body;
	private Map<String, List<String>> headers = new HashMap<>();
	private Integer responseCode = 200; //unless provided with other value, response code is OK

	public ServiceResponse() {
	}

	public ServiceResponse(String errorMessage, int code) {
		setBody(errorMessage);
		setResponseCode(code);
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Map<String, List<String>> getHeaders() {
		return headers;
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

	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}

	public Integer getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(Integer responseCode) {
		this.responseCode = responseCode;
	}
}
