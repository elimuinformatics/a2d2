package io.elimu.task.fhir.r4;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
	protected Object toFhirTask(TaskEvent event) {
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
	        Object ctx = getFhirContext();
	        Object parser = ctx.getClass().getMethod("newJsonParser").invoke(ctx);
	        return parser.getClass().getMethod("parseResource", String.class).invoke(parser, json);
		} catch (Exception e) {
			LOG.error("Couldn't transform jBPM task to FHIR task", e);
			throw new RuntimeException("Error", e);
		}
	}

	@Override
	protected void setId(Object resource, String id) throws ReflectiveOperationException {
		resource.getClass().getMethod("setId", String.class).invoke(resource, id);
	}

	@Override
	protected Object getFhirContext() throws ReflectiveOperationException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		return cl.loadClass("ca.uhn.fhir.context.FhirContext").getMethod("forR4").invoke(null);
	}

	@Override
	protected String getFhirSubscriptionId(Object client, TaskEvent event) throws ReflectiveOperationException {
		WorkflowProcessInstance wfInstance = super.getProcessVariables(event);
		String url = getSubscriptionUrl(wfInstance);
		Object searchPath = client.getClass().getMethod("search").invoke(client);
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		searchPath = searchPath.getClass().getMethod("forResource", Class.class).invoke(searchPath, cl.loadClass("org.hl7.fhir.r4.model.Subscription"));
		Class<?> strClientParamClass = cl.loadClass("ca.uhn.fhir.rest.gclient.StringClientParam");
		Object strClientParam = strClientParamClass.getConstructor(String.class).newInstance("criteria");
		strClientParam = strClientParam.getClass().getMethod("matchesExactly").invoke(strClientParam);
		strClientParam = strClientParam.getClass().getMethod("value", String.class).invoke(strClientParam, "Task?");
		searchPath = searchPath.getClass().getMethod("where", strClientParamClass).invoke(searchPath, strClientParam);
		Object bundle = searchPath.getClass().getMethod("execute").invoke(searchPath);
		boolean hasEntry = (boolean) bundle.getClass().getMethod("hasEntry").invoke(bundle);
		if (hasEntry) {
			List<?> entries = (List<?>) bundle.getClass().getMethod("getEntry").invoke(bundle);
			for (Object entry : entries) {
				Object sub = entry.getClass().getMethod("getResource").invoke(entry);
				if (sub != null) {
					Object channel = sub.getClass().getMethod("getChannel").invoke(sub);
					if (channel != null) {
						Object endpoint = channel.getClass().getMethod("getEndpoint").invoke(channel);
						if (url.equals(endpoint)) {
							Object id = sub.getClass().getMethod("getId").invoke(sub);
							if (id != null) {
								return id.toString();
							}
						}
					}
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
	protected Object toFhirSubscription(TaskEvent event) throws ReflectiveOperationException {
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Object sub = cl.loadClass("org.hl7.fhir.r4.model.Subscription").getConstructor().newInstance();
			Object channel = cl.loadClass("org.hl7.fhir.r4.model.Subscription$SubscriptionChannelComponent").getConstructor().newInstance();
			WorkflowProcessInstance wfInstance = super.getProcessVariables(event);
			getSubscriptionUrl(wfInstance);
			String microserviceAuth = (String) wfInstance.getVariable(microserviceAuthVariableName);
			channel.getClass().getMethod("setEndpoint", String.class).invoke(channel, getSubscriptionUrl(wfInstance));
			Class<?> subChannelTypeClass = cl.loadClass("org.hl7.fhir.r4.model.Subscription$SubscriptionChannelType");
			channel.getClass().getMethod("setType", subChannelTypeClass).invoke(channel, subChannelTypeClass.getField("RESTHOOK").get(null));
			channel.getClass().getMethod("setPayload", String.class).invoke(channel, "application/fhir+json");
			if (microserviceAuth != null) {
				channel.getClass().getMethod("addHeader", String.class).invoke(channel, "Authorization: Basic " + microserviceAuth);
			}
			sub.getClass().getMethod("setChannel", channel.getClass()).invoke(sub, channel);
			Class<?> subStatusClass = cl.loadClass("org.hl7.fhir.r4.model.Subscription$SubscriptionStatus");
			sub.getClass().getMethod("setStatus", subStatusClass).invoke(sub, subStatusClass.getField("REQUESTED").get(null));
			sub.getClass().getMethod("setCriteria", String.class).invoke(sub, "Task?");
			return sub;
		} catch (Exception e) {
			LOG.error("Couldn't create FHIR subscription", e);
			throw new RuntimeException("Error creating sub", e);
		}
	}
	
	public org.kie.api.task.model.Task toJbpmTask(Object r4task, org.kie.api.task.model.Task originalTask) {
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
	protected void assertValidOutcome(WorkflowProcessInstance pInstance, Object out) throws WorkflowRuntimeException {
		try {
			Object opOut = out.getClass().getMethod("getOperationOutcome").invoke(out);
			if (opOut != null) {
				List<?> opOutIssue = (List<?>) opOut.getClass().getMethod("getIssue").invoke(opOut);
				if (opOutIssue != null && !opOutIssue.isEmpty()) {
					StringBuilder sb = new StringBuilder();
					for (Object issue : opOutIssue) {
						Object severity = issue.getClass().getMethod("getSeverity").invoke(issue);
						severity = severity.getClass().getMethod("name").invoke(severity);
						Object code = issue.getClass().getMethod("getCode").invoke(issue);
						code = code.getClass().getMethod("name").invoke(code);
						Object details = issue.getClass().getMethod("getDetails").invoke(issue);
						details = details.getClass().getMethod("getText").invoke(details);
						Object diagnostics = issue.getClass().getMethod("getDiagnostics").invoke(issue);
						Object location = issue.getClass().getMethod("getLocation").invoke(issue);
						Object expression = issue.getClass().getMethod("getExpression").invoke(issue);
						sb.append(severity).append(" - ").append(code).append(": ").append(details).
								append(" (").append(diagnostics).append(" at location ").
								append(location).append(", expression ").append(expression).append(")\n");
					}
					throw new WorkflowRuntimeException(null, pInstance, new IllegalArgumentException(sb.toString()));
				}
			}
		} catch (ReflectiveOperationException e) {
			throw new WorkflowRuntimeException(null, pInstance, new IllegalArgumentException("Couldn't reflect FHIR4 components", e));
		}
	}
}
