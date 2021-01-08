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

package io.elimu.cdshookapi.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskData;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.elimu.genericapi.service.RunningServices;

public class TaskServiceHelper {


	private RuntimeManager runTimeManager = null;
	private RuntimeEngine runTimeEngine = null;
	private TaskService taskService = null;
	
	private static final Logger log = LoggerFactory.getLogger(TaskServiceHelper.class);


	public TaskServiceHelper() {
		for(String serviceName: RunningServices.getInstance().serviceNames()) {
			try {
				this.runTimeManager = RuntimeManagerRegistry.get().getManager(serviceName);
				this.taskService = this.runTimeManager.getRuntimeEngine(ProcessInstanceIdContext.get()).getTaskService();
				break;
			} catch(Exception e) {
				log.error("Task Service is not configured for :: " + serviceName);
			}
		}
	}

	public TaskServiceHelper(Long taskId) {
		this.runTimeManager = RuntimeManagerHelper.findRuntimeManagerByTaskId(taskId);
		this.taskService = this.runTimeManager.getRuntimeEngine(ProcessInstanceIdContext.get()).getTaskService();
		Task task = taskService.getTaskById(taskId);
		this.runTimeEngine = this.runTimeManager
				.getRuntimeEngine(ProcessInstanceIdContext.get(task.getTaskData().getProcessInstanceId()));
	}

	public Task getTask(Long taskId) {
		if (this.taskService != null) {
			return this.taskService.getTaskById(taskId);
		}
		return null;
	}

	public Task getTaskWithVariables(Long taskId) throws Exception { //TODO refactor into a TaskCommand
		if (this.taskService != null) {
			return this.taskService.execute(new LoadTaskWithVariablesCommand(taskId));
		}
		return null;
	}

	public void claimTask(Long taskId, String userId) {
		this.runTimeEngine.getTaskService().claim(taskId, userId);
	}

	public void startTask(Long taskId, String userId) {
		this.runTimeEngine.getTaskService().start(taskId, userId);
	}

	public void completeTask(Long taskId, String userId, Map<String, Object> resultData) {
		this.runTimeEngine.getTaskService().complete(taskId, userId, resultData);
	}

	public List<TaskEntity> getTaskList(String userId) {
		List<TaskSummary> taskSummaryList = this.taskService.getTasksOwnedByStatus(userId,
				Arrays.asList(Status.Reserved, Status.Ready), null);
		List<TaskEntity> taskEntityList = new ArrayList<>();
		for (TaskSummary taskSummary : taskSummaryList) {
			taskEntityList.add(buildTaskEntity(taskSummary.getId()));
		}
		return taskEntityList;
	}

	public TaskEntity buildTaskEntity(Long taskId) {

		TaskEntity taskEntity = new TaskEntity();
		Task task = getTask(taskId);
		TaskData taskData = task.getTaskData();
		taskEntity.setTaskId(task.getId());
		taskEntity.setTaskName(task.getName());
		taskEntity.setProcessInstanceId(taskData.getProcessInstanceId());
		taskEntity.setProcessId(taskData.getProcessId());
		long inputId = taskData.getDocumentContentId();
		if (inputId > 0) {
			Content c = taskService.getContentById(inputId);
			Map<String, Object> inputs = (Map<String, Object>) ContentMarshallerHelper.unmarshall(c.getContent(), null);
			taskEntity.setTaskInputs(inputs);
		}
		Long outputId = taskData.getOutputContentId();
		if (outputId != null && outputId > 0) {
			Content c = taskService.getContentById(outputId);
			Map<String, Object> outputs = (Map<String, Object>) ContentMarshallerHelper.unmarshall(c.getContent(), null);
			taskEntity.setTaskOutputs(outputs);
		}
		return taskEntity;

	}

}
