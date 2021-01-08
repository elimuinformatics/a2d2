package io.elimu.task.fhir.r4;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;

import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Subscription.SubscriptionChannelComponent;
import org.hl7.fhir.r4.model.Task;
import org.jbpm.services.task.impl.model.xml.JaxbAttachment;
import org.jbpm.services.task.impl.model.xml.JaxbComment;
import org.jbpm.services.task.impl.model.xml.JaxbContent;
import org.jbpm.services.task.impl.model.xml.JaxbContentData;
import org.jbpm.services.task.impl.model.xml.JaxbDeadlines;
import org.jbpm.services.task.impl.model.xml.JaxbFaultData;
import org.jbpm.services.task.impl.model.xml.JaxbI18NText;
import org.jbpm.services.task.impl.model.xml.JaxbOrganizationalEntity;
import org.jbpm.services.task.impl.model.xml.JaxbPeopleAssignments;
import org.jbpm.services.task.impl.model.xml.JaxbTask;
import org.jbpm.services.task.impl.model.xml.JaxbTaskData;
import org.jbpm.workflow.instance.WorkflowRuntimeException;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.api.task.TaskEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import io.elimu.task.xml.JaxbTaskWrapper;

public class R4FhirTaskEventListener extends io.elimu.task.fhir.FhirTaskEventListener {

	private static final Logger LOG = LoggerFactory.getLogger(R4FhirTaskEventListener.class);
	
	public R4FhirTaskEventListener(RuntimeManager runtimeManager, String fhirServerVariableName, String microserviceServerVariableName, String microserviceAuthVariableName) {
		super(runtimeManager, fhirServerVariableName, microserviceServerVariableName, microserviceAuthVariableName);
	}

	@Override
	protected IBaseResource toFhirTask(TaskEvent event) {
		try {
			String fileContent = IOUtils.toString(getClass().getResourceAsStream("/taskTemplate.ftl"), "UTF-8");
			File file = File.createTempFile("taskTemplate.ftl", ".ftl");
			FileUtils.writeStringToFile(file, fileContent, "UTF-8");
			Configuration cfg = new Configuration(Configuration.VERSION_2_3_30);
	        cfg.setClassForTemplateLoading(getClass(), "/");
	        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	        cfg.setDefaultEncoding("UTF-8");
	        cfg.setDirectoryForTemplateLoading(new File(System.getProperty("java.io.tmpdir")));
	        Template template = cfg.getTemplate(file.getName());
	        StringWriter processedOutput = new StringWriter();
	        Map<String, Object> templateData = new HashMap<>();
	    	//variables
	    	//   event: TaskEvent
	    	//	 now: new java.util.Date()
	        templateData.put("now", event.getEventDate());
	        templateData.put("event", event);
	        template.process(templateData, processedOutput);
	        String json = processedOutput.getBuffer().toString();
	        return getFhirContext().newJsonParser().parseResource(json);
		} catch (Exception e) {
			LOG.error("Couldn't transform jBPM task to FHIR task", e);
			throw new RuntimeException("Error", e);
		}
	}

	@Override
	protected void setId(IBaseResource resource, String id) {
		((DomainResource) resource).setId(id);
	}

	@Override
	protected FhirContext getFhirContext() {
		return FhirContext.forR4();
	}

	@Override
	protected String getFhirSubscriptionId(IGenericClient client, TaskEvent event) {
		WorkflowProcessInstance wfInstance = super.getProcessVariables(event);
		String url = getSubscriptionUrl(wfInstance);
		Bundle bundle = (Bundle) client.search().forResource(Subscription.class)
			.where(new StringClientParam("criteria").matchesExactly().value("Task?"))
			.execute();
		if (bundle.hasEntry()) {
			for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
				Subscription sub = (Subscription) entry.getResource();
				if (sub != null && sub.getChannel() != null && url.equals(sub.getChannel().getEndpoint())) {
					return sub.getId();
				}
			}
		}
		return null;
	}

	private String getSubscriptionUrl(WorkflowProcessInstance wfInstance) {
		String microserviceBaseUrl = (String) wfInstance.getVariable(microserviceServerVariableName);
		if (!microserviceBaseUrl.endsWith("/")) {
			microserviceBaseUrl += "/";
		}
		return microserviceBaseUrl + "on-task-update?";
	}

	@Override
	protected IBaseResource toFhirSubscription(TaskEvent event) {
		try {
			Subscription sub = new Subscription();
			SubscriptionChannelComponent channel = new SubscriptionChannelComponent();
			WorkflowProcessInstance wfInstance = super.getProcessVariables(event);
			getSubscriptionUrl(wfInstance);
			String microserviceAuth = (String) wfInstance.getVariable(microserviceAuthVariableName);
			channel.setEndpoint(getSubscriptionUrl(wfInstance));
			channel.setType(Subscription.SubscriptionChannelType.RESTHOOK);
			channel.setPayload("application/fhir+json");
			if (microserviceAuth != null) {
				channel.addHeader("Authorization: Basic " + microserviceAuth);
			}
			sub.setChannel(channel);
			sub.setStatus(Subscription.SubscriptionStatus.REQUESTED);
			sub.setCriteria("Task?");
			return sub;
		} catch (Exception e) {
			LOG.error("Couldn't create FHIR subscription", e);
			throw new RuntimeException("Error creating sub", e);
		}
	}
	
	public org.kie.api.task.model.Task toJbpmTask(Task r4task, org.kie.api.task.model.Task originalTask) {
		try {
			String fileContent = IOUtils.toString(getClass().getResourceAsStream("/jbpmTemplate.ftl"), "UTF-8");
			File file = File.createTempFile("jbpmTemplate.ftl", ".ftl");
			FileUtils.writeStringToFile(file, fileContent, "UTF-8");
			Configuration cfg = new Configuration(Configuration.VERSION_2_3_30);
	        cfg.setClassForTemplateLoading(getClass(), "/");
	        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	        cfg.setDefaultEncoding("UTF-8");
	        cfg.setDirectoryForTemplateLoading(new File(System.getProperty("java.io.tmpdir")));
	        Template template = cfg.getTemplate(file.getName());
	        StringWriter processedOutput = new StringWriter();
	        Map<String, Object> templateData = new HashMap<>();
	        templateData.put("fhirTask", r4task);
	        templateData.put("originalTask", originalTask);
	        template.process(templateData, processedOutput);
	        String xml = processedOutput.getBuffer().toString();
	        JaxbTaskWrapper xmlTask = (JaxbTaskWrapper) JAXBContext.newInstance(
	        		JaxbAttachment.class, JaxbComment.class, JaxbContent.class,
	        		JaxbContentData.class, JaxbDeadlines.class, JaxbFaultData.class,
	        		JaxbI18NText.class, JaxbOrganizationalEntity.class, JaxbPeopleAssignments.class,
	        		JaxbTaskData.class, JaxbTask.class, JaxbTaskWrapper.class).
	        		createUnmarshaller().unmarshal(new StringReader(xml));
	        return xmlTask.getTaskWithInputsAndOutputs();
		} catch (Exception e) {
			LOG.error("Couldn't transform FHIR task to jBPM task", e);
			throw new RuntimeException("Error", e);
		}
	}

	@Override
	protected void assertValidOutcome(WorkflowProcessInstance pInstance, MethodOutcome out) throws WorkflowRuntimeException {
		OperationOutcome opOut = (OperationOutcome) out.getOperationOutcome();
		if (opOut != null && opOut.getIssue() != null && !opOut.getIssue().isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (OperationOutcomeIssueComponent issue : opOut.getIssue()) {
				sb.append(issue.getSeverity().name()).
						append(" - ").append(issue.getCode().name()).
						append(": ").append(issue.getDetails().getText()).
						append(" (").append(issue.getDiagnostics()).append(" at location ").
						append(issue.getLocation().toString()).append(", expression ").
						append(issue.getExpression()).append(")\n");
			}
			throw new WorkflowRuntimeException(null, pInstance, new IllegalArgumentException(sb.toString()));
		}
	}
}
