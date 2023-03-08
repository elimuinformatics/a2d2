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

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class FhirClientWrapper {

	private static final Logger LOG = LoggerFactory.getLogger(FhirClientWrapper.class);
	
	private final IGenericClient wrap;
	private boolean inUse = false;
	private RequestDataInterceptor tracker;
	
	public FhirClientWrapper(IGenericClient client) {
		this.wrap = client;
	}
	
	public <T extends IBaseResource> T fetchResourceFromUrl(Class<T> theResourceType, String theUrl) {
		return wrap.fetchResourceFromUrl(theResourceType, theUrl);
	}

	public FhirContext getFhirContext() {
		return wrap.getFhirContext();
	}

	public void registerInterceptor(IClientInterceptor theInterceptor) {
		wrap.registerInterceptor(theInterceptor);
		if (theInterceptor instanceof RequestDataInterceptor) {
			this.tracker = (RequestDataInterceptor) theInterceptor;
		}
	}

	public void unregisterInterceptor(IClientInterceptor theInterceptor) {
		wrap.unregisterInterceptor(theInterceptor);
		if (theInterceptor instanceof RequestDataInterceptor) {
			this.tracker = null;
		}
	}

	public RequestDataInterceptor getTracker() {
		return tracker;
	}

	public IGenericClient unwrap() {
		return wrap;
	}
	
	public void inUse() {
		if (inUse) {
			LOG.warn("This FHIR Client class is in use while another endpoint hasn't finished using it!");
		}
		inUse = true;
	}
	
	public void notInUse() {
		inUse = false;
	}
}
