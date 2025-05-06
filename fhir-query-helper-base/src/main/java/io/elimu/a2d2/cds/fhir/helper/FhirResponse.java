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

import java.io.Serializable;

/**
 * Wrapper for an HTTP response received from a FHIR server. 
 *
 * @param <T> the Resource specific class, depending on the FHIR version
 */
public class FhirResponse<T> implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 4205440029216884021L;

	/**
	 * The result object, if the {@link #responseStatusCode} is 200
	 */
	private final T result;
	/**
	 * The HTTP status received from the FHIR server
	 */
	private int responseStatusCode;
	/**
	 * The response string related to the HTTP status
	 */
	private String responseStatusInfo;
	
	private String responseBody;

	public FhirResponse(T result, int responseStatusCode, String responseStatusInfo) {
		this(result, responseStatusCode, responseStatusInfo, null);
	}

	public FhirResponse(T result, int responseStatusCode, String responseStatusInfo, String responseBody) {
		this.result = result;
		this.responseStatusCode = responseStatusCode;
		this.responseStatusInfo = responseStatusInfo;
		this.responseBody = responseBody;
	}
	
	/**
	 * @return the result object for the query
	 */
	public T getResult() {
		return result;
	}

	/**
	 * @return the HTTP status received from the FHIR server
	 */
	public int getResponseStatusCode() {
		return responseStatusCode;
	}

	/**
	 * @return the response string related to the HTTP status
	 */
	public String getResponseStatusInfo() {
		return responseStatusInfo;
	}

	/**
	 * @param responseStatusCode sets an HTTP status
	 */
	public void setResponseStatusCode(int responseStatusCode) {
		this.responseStatusCode = responseStatusCode;
	}

	/**
	 * @param responseStatusInfo sets an HTT status info
	 */
	public void setResponseStatusInfo(String responseStatusInfo) {
		this.responseStatusInfo = responseStatusInfo;
	}

	public String getResponseBody() {
		return responseBody;
	}
	
	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}
}
