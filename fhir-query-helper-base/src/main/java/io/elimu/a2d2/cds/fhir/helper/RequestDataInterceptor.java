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

package io.elimu.a2d2.cds.fhir.helper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;

public class RequestDataInterceptor implements IClientInterceptor {

	private static final Logger log = LoggerFactory.getLogger(RequestDataInterceptor.class);

	private int responseStatusCode = -1;
	private String responseStatusInfo = "not invoked";

	public int getResponseStatusCode() {
		return responseStatusCode;
	}

	public String getResponseStatusInfo() {
		return responseStatusInfo;
	}

	@Override
	public void interceptRequest(IHttpRequest theRequest) {
		if (theRequest.getAllHeaders() != null) {
			for (Map.Entry<String, List<String>> entry : theRequest.getAllHeaders().entrySet()) {
				String value = null;
				if (entry.getValue() != null && entry.getValue().size() == 1) {
					value = entry.getValue().iterator().next();
				}
				if (entry.getValue() != null && entry.getValue().size() > 1) {
					value = entry.getValue().toString();
				}
				log.trace("HTTP request header " + entry.getKey() + ": " + value);
			}
		}
		if (theRequest.getHttpVerbName() != null) {
			log.trace("HTTP request method: " + theRequest.getHttpVerbName());
		}
		if (theRequest.getUri() != null) {
			log.trace("HTTP request URL: " + theRequest.getUri());
		}
	}

	@Override
	public void interceptResponse(IHttpResponse theResponse) throws IOException {
		if (theResponse.getAllHeaders() != null) {
			for (Map.Entry<String, List<String>> entry : theResponse.getAllHeaders().entrySet()) {
				String value = null;
				if (entry.getValue() != null && entry.getValue().size() == 1) {
					value = entry.getValue().iterator().next();
				}
				if (entry.getValue() != null && entry.getValue().size() > 1) {
					value = entry.getValue().toString();
				}
				log.trace("HTTP response header " + entry.getKey() + ": " + value);
			}
		}
		log.trace("HTTP response status: " + theResponse.getStatus());
		this.responseStatusCode = theResponse.getStatus();
		if (theResponse.getStatusInfo() != null) {
			log.trace("HTTP response status info: " + theResponse.getStatusInfo());
			this.responseStatusInfo = theResponse.getStatusInfo();
		}
	}

}
