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
import ca.uhn.fhir.rest.client.apache.ApacheHttpRequest;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;

public class SingleAuthInterceptor implements IClientInterceptor {

	private static final String AUTHORIZATION = "Authorization";
	
	@Override
	public void interceptRequest(IHttpRequest theRequest) {
		if (theRequest.getAllHeaders() != null) {
			List<String> values = theRequest.getAllHeaders().get(AUTHORIZATION);
			if (values == null) {
				return;
			}
			if (values.size() > 1) {
				String lastAuth = values.get(values.size() - 1);
				ApacheHttpRequest request = (ApacheHttpRequest) theRequest;
				request.getApacheRequest().removeHeaders(AUTHORIZATION);
				request.getApacheRequest().addHeader(AUTHORIZATION, lastAuth);
			}
		}
	}

	@Override
	public void interceptResponse(IHttpResponse theResponse) throws IOException {
		// do nothing
	}

}
