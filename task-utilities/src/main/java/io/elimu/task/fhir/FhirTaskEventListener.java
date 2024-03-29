package io.elimu.task.fhir;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jbpm.services.task.impl.model.ContentImpl;
import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.jbpm.workflow.instance.WorkflowRuntimeException;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.api.task.TaskEvent;
import org.kie.api.task.TaskLifeCycleEventListener;
import org.kie.api.task.model.Comment;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.internal.task.api.ContentMarshallerContext;
import org.kie.internal.task.api.TaskContentService;
import org.kie.internal.task.api.model.InternalTaskData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FhirTaskEventListener implements TaskLifeCycleEventListener {

	private static final Logger LOG = LoggerFactory.getLogger(FhirTaskEventListener.class);

	private static String FHIR_SUB = null;

	public static enum FhirTaskInputs {
		FHIR_TASK_ID_NAME("io.elimu.fhir:taskid"),
		FHIR_INSURANCE_INPUT_NAME("io.elimu.fhir:insurance"),
		FHIR_INTENT_INPUT_NAME("io.elimu.fhir:intent"),
		FHIR_ENCOUNTER_INPUT_NAME("io.elimu.fhir:encounter"),
		FHIR_REASON_CODE_NAME("io.elimu.fhir:reasonCode"),
		FHIR_REASON_REF_NAME("io.elimu.fhir:reasonRef"),
		FHIR_FOCUS_INPUT_NAME("io.elimu.fhir:focus"),
		FHIR_FOR_INPUT_NAME("io.elimu.fhir:for"),
		JBPM_FORM_CODE_NAME("org.jbpm.task:form"),
		JBPM_STATUS_CODE_NAME("org.jbpm.task:status"), 
		JBPM_TASKTYPE_NAME("org.jbpm.task:taskType"), 
		JBPM_DEPLOYMENTID_NAME("org.jbpm.task:deploymentId"), 
		JBPM_PARENTID_NAME("org.jbpm.task:parentId"),
		JBPM_PROCESSID_NAME("org.jbpm.task:processId"),
		JBPM_PROCESSINSTANCEID_NAME("org.jbpm.task:processInstanceId"),
		JBPM_PROCESSSESSIONID_NAME("org.jbpm.task:processSessionId"),
		JBPM_WORKITEMID_NAME("org.jbpm.task:workItemId"),
		JBPM_TASK_HASH_NAME("org.jbpm.task:hash"), 
		FHIR_UPDATE_FLAG("this_is_a_flag");

		final String description;
		
		private FhirTaskInputs(String description) {
			this.description = description;
		}
		
		public String getDescription() {
			return description;
		}
		
		public static boolean has(String description) {
			for (FhirTaskInputs input : values()) {
				if (description != null && description.equals(input.description)) {
					return true;
				}
			}
			return false;
		}
	}
	
	protected final RuntimeManager runtimeManager;
	protected final String fhirServerVariableName;
	protected final String microserviceServerVariableName;
	protected final String microserviceAuthVariableName;
	
	public FhirTaskEventListener(RuntimeManager runtimeManager, String fhirServerVariableName, String microserviceServerVariableName, String microserviceAuthVariableName) {
		this.runtimeManager = runtimeManager;
		this.fhirServerVariableName = fhirServerVariableName;
		this.microserviceServerVariableName = microserviceServerVariableName;
		this.microserviceAuthVariableName = microserviceAuthVariableName;
	}
	
	@Override
	public void beforeTaskActivatedEvent(TaskEvent event) {
		updateTask(event);
	}

	@Override
	public void beforeTaskClaimedEvent(TaskEvent event) {
		updateTask(event);
	}

	@Override
	public void beforeTaskSkippedEvent(TaskEvent event) {
		updateTask(event);
	}

	@Override
	public void beforeTaskStartedEvent(TaskEvent event) {
		updateTask(event);
	}

	@Override
	public void beforeTaskStoppedEvent(TaskEvent event) {
		updateTask(event);
	}

	@Override
	public void beforeTaskCompletedEvent(TaskEvent event) {
		updateTask(event);
	}

	@Override
	public void beforeTaskFailedEvent(TaskEvent event) {
		updateTask(event);
	}

	@Override
	public void beforeTaskAddedEvent(TaskEvent event) {
//		insertTask(event);
	}

	@Override
	public void beforeTaskExitedEvent(TaskEvent event) {
		updateTask(event);
	}

	@Override
	public void beforeTaskReleasedEvent(TaskEvent event) {
		updateTask(event);
	}

	@Override
	public void beforeTaskResumedEvent(TaskEvent event) {
		updateTask(event);
	}

	@Override
	public void beforeTaskSuspendedEvent(TaskEvent event) {
		updateTask(event);
	}

	@Override
	public void beforeTaskForwardedEvent(TaskEvent event) {
		updateTask(event);
	}

	@Override
	public void beforeTaskDelegatedEvent(TaskEvent event) {
		updateTask(event);
	}

	@Override
	public void beforeTaskNominatedEvent(TaskEvent event) {
		updateTask(event);
	}

	@Override
	public void afterTaskActivatedEvent(TaskEvent event) {
	}

	@Override
	public void afterTaskClaimedEvent(TaskEvent event) {
	}

	@Override
	public void afterTaskSkippedEvent(TaskEvent event) {
	}

	@Override
	public void afterTaskStartedEvent(TaskEvent event) {
	}

	@Override
	public void afterTaskStoppedEvent(TaskEvent event) {
	}

	@Override
	public void afterTaskCompletedEvent(TaskEvent event) {
	}

	@Override
	public void afterTaskFailedEvent(TaskEvent event) {
	}

	@Override
	public void afterTaskAddedEvent(TaskEvent event) {
		insertTask(event);
	}

	@Override
	public void afterTaskExitedEvent(TaskEvent event) {
	}

	@Override
	public void afterTaskReleasedEvent(TaskEvent event) {
	}

	@Override
	public void afterTaskResumedEvent(TaskEvent event) {
	}

	@Override
	public void afterTaskSuspendedEvent(TaskEvent event) {
	}

	@Override
	public void afterTaskForwardedEvent(TaskEvent event) {
	}

	@Override
	public void afterTaskDelegatedEvent(TaskEvent event) {
	}

	@Override
	public void afterTaskNominatedEvent(TaskEvent event) {
	}


	protected void updateTask(TaskEvent event) {
		List<Comment> comments = event.getTask().getTaskData().getComments();
		if (comments != null && !comments.isEmpty()) {
			for (int index = 0; index < comments.size(); index++) {
				Comment comment = comments.get(index);
				if (comment != null && comment.getText() != null && FhirTaskInputs.FHIR_UPDATE_FLAG.getDescription().equals(comment.getText())) {
					//this is a flag to avoid cases where the update is triggered by FHIR, 
					//so that it wouldn't produce an infinite loop. We remove the flag and return
					comments.remove(comment);
					return;
				}
			}
		}
		WorkflowProcessInstance wfInstance = getProcessVariables(event);
		Map<String, Object> inputs = event.getTask().getTaskData().getTaskInputVariables();
		if (inputs == null) {
			event.getTaskContext().loadTaskVariables(event.getTask());
			inputs = event.getTask().getTaskData().getTaskInputVariables();
		}
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Object client = newFhirClient((String) wfInstance.getVariable(fhirServerVariableName));
			Object execPath = client.getClass().getMethod("update").invoke(client);
			execPath = execPath.getClass().getMethod("resource", cl.loadClass("org.hl7.fhir.instance.model.api.IBaseResource")).invoke(execPath, toFhirTask(event));
			execPath = execPath.getClass().getMethod("execute").invoke(execPath);
			assertValidOutcome(wfInstance, execPath);
		} catch (ReflectiveOperationException e) {
			throw new WorkflowRuntimeException(null, wfInstance, e);
		}
	}

	protected Object newFhirClient(String baseUrl) throws ReflectiveOperationException {
		Object ctx = getFhirContext();
		Object client = ctx.getClass().getMethod("newRestfulGenericClient", String.class).invoke(ctx, baseUrl);
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Class<?> encEnumClass = cl.loadClass("ca.uhn.fhir.rest.api.EncodingEnum");
		client.getClass().getMethod("setEncoding", encEnumClass).invoke(client, encEnumClass.getField("JSON").get(null));
		client.getClass().getMethod("setDontValidateConformance", boolean.class).invoke(client, true);
		return client;
	}

	protected WorkflowProcessInstance getProcessVariables(TaskEvent event) {
		Long processInstanceId = event.getTask().getTaskData().getProcessInstanceId();
		RuntimeEngine engine = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
		WorkflowProcessInstance wfInstance = (WorkflowProcessInstance) engine.getKieSession().getProcessInstance(processInstanceId);
		if (wfInstance == null) {
			LOG.warn("Could not find process instance in ksession. Will return empty Map for variables");
			return null;
		}
		return wfInstance;
	}

	protected void insertTask(TaskEvent event) {
		WorkflowProcessInstance wfInstance = getProcessVariables(event);
		Map<String, Object> inputs = event.getTask().getTaskData().getTaskInputVariables();
		if (inputs == null) {
			event.getTaskContext().loadTaskVariables(event.getTask());
			inputs = event.getTask().getTaskData().getTaskInputVariables();
		}
		String url = (String) wfInstance.getVariable(fhirServerVariableName);
		try {
			Object client = newFhirClient(url);
			final Object fhirTask = toFhirTask(event);
			Object execPath = client.getClass().getMethod("create").invoke(client);
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Class<?> ibaseResClass = cl.loadClass("org.hl7.fhir.instance.model.api.IBaseResource");
			execPath = execPath.getClass().getMethod("resource", ibaseResClass).invoke(execPath, fhirTask);
			Object out = execPath.getClass().getMethod("execute").invoke(execPath);
			assertValidOutcome(wfInstance, out);
			Object fhirId = out.getClass().getMethod("getId").invoke(out);
			fhirId = fhirId.getClass().getMethod("getIdPartAsLong").invoke(fhirId);
			setId(fhirTask, fhirId.toString());
			if (FhirTaskEventListener.FHIR_SUB == null) {
				//we need to check if the fhir sub exists
				FhirTaskEventListener.FHIR_SUB = getFhirSubscriptionId(client, event);
				if (FhirTaskEventListener.FHIR_SUB == null) {
					//we need to create it
					execPath = client.getClass().getMethod("create").invoke(client);
					execPath = execPath.getClass().getMethod("resource", ibaseResClass).invoke(execPath, toFhirSubscription(event));
					Object out2 = execPath.getClass().getMethod("execute").invoke(execPath);
					assertValidOutcome(wfInstance, out2);
					Object fhirSubId = out2.getClass().getMethod("getId").invoke(out2);
					fhirSubId = fhirSubId.getClass().getMethod("getIdPart").invoke(fhirSubId);
					FhirTaskEventListener.FHIR_SUB = fhirSubId.toString();
				}
			}
			String guid = UUID.randomUUID().toString();
			inputs.put(FhirTaskInputs.JBPM_TASK_HASH_NAME.getDescription(), guid);
			inputs.put(FhirTaskInputs.FHIR_TASK_ID_NAME.getDescription(), fhirId);
			final TaskContentService contentService = ((org.jbpm.services.task.commands.TaskContext)event.getTaskContext()).getTaskContentService();
			ContentMarshallerContext mctx = contentService.getMarshallerContext(event.getTask());
	        byte[] inputData = ContentMarshallerHelper.marshallContent(inputs, mctx.getEnvironment());
			long documentContentId = contentService.setDocumentContent(event.getTask().getId(), new ContentImpl(inputData));
			((InternalTaskData)event.getTask().getTaskData()).setDocumentContentId(documentContentId);
		} catch (ReflectiveOperationException e) {
			throw new WorkflowRuntimeException(null, wfInstance, e);
		}
	}

	protected abstract void assertValidOutcome(WorkflowProcessInstance instance, Object methodOutcome) throws WorkflowRuntimeException;

	protected abstract Object toFhirTask(TaskEvent event) throws ReflectiveOperationException;

	protected abstract void setId(Object resource, String id) throws ReflectiveOperationException;
	
	protected abstract String getFhirSubscriptionId(Object client, TaskEvent event) throws ReflectiveOperationException;
	
	protected abstract Object toFhirSubscription(TaskEvent event) throws ReflectiveOperationException;

	protected abstract Object getFhirContext() throws ReflectiveOperationException;
}
