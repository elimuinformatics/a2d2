package io.elimu.cdshookapi.service;

import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.commands.TaskContext;
import org.kie.api.runtime.Context;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.TaskPersistenceContext;

public class LoadTaskWithVariablesCommand extends TaskCommand<Task> {

	private static final long serialVersionUID = 1L;
	
	private final Long taskId;
	
	public LoadTaskWithVariablesCommand(Long taskId) {
		this.taskId = taskId;
	}
	
	@Override
	public Task execute(Context cntxt) {
		TaskContext context = (TaskContext) cntxt;
		TaskPersistenceContext persistenceContext = context.getPersistenceContext();
		Task task = persistenceContext.findTask(taskId);
		if (task == null) {
			return null;
		}
		context.getTaskContentService().loadTaskVariables(task);
		return task;
	}

}
