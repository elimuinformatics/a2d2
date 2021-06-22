package io.elimu.a2d2.fhirresourcewih;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import ca.uhn.fhir.context.FhirContext;
import io.elimu.a2d2.exception.WorkItemHandlerException;
import io.elimu.a2d2.genericmodel.ServiceResponse;

public class OperationOutcomeWorkItemHandler implements WorkItemHandler {

	public static final String ISSUE_PREFIX = "issue_";
	
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String version = (String) workItem.getParameter("fhirVersion");
		Object status = workItem.getParameter("response_status");
		Set<String> issueIds = workItem.getParameters().keySet().stream().
				filter(e -> e.startsWith(ISSUE_PREFIX)).
				map(s -> s.replace(ISSUE_PREFIX, "")).
				map(s -> s.substring(0, s.indexOf("_"))).
				distinct().
				collect(Collectors.toSet());
		Object outcomeObj = null;
		String outcomeJson = null;
		if (version == null || "FHIR4".equalsIgnoreCase(version) || "R4".equalsIgnoreCase(version)) {
			org.hl7.fhir.r4.model.OperationOutcome outcome = new org.hl7.fhir.r4.model.OperationOutcome();
			for (String issueId : issueIds) {
				Map<String, Object> subset = extractIssueDetails(workItem.getParameters(), issueId);
				org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent issue = outcome.addIssue();
				if (subset.containsKey("code")) {
					String code = (String) subset.get("code");
					issue.setCode(org.hl7.fhir.r4.model.OperationOutcome.IssueType.fromCode(code));
				}
				if (subset.containsKey("details") || subset.containsKey("detailsCode") || subset.containsKey("detailsSystem")) {
					String details = (String) subset.get("details");
					String code = (String) subset.get("detailsCode");
					String system = (String) subset.get("detailsSystem");
					org.hl7.fhir.r4.model.CodeableConcept cc = new org.hl7.fhir.r4.model.CodeableConcept();
					if (code != null || system != null) {
						org.hl7.fhir.r4.model.Coding coding = cc.addCoding();
						if (code != null) {
							coding.setCode(code);
						}
						if (system != null) {
							coding.setSystem(system);
						}
					}
					if (details != null) {
						cc.setText(details);
					}
					issue.setDetails(cc);
				}
				if (subset.containsKey("diagnostics")) {
					issue.setDiagnostics((String) subset.get("diagnostics"));
				}
				if (subset.containsKey("location")) {
					issue.addLocation((String) subset.get("location"));
				}
				if (subset.containsKey("location0")) {
					for (int index = 0; subset.containsKey("location" + index); index++) {
						issue.addLocation((String) subset.get("location" + index));
					}
				}
				if (subset.containsKey("severity")) {
					String code = (String) subset.get("severity");
					issue.setSeverity(org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.fromCode(code));
				}
			}
			outcomeObj = outcome;
			outcomeJson = FhirContext.forR4().newJsonParser().encodeResourceToString(outcome);
		} else if ("FHIR3".equalsIgnoreCase(version) || "DSTU3".equalsIgnoreCase(version)) {
			org.hl7.fhir.dstu3.model.OperationOutcome outcome = new org.hl7.fhir.dstu3.model.OperationOutcome();
			for (String issueId : issueIds) {
				Map<String, Object> subset = extractIssueDetails(workItem.getParameters(), issueId);
				org.hl7.fhir.dstu3.model.OperationOutcome.OperationOutcomeIssueComponent issue = outcome.addIssue();
				if (subset.containsKey("code")) {
					String code = (String) subset.get("code");
					issue.setCode(org.hl7.fhir.dstu3.model.OperationOutcome.IssueType.fromCode(code));
				}
				if (subset.containsKey("details") || subset.containsKey("detailsCode") || subset.containsKey("detailsSystem")) {
					String details = (String) subset.get("details");
					String code = (String) subset.get("detailsCode");
					String system = (String) subset.get("detailsSystem");
					org.hl7.fhir.dstu3.model.CodeableConcept cc = new org.hl7.fhir.dstu3.model.CodeableConcept();
					if (code != null || system != null) {
						org.hl7.fhir.dstu3.model.Coding coding = cc.addCoding();
						if (code != null) {
							coding.setCode(code);
						}
						if (system != null) {
							coding.setSystem(system);
						}
					}
					if (details != null) {
						cc.setText(details);
					}
					issue.setDetails(cc);
				}
				if (subset.containsKey("diagnostics")) {
					issue.setDiagnostics((String) subset.get("diagnostics"));
				}
				if (subset.containsKey("location")) {
					issue.addLocation((String) subset.get("location"));
				}
				if (subset.containsKey("location0")) {
					for (int index = 0; subset.containsKey("location" + index); index++) {
						issue.addLocation((String) subset.get("location" + index));
					}
				}
				if (subset.containsKey("severity")) {
					String code = (String) subset.get("severity");
					issue.setSeverity(org.hl7.fhir.dstu3.model.OperationOutcome.IssueSeverity.fromCode(code));
				}
			}
			outcomeObj = outcome;
			outcomeJson = FhirContext.forDstu3().newJsonParser().encodeResourceToString(outcome);
		} else if ("FHIR2".equalsIgnoreCase(version) || "DSTU2".equalsIgnoreCase(version)) {
			ca.uhn.fhir.model.dstu2.resource.OperationOutcome outcome = new ca.uhn.fhir.model.dstu2.resource.OperationOutcome();
			for (String issueId : issueIds) {
				Map<String, Object> subset = extractIssueDetails(workItem.getParameters(), issueId);
				ca.uhn.fhir.model.dstu2.resource.OperationOutcome.Issue issue = outcome.addIssue();
				if (subset.containsKey("code")) {
					String code = (String) subset.get("code");
					issue.setCode(ca.uhn.fhir.model.dstu2.valueset.IssueTypeEnum.forCode(code));
				}
				if (subset.containsKey("details") || subset.containsKey("detailsCode") || subset.containsKey("detailsSystem")) {
					String details = (String) subset.get("details");
					String code = (String) subset.get("detailsCode");
					String system = (String) subset.get("detailsSystem");
					ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt cc = new ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt();
					if (code != null || system != null) {
						ca.uhn.fhir.model.dstu2.composite.CodingDt coding = cc.addCoding();
						if (code != null) {
							coding.setCode(code);
						}
						if (system != null) {
							coding.setSystem(system);
						}
					}
					if (details != null) {
						cc.setText(details);
					}
					issue.setDetails(cc);
				}
				if (subset.containsKey("diagnostics")) {
					issue.setDiagnostics((String) subset.get("diagnostics"));
				}
				if (subset.containsKey("location")) {
					issue.addLocation((String) subset.get("location"));
				}
				if (subset.containsKey("location0")) {
					for (int index = 0; subset.containsKey("location" + index); index++) {
						issue.addLocation((String) subset.get("location" + index));
					}
				}
				if (subset.containsKey("severity")) {
					String code = (String) subset.get("severity");
					issue.setSeverity(ca.uhn.fhir.model.dstu2.valueset.IssueSeverityEnum.forCode(code));
				}
			}
			outcomeObj = outcome;
			outcomeJson = FhirContext.forDstu2().newJsonParser().encodeResourceToString(outcome);
		} else {
			throw new WorkItemHandlerException("Unrecognized fhirVersion attribute: '" + version + "'");
		}
		Map<String, Object> results = workItem.getResults();
		results.put("outcome", outcomeObj);
		results.put("outcomeJson", outcomeJson);
		ServiceResponse serviceResponse = new ServiceResponse();
		serviceResponse.setBody(outcomeJson);
		serviceResponse.addHeaderValue("Content-Type", "application/json");
		serviceResponse.setResponseCode(responseCode(status));
		results.put("serviceResponse", serviceResponse);
		manager.completeWorkItem(workItem.getId(), results);
	}

	private Integer responseCode(Object status) {
		int retval = 200;
		if (status != null) {
			try {
				retval = Integer.valueOf(status.toString());
			} catch (Exception ignore) { }
		}
		return retval;
	}

	public static Map<String, Object> extractIssueDetails(Map<String, Object> params, String issueId) {
		return params.entrySet().stream().
				filter(e -> e.getKey().startsWith(ISSUE_PREFIX + issueId + "_")).
				collect(Collectors.toMap(
						e -> e.getKey().replace(ISSUE_PREFIX + issueId + "_", ""), 
						e -> e.getValue()));
	}
	
	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// do nothing
	}
}
