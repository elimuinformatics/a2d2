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

package io.elimu.cdshookapi.api.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.elimu.cdshookapi.domain.GenericResponse;
import io.elimu.cdshookapi.service.TaskEntity;
import io.elimu.cdshookapi.service.TaskServiceHelper;

@RestController
@CrossOrigin(maxAge = 6000)
@RequestMapping(value = "/api/v1/cds-task")
public class TaskController {
	
	private static final Logger log = LoggerFactory.getLogger(TaskController.class);
	private static final String USERID = "userId";

	@CrossOrigin
	@GetMapping(value = "/getTasksOwned", produces = { "application/json" })
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<TaskEntity> getTasksOwned(HttpServletRequest request) {
		try {
		String userId = request.getHeader(USERID);
		TaskServiceHelper taskServiceHelper = new TaskServiceHelper();
		return taskServiceHelper.getTaskList(userId);
		} catch(Exception e) {
			log.error("Exception occured due to {}", e.getMessage(), e);
			return new ArrayList<>();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@CrossOrigin
	@PostMapping(value = "/{taskId}/start", produces = { "application/json" })
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<GenericResponse> startTask(HttpServletRequest request, @PathVariable("taskId") Long taskId) {
		try {
			String userId = request.getHeader(USERID);
			if (taskId != null) {
				TaskServiceHelper taskServiceHelper = new TaskServiceHelper(taskId);
				Task task = taskServiceHelper.getTask(taskId);
				TaskData taskData = task.getTaskData();

				if (taskData.getStatus().equals(Status.Ready)) {
					taskServiceHelper.claimTask(taskId, userId);
				}
				if (taskData.getStatus().equals(Status.Reserved)) {
					taskServiceHelper.startTask(taskId, userId);
				}
				return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse(taskServiceHelper.buildTaskEntity(taskId)));
			} else {
				return ResponseEntity.status(HttpStatus.ACCEPTED).body(new GenericResponse("No task found for task id"));
			}

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(new GenericResponse("Failed to start task: " + e.getMessage()));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@CrossOrigin
	@PostMapping(value = "/{taskId}/completeTask", produces = { "application/json" })
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<GenericResponse> completeTask(HttpServletRequest request, @PathVariable("taskId") Long taskId,
			@RequestBody Object requestData) {

		Map<String, Object> resultData = new ObjectMapper().convertValue(requestData, Map.class);
		try {

			String userId = request.getHeader(USERID);

			TaskServiceHelper taskServiceHelper = new TaskServiceHelper(taskId);
			Task task = taskServiceHelper.getTask(taskId);
			TaskData taskData = task.getTaskData();

			if (taskData.getStatus().equals(Status.Ready)) {
				taskServiceHelper.claimTask(taskId, userId);
			}
			if (task.getTaskData().getStatus().equals(Status.Reserved)) {
				taskServiceHelper.startTask(taskId, userId);
			}
			if (task.getTaskData().getStatus().equals(Status.InProgress)) {
				taskServiceHelper.completeTask(taskId, userId, resultData);
			}

			return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse(taskServiceHelper.buildTaskEntity(taskId)));
		} catch (Exception e) {
			log.error("Exception occured due to {}", e.getMessage(), e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse("Failed to complete task"));
	}
}
