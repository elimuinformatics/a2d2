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

public class FhirResponse<T> implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 4205440029216884021L;


	private final T result;
	private int responseStatusCode;
	private String responseStatusInfo;

	public FhirResponse(T result, int responseStatusCode, String responseStatusInfo) {
		this.result = result;
		this.responseStatusCode = responseStatusCode;
		this.responseStatusInfo = responseStatusInfo;
	}

	public T getResult() {
		return result;
	}

	public int getResponseStatusCode() {
		return responseStatusCode;
	}

	public String getResponseStatusInfo() {
		return responseStatusInfo;
	}

	public void setResponseStatusCode(int responseStatusCode) {
		this.responseStatusCode = responseStatusCode;
	}

	public void setResponseStatusInfo(String responseStatusInfo) {
		this.responseStatusInfo = responseStatusInfo;
	}

}
