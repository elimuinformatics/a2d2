
package io.elimu.a2d2.fhirresourcewih;

import java.util.List;

import org.hl7.fhir.r4.model.CommunicationRequest;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.StringType;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.impl.GenericClient;
import ca.uhn.fhir.rest.client.interceptor.SimpleRequestHeaderInterceptor;
import io.elimu.a2d2.exception.WorkItemHandlerException;

public class StoreCommRequestWorkItemHandler implements WorkItemHandler {

	private static final Logger log = LoggerFactory.getLogger(StoreCommRequestWorkItemHandler.class);
	
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		// get inputs
		String fhirServerUrl = (String) workItem.getParameter("fhirServerUrl");
		String fhirServerApiKey = (String) workItem.getParameter("fhirServerApiKey");
		String patientId = (String) workItem.getParameter("patientId");
		String carePlanId = (String) workItem.getParameter("carePlanId");
		String toPhone = (String) workItem.getParameter("tophone");
		String fromPhone = (String) workItem.getParameter("fromphone");
		String processInstanceId = String.valueOf(workItem.getProcessInstanceId());
		String messageText = (String) workItem.getParameter("message");
		
		// validate inputs
		List<String> missingValues = new java.util.ArrayList<String>();
		if (messageText == null) {
			missingValues.add("message");
		}
		if (toPhone == null) {
			missingValues.add("tophone");
		}
		if (fromPhone == null) {
			missingValues.add("fromphone");
		}
		if (patientId == null) {
			missingValues.add("patientId");
		}
		/*if (carePlanId == null) {
			missingValues.add("carePlanId");
		}*/
		if (fhirServerUrl == null) {
			missingValues.add("fhirServerUrl");
		}
		if (fhirServerApiKey == null) {
			missingValues.add("fhirServerApiKey");
		}
		if (missingValues.size() > 0) {
			log.error("Required parameters not provided");
			throw new WorkItemHandlerException("StoreCommRequestWorkItemHandler requires these work item parameters: " + missingValues.toString());
		}
		
		//create comm request
		CommunicationRequest request = new CommunicationRequest();
		request.setStatus(CommunicationRequest.CommunicationRequestStatus.ACTIVE);
		request.getSubject().setReference("Patient/" + patientId);
		if (carePlanId != null) {
			request.addBasedOn().setReference("CarePlan/" + carePlanId);
		}
		request.addMedium().addCoding().setSystem("http://elimu.io/sms/receiver").setCode(toPhone.replaceAll("[^0-9]", "")).setDisplay("SMS Receiver");
		request.addMedium().addCoding().setSystem("http://elimu.io/sms/sender").setCode(fromPhone.replaceAll("[^0-9]", "")).setDisplay("SMS Sender");
		request.addMedium().addCoding().setSystem("http://elimu.io/sms/processinstance").setCode(processInstanceId).setDisplay("Process Instance Id");
		request.addPayload().setContent(new StringType(messageText));
		request.setOccurrence(new DateTimeType(new java.util.Date()));

		//add to the server
		GenericClient client = (GenericClient) FhirContext.forR4().newRestfulGenericClient(fhirServerUrl);
		client.setEncoding(EncodingEnum.JSON);
		client.setDontValidateConformance(true);
		client.registerInterceptor(new SimpleRequestHeaderInterceptor("x-api-key", fhirServerApiKey));
		MethodOutcome result = client.create().resource(request).execute();

		//TODO validate storing
		
		workItem.getResults().put("commRequestId", result.getId().getIdPart());
		manager.completeWorkItem(workItem.getId(), workItem.getResults());
	}
	
	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// leave empty
	}

}
