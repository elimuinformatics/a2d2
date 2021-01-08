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
import java.util.List;
import java.util.Map;

public interface DataFetchService {

	Object fetchPatient(String patientMrId);

	default List<Object> fetchMedications(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchMedicationAdministrations(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchMedicationOrders(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchMedicationDispenses(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchMedicationRequests(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchMedicationStatement(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchAccounts(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchAdverseEvents(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchAllergyIntolerances(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchAppointments(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchAppointmentResponses(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchAuditEvents(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchClaims(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchClaimResponses(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchConditions(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchCoverages(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchDetectedIssues(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchDevices(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchDeviceComponents(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchEligibilityRequests(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchEligibilityResponses(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchEncounters(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchEnrollmentRequests(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchEnrollmentResponses(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchFamilyMemberHistories(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchFlags(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchGoals(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchGuidanceResponses(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchImmunizations(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchImmunizationRecommendations(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchNutritionOrders(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchProcedures(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchProcedureRequests(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchProcessRequests(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchProcessResponses(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchReferralRequests(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchSupplyDeliveries(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchSupplyRequests(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchTasks(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchTestReports(String patientMrId) {
		return new ArrayList<>();
	}

	default List<Object> fetchObservations(String patientMrId) {
		return new ArrayList<>();
	}

	void initParameters(String server, Map<String, Object> auth);
}
