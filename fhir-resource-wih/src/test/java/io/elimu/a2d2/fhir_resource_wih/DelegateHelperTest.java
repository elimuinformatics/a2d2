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

package io.elimu.a2d2.fhir_resource_wih;

import org.hl7.fhir.instance.model.api.IBaseResource;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
//import ca.uhn.fhir.model.dstu2.resource.Patient;
//import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.impl.BaseClient;

public class DelegateHelperTest {

	private static transient FhirContext ctx = FhirContext.forDstu2();
	private static IGenericClient client;
	private static String fhirUrl = null;

	public static void setUrl(String url) {
		fhirUrl = url;
	}

/*	public static Patient createFHIR2TestPatient() {
		Patient patient = new Patient();
		patient.addIdentifier().setSystem("urn:oid:0.1.2.3.4.5.6.7").setValue("565656");
		patient.addName().addFamily("Snow").addGiven("John");
		patient.setGender(AdministrativeGenderEnum.MALE);
		return (Patient) createResource(patient);
	}
*/
	public static void deleteResourceByPatient(String patientId) {
		client.delete().resourceById(new IdDt("Patient", patientId)).execute();
	}

	public static IBaseResource createResource(IResource resource) {
		client = ctx.newRestfulGenericClient(fhirUrl);
		client.setEncoding(EncodingEnum.JSON);
		((BaseClient) client).setDontValidateConformance(true);
		MethodOutcome outcome = client.create().resource(resource).execute();
		String resourceId = outcome.getId().getIdPartAsLong().toString();
		return client.read().resource(resource.getResourceName()).withId(resourceId).execute();
	}

}
