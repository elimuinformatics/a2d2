package io.elimu.a2d2.fhirresourcewih;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import io.elimu.a2d2.exception.WorkItemHandlerException;
import io.elimu.a2d2.genericmodel.ServiceResponse;
import io.elimu.a2d2.parsing.FhirParseUtil;
import io.elimu.a2d2.parsing.FhirParseUtil.FormatType;

public class OperationOutcomeDelegate implements WorkItemHandler {

	public static final String ISSUE_PREFIX = "issue_";

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String version = (String) workItem.getParameter("fhirVersion");
		Object status = workItem.getParameter("response_status");
		Object outcomeObj = null;
		String outcomeJson = null;
		if (version == null || "FHIR4".equalsIgnoreCase(version) || "R4".equalsIgnoreCase(version)) {
			Object outcome = createOutcome(workItem, "org.hl7.fhir.r4.model.OperationOutcome", 
					"org.hl7.fhir.r4.model.OperationOutcome$IssueType", 
					"org.hl7.fhir.r4.model.OperationOutcome$IssueSeverity",
					"org.hl7.fhir.r4.model.CodeableConcept",
					"fromCode");
			outcomeObj = outcome;
			outcomeJson = FhirParseUtil.getInstance().encodeJsonResource(FormatType.FHIR4, outcome);
		} else if ("FHIR3".equalsIgnoreCase(version) || "DSTU3".equalsIgnoreCase(version)) {
			Object outcome = createOutcome(workItem, "org.hl7.fhir.dstu3.model.OperationOutcome", 
					"org.hl7.fhir.dstu3.model.OperationOutcome$IssueType",
					"org.hl7.fhir.dstu3.model.OperationOutcome$IssueSeverity",
					"org.hl7.fhir.dstu3.model.CodeableConcept",
					"fromCode");
			outcomeObj = outcome;
			outcomeJson = FhirParseUtil.getInstance().encodeJsonResource(FormatType.FHIR3, outcome);
		} else if ("FHIR2".equalsIgnoreCase(version) || "DSTU2".equalsIgnoreCase(version)) {
			Object outcome = createOutcome(workItem, "ca.uhn.fhir.model.dstu2.resource.OperationOutcome",
					"ca.uhn.fhir.model.dstu2.valueset.IssueTypeEnum",
					"ca.uhn.fhir.model.dstu2.valueset.IssueSeverityEnum",
					"ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt",
					"forCode");
			outcomeObj = outcome;
			outcomeJson = FhirParseUtil.getInstance().encodeJsonResource(FormatType.FHIR2, outcome);
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

	private Object createOutcome(WorkItem workItem, String outcomeClassName, String issueTypeClassName, String issueSeverityClassName, String ccClassName, String codeMethodName) {
		Set<String> issueIds = workItem.getParameters().keySet().stream().
				filter(e -> e.startsWith(ISSUE_PREFIX)).
				map(s -> s.replace(ISSUE_PREFIX, "")).
				map(s -> s.substring(0, s.indexOf("_"))).
				distinct().
				collect(Collectors.toSet());
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Object outcome = cl.loadClass(outcomeClassName).getConstructor().newInstance();
			for (String issueId : issueIds) {
				Map<String, Object> subset = extractIssueDetails(workItem.getParameters(), issueId);
				Method addIssueMethod = outcome.getClass().getMethod("addIssue");
				Object issue = addIssueMethod.invoke(outcome);
				if (subset.containsKey("code")) {
					String code = (String) subset.get("code");
					Object ccode = cl.loadClass(issueTypeClassName).getMethod(codeMethodName, String.class).invoke(null, code);
					issue.getClass().getMethod("setCode", cl.loadClass(issueTypeClassName)).invoke(issue, ccode);
				}
				if (subset.containsKey("details") || subset.containsKey("detailsCode") || subset.containsKey("detailsSystem")) {
					String details = (String) subset.get("details");
					String code = (String) subset.get("detailsCode");
					String system = (String) subset.get("detailsSystem");
					Object cc = cl.loadClass(ccClassName).getConstructor().newInstance();
					if (code != null || system != null) {
						Object coding = cc.getClass().getMethod("addCoding").invoke(cc);
						if (code != null) {
							coding.getClass().getMethod("setCode", String.class).invoke(coding, code);
						}
						if (system != null) {
							coding.getClass().getMethod("setSystem", String.class).invoke(coding, system);
						}
					}
					if (details != null) {
						cc.getClass().getMethod("setText", String.class).invoke(cc, details);
					}
					issue.getClass().getMethod("setDetails", cc.getClass()).invoke(issue, cc);
				}
				if (subset.containsKey("diagnostics")) {
					issue.getClass().getMethod("setDiagnostics", String.class).invoke(issue, subset.get("diagnostics"));
				}
				if (subset.containsKey("location")) {
					issue.getClass().getMethod("addLocation", String.class).invoke(issue, subset.get("location"));
				}
				if (subset.containsKey("location0")) {
					for (int index = 0; subset.containsKey("location" + index); index++) {
						issue.getClass().getMethod("addLocation", String.class).invoke(issue, subset.get("location" + index));
					}
				}
				if (subset.containsKey("severity")) {
					String code = (String) subset.get("severity");
					Object ccode = cl.loadClass(issueSeverityClassName).getMethod(codeMethodName, String.class).invoke(null, code);
					issue.getClass().getMethod("setSeverity", ccode.getClass()).invoke(issue, ccode);
				}
			}
			return outcome;
		} catch (Exception e) {
			throw new RuntimeException("Couldn't reflect invocations to FHIR methods", e);
		}
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
