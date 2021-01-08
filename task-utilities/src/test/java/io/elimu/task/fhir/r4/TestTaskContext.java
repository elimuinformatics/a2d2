package io.elimu.task.fhir.r4;

import java.util.Map;
import java.util.Properties;

import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.kie.api.task.TaskContext;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.model.InternalTaskData;

public class TestTaskContext implements TaskContext {

	private Map<String, Object> inputs;
	private Map<String, Object> outputs;

	public TestTaskContext(Map<String, Object> inputs, Map<String, Object> outputs) {
		this.inputs = inputs;
		this.outputs = outputs;
	}
	
	@Override
	public UserGroupCallback getUserGroupCallback() {
		return new JBossUserGroupCallbackImpl(new Properties());
	}

	@Override
	public Task loadTaskVariables(Task task) {
		((InternalTaskData) task.getTaskData()).setTaskInputVariables(inputs);
		((InternalTaskData) task.getTaskData()).setTaskOutputVariables(outputs);
		return task;
	}

	@Override
	public String getUserId() {
		return null;
	}

}
