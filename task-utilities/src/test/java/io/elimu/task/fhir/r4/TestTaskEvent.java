package io.elimu.task.fhir.r4;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.kie.api.task.TaskContext;
import org.kie.api.task.TaskEvent;
import org.kie.api.task.model.Task;

public class TestTaskEvent implements TaskEvent {

	private final Task task;
	private final Date eventDate;
	private final Map<String, Object> inputs;
	private final Map<String, Object> outputs;
	
	public TestTaskEvent(Task task, Map<String, Object> inputs, Map<String, Object> outputs) {
		this.task = task;
		this.eventDate = new Date();
		this.inputs = inputs;
		this.outputs = outputs;
	}
	
	@Override
	public Task getTask() {
		return task;
	}

	@Override
	public TaskContext getTaskContext() {
		return new TestTaskContext(inputs, outputs);
	}

	@Override
	public Date getEventDate() {
		return eventDate;
	}

	@Override
	public Map<String, Object> getMetadata() {
		return new HashMap<>();
	}

}
