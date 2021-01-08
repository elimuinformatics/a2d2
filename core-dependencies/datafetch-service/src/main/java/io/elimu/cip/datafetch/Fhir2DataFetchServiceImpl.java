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

package io.elimu.cip.datafetch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Account;
import ca.uhn.fhir.model.dstu2.resource.AllergyIntolerance;
import ca.uhn.fhir.model.dstu2.resource.Appointment;
import ca.uhn.fhir.model.dstu2.resource.AppointmentResponse;
import ca.uhn.fhir.model.dstu2.resource.AuditEvent;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Claim;
import ca.uhn.fhir.model.dstu2.resource.ClaimResponse;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.Coverage;
import ca.uhn.fhir.model.dstu2.resource.DetectedIssue;
import ca.uhn.fhir.model.dstu2.resource.Device;
import ca.uhn.fhir.model.dstu2.resource.DeviceComponent;
import ca.uhn.fhir.model.dstu2.resource.EligibilityRequest;
import ca.uhn.fhir.model.dstu2.resource.EligibilityResponse;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.EnrollmentRequest;
import ca.uhn.fhir.model.dstu2.resource.EnrollmentResponse;
import ca.uhn.fhir.model.dstu2.resource.FamilyMemberHistory;
import ca.uhn.fhir.model.dstu2.resource.Flag;
import ca.uhn.fhir.model.dstu2.resource.Goal;
import ca.uhn.fhir.model.dstu2.resource.Immunization;
import ca.uhn.fhir.model.dstu2.resource.ImmunizationRecommendation;
import ca.uhn.fhir.model.dstu2.resource.Medication;
import ca.uhn.fhir.model.dstu2.resource.MedicationAdministration;
import ca.uhn.fhir.model.dstu2.resource.MedicationDispense;
import ca.uhn.fhir.model.dstu2.resource.MedicationOrder;
import ca.uhn.fhir.model.dstu2.resource.MedicationStatement;
import ca.uhn.fhir.model.dstu2.resource.NutritionOrder;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Procedure;
import ca.uhn.fhir.model.dstu2.resource.ProcedureRequest;
import ca.uhn.fhir.model.dstu2.resource.ProcessRequest;
import ca.uhn.fhir.model.dstu2.resource.ProcessResponse;
import ca.uhn.fhir.model.dstu2.resource.ReferralRequest;
import ca.uhn.fhir.model.dstu2.resource.SupplyDelivery;
import ca.uhn.fhir.model.dstu2.resource.SupplyRequest;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.impl.BaseClient;

public class Fhir2DataFetchServiceImpl implements DataFetchService {

	private static final String VERSION_PROBLEM = "This resource type is only available in FHIR3. This is a FHIR2 implementation";

	private FhirContext ctx;
	private String baseUrl;
	private Map<String, Object> auth = new HashMap<>();

	public Fhir2DataFetchServiceImpl() {
		ctx = FhirContext.forDstu2();
		ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setAuth(Map<String, Object> fhirAuth) {
		this.auth = fhirAuth;
	}

	@Override
	public Object fetchPatient(String patientMrId) {
		IGenericClient client = ctx.newRestfulGenericClient(baseUrl);
		client.setEncoding(EncodingEnum.JSON);
		((BaseClient) client).setDontValidateConformance(true);
		if (auth != null && !auth.isEmpty()) {
			client.registerInterceptor(new AuthHeaderInterceptor(TokenGen.generateHttpHeader(auth)));
		}
		Bundle patientBundle = client.fetchResourceFromUrl(Bundle.class, toPatientUrl(patientMrId));
		if (patientBundle == null || patientBundle.getEntry().isEmpty()) {
			return null;
		}
		Patient patient = (Patient) patientBundle.getEntryFirstRep().getResource();
		return patient;
	}

	@Override
	public List<Object> fetchMedicationOrders(String patientMrId) {
		return fetch(MedicationOrder.class, patientMrId);
	}

	@Override
	public List<Object> fetchAccounts(String patientMrId) {
		return fetch(Account.class, patientMrId);
	}

	@Override
	public List<Object> fetchAllergyIntolerances(String patientMrId) {
		return fetch(AllergyIntolerance.class, patientMrId);
	}

	@Override
	public List<Object> fetchAppointmentResponses(String patientMrId) {
		return fetch(AppointmentResponse.class, patientMrId);
	}

	@Override
	public List<Object> fetchAdverseEvents(String patientMrId) {
		throw new UnsupportedOperationException(VERSION_PROBLEM);
	}

	@Override
	public List<Object> fetchAppointments(String patientMrId) {
		return fetch(Appointment.class, patientMrId);
	}

	@Override
	public List<Object> fetchAuditEvents(String patientMrId) {
		return fetch(AuditEvent.class, patientMrId);
	}

	@Override
	public List<Object> fetchClaimResponses(String patientMrId) {
		return fetch(ClaimResponse.class, patientMrId);
	}

	@Override
	public List<Object> fetchClaims(String patientMrId) {
		return fetch(Claim.class, patientMrId);
	}

	@Override
	public List<Object> fetchConditions(String patientMrId) {
		return fetch(Condition.class, patientMrId);
	}

	@Override
	public List<Object> fetchCoverages(String patientMrId) {
		return fetch(Coverage.class, patientMrId);
	}

	@Override
	public List<Object> fetchDetectedIssues(String patientMrId) {
		return fetch(DetectedIssue.class, patientMrId);
	}

	@Override
	public List<Object> fetchDeviceComponents(String patientMrId) {
		return fetch(DeviceComponent.class, patientMrId);
	}

	@Override
	public List<Object> fetchDevices(String patientMrId) {
		return fetch(Device.class, patientMrId);
	}

	@Override
	public List<Object> fetchEligibilityRequests(String patientMrId) {
		return fetch(EligibilityRequest.class, patientMrId);

	}

	@Override
	public List<Object> fetchEligibilityResponses(String patientMrId) {
		return fetch(EligibilityResponse.class, patientMrId);
	}

	@Override
	public List<Object> fetchEncounters(String patientMrId) {
		return fetch(Encounter.class, patientMrId);
	}

	@Override
	public List<Object> fetchEnrollmentRequests(String patientMrId) {
		return fetch(EnrollmentRequest.class, patientMrId);
	}

	@Override
	public List<Object> fetchEnrollmentResponses(String patientMrId) {
		return fetch(EnrollmentResponse.class, patientMrId);
	}

	@Override
	public List<Object> fetchFamilyMemberHistories(String patientMrId) {
		return fetch(FamilyMemberHistory.class, patientMrId);
	}

	@Override
	public List<Object> fetchFlags(String patientMrId) {
		return fetch(Flag.class, patientMrId);
	}

	@Override
	public List<Object> fetchGoals(String patientMrId) {
		return fetch(Goal.class, patientMrId);
	}

	@Override
	public List<Object> fetchGuidanceResponses(String patientMrId) {
		throw new UnsupportedOperationException(VERSION_PROBLEM);
	}

	@Override
	public List<Object> fetchImmunizationRecommendations(String patientMrId) {
		return fetch(ImmunizationRecommendation.class, patientMrId);
	}

	@Override
	public List<Object> fetchImmunizations(String patientMrId) {
		return fetch(Immunization.class, patientMrId);
	}

	@Override
	public List<Object> fetchMedicationAdministrations(String patientMrId) {
		return fetch(MedicationAdministration.class, patientMrId);
	}

	@Override
	public List<Object> fetchMedicationDispenses(String patientMrId) {
		return fetch(MedicationDispense.class, patientMrId);
	}

	@Override
	public List<Object> fetchMedicationRequests(String patientMrId) {
		throw new UnsupportedOperationException(VERSION_PROBLEM);
	}

	@Override
	public List<Object> fetchMedications(String patientMrId) {
		return fetch(Medication.class, patientMrId);
	}

	@Override
	public List<Object> fetchMedicationStatement(String patientMrId) {
		return fetch(MedicationStatement.class, patientMrId);
	}

	@Override
	public List<Object> fetchNutritionOrders(String patientMrId) {
		return fetch(NutritionOrder.class, patientMrId);
	}

	@Override
	public List<Object> fetchObservations(String patientMrId) {
		return fetch(Observation.class, patientMrId);
	}

	@Override
	public List<Object> fetchProcedureRequests(String patientMrId) {
		return fetch(ProcedureRequest.class, patientMrId);
	}

	@Override
	public List<Object> fetchProcedures(String patientMrId) {
		return fetch(Procedure.class, patientMrId);
	}

	@Override
	public List<Object> fetchProcessRequests(String patientMrId) {
		return fetch(ProcessRequest.class, patientMrId);
	}

	@Override
	public List<Object> fetchProcessResponses(String patientMrId) {
		return fetch(ProcessResponse.class, patientMrId);
	}

	@Override
	public List<Object> fetchReferralRequests(String patientMrId) {
		return fetch(ReferralRequest.class, patientMrId);
	}

	@Override
	public List<Object> fetchSupplyDeliveries(String patientMrId) {
		return fetch(SupplyDelivery.class, patientMrId);
	}

	@Override
	public List<Object> fetchSupplyRequests(String patientMrId) {
		return fetch(SupplyRequest.class, patientMrId);
	}

	@Override
	public List<Object> fetchTasks(String patientMrId) {
		throw new UnsupportedOperationException(VERSION_PROBLEM);
	}

	@Override
	public List<Object> fetchTestReports(String patientMrId) {
		throw new UnsupportedOperationException(VERSION_PROBLEM);
	}

	private String toPatientUrl(String patientIdentifier) {
		return String.format("/Patient?identifier=%s%s",
				"urn%3Aoid%3A0.1.2.3.4.5.6.7%7C", patientIdentifier);
	}

	protected List<Object> fetch(Class<?> clz, String patientMrId) {
		Patient patient = (Patient) fetchPatient(patientMrId);
		if (patient == null) {
			return new ArrayList<>();
		}
		IGenericClient client = ctx.newRestfulGenericClient(baseUrl);
		client.setEncoding(EncodingEnum.JSON);
		if (auth != null && !auth.isEmpty()) {
			client.registerInterceptor(new AuthHeaderInterceptor(TokenGen.generateHttpHeader(auth)));
		}
		Bundle bundle = client.fetchResourceFromUrl(Bundle.class,
				String.format("/%s?patient=%s", clz.getSimpleName(), patient.getIdElement().getIdPart()));
		if (bundle == null) {
			return new ArrayList<>();
		}
		List<Bundle.Entry> entries = bundle.getEntry();
		if (entries == null) {
			 return new ArrayList<>();
		}
		List<Object> retval = new ArrayList<>(entries.size());
		entries.forEach(e -> retval.add(e.getResource()));
		return retval;
	}

	@Override
	public void initParameters(String server, Map<String, Object> auth) {
		this.baseUrl = server;
		this.auth = auth;
	}
}
