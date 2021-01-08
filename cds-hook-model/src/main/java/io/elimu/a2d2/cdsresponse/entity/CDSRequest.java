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

package io.elimu.a2d2.cdsresponse.entity;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class CDSRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private String hookInstance;

	private String fhirServer;

	private Map<String, Object> fhirAuthorization;

	private String hook;

	private String redirect;

	private String user;

	private Map<String, Object> context;

	private String patient;

	private Map<String, Object> prefetch;

	public String getHookInstance() {
		return hookInstance;
	}

	public void setHookInstance(String hookInstance) {
		this.hookInstance = hookInstance;
	}

	public String getFhirServer() {
		return fhirServer;
	}

	public void setFhirServer(String fhirServer) {
		this.fhirServer = fhirServer;
	}

	public Map<String, Object> getFhirAuthorization() {
		return fhirAuthorization;
	}

	public void setFhirAuthorization(Map<String, Object> fhirAuthorization) {
		this.fhirAuthorization = fhirAuthorization;
	}

	public String getHook() {
		return hook;
	}

	public void setHook(String hook) {
		this.hook = hook;
	}

	public String getRedirect() {
		return redirect;
	}

	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}



	public Map<String, Object> getContext() {
		return context;
	}

	public void setContext(Map<String, Object> context) {
		this.context = context;
	}

	public Map<String, Object> getPrefetch() {
		return prefetch;
	}

	public void setPrefetch(Map<String, Object> prefetch) {
		this.prefetch = prefetch;
	}



	public String getPatient() {
		return patient;
	}

	public void setPatient(String patient) {
		this.patient = patient;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		result = prime * result + ((fhirServer == null) ? 0 : fhirServer.hashCode());
		result = prime * result + ((hook == null) ? 0 : hook.hashCode());
		result = prime * result + ((hookInstance == null) ? 0 : hookInstance.hashCode());
		result = prime * result + ((patient == null) ? 0 : patient.hashCode());
		result = prime * result + ((prefetch == null) ? 0 : prefetch.hashCode());
		result = prime * result + ((redirect == null) ? 0 : redirect.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CDSRequest other = (CDSRequest) obj;
		return new EqualsBuilder().appendSuper(super.equals(obj)).append(context, other.context)
				.append(fhirServer, other.fhirServer).append(hook, other.hook).append(hookInstance, other.hookInstance)
				.append(patient, other.patient).append(prefetch, other.prefetch).append(redirect, other.redirect)
				.append(user, other.user).isEquals();
	}

	@Override
	public String toString() {
		return "CDSRequest [hookInstance=" + hookInstance + ", fhirServer=" + fhirServer + ", hook=" + hook
				+ ", redirect=" + redirect + ", user=" + user + ", context=" + context + ", patient=" + patient
				+ ", prefetch=" + prefetch + "]";
	}



}
