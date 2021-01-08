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

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.model.InternalComment;
import org.kie.internal.task.api.model.InternalTaskData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import io.elimu.cdshookapi.service.TaskServiceHelper;
import io.elimu.genericapi.service.GenericKieBasedService;
import io.elimu.genericapi.service.GenericService;
import io.elimu.genericapi.service.RunningServices;

@RestController
@CrossOrigin(maxAge = 6000)
@RequestMapping(value = "/api/task-events")
public class TaskEventsController {

	private static final Logger LOG = LoggerFactory.getLogger(TaskEventsController.class);

	@CrossOrigin
	@PostMapping(value = "/on-task-update/{taskId}", produces = {
			"application/fhir+json" })
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<String> onTaskUpdate(HttpEntity<String> httpEntity, 
			@PathVariable("taskId") Long fhirTaskId) {
		try {
			String body = httpEntity.getBody();
			Long taskId = extractTaskIdFromJson(body);
			TaskServiceHelper taskServiceHelper = new TaskServiceHelper(taskId);
			Task jbpmTask = taskServiceHelper.getTaskWithVariables(taskId);
			if (jbpmTask == null) {
				return new ResponseEntity<>(
						"Cannot find task " + taskId, HttpStatus.NOT_FOUND);
			}
			if (jbpmTask.getTaskData().getTaskInputVariables() == null) {
				((InternalTaskData) jbpmTask.getTaskData()).setTaskInputVariables(new HashMap<>());
			}
			if (jbpmTask.getTaskData().getTaskOutputVariables() == null) {
				((InternalTaskData) jbpmTask.getTaskData()).setTaskOutputVariables(new HashMap<>());
			}
			Map<String, Object> inputs = jbpmTask.getTaskData().getTaskInputVariables();
			Object taskGuid = inputs.get("org.jbpm.task:hash");
			if (taskGuid == null) {
				return new ResponseEntity<>(
						"Cannot access task " + taskId + ". Not a FHIR task", HttpStatus.FORBIDDEN);
			}
			GenericService service = RunningServices.getInstance().get(jbpmTask.getTaskData().getDeploymentId());
			final Task toStoreJbpmTask = toJbpmTask(jbpmTask, body, ((GenericKieBasedService) service).getClassLoader());
			//using task comments to pass the flag since it is not used
			InternalComment comment = (InternalComment) TaskModelProvider.getFactory().newComment();
			comment.setText("this_is_a_flag");
			if(toStoreJbpmTask != null) {
				((InternalTaskData) toStoreJbpmTask.getTaskData()).setComments(Arrays.asList(comment));
				//update task with the flag on (the listener will remove it after finding it should be ignored from FHIR updating step)
				service.updateTask(toStoreJbpmTask);
			} else {
				throw new RuntimeException("Unable to get jbpm task");
			}
			return new ResponseEntity<>(body, HttpStatus.OK);
		} catch(Exception e) {
			LOG.error("Error updating task", e);
			return new ResponseEntity<>("Error updating task: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private Long extractTaskIdFromJson(String json) throws IOException {
		JsonNode jsonData = new ObjectMapper().readTree(json);
		JsonNode codeData = jsonData.get("code");
		String taskName = null;
		String taskIdString = null;
		if (codeData != null) {
			taskName = codeData.get("text").asText();
			if (taskName != null) {
				JsonNode basedOnData = jsonData.get("basedOn");
				for (JsonNode basedOn : nullGuard(basedOnData)) {
					JsonNode value = basedOn.get("display");
					if (value != null && value.asText().startsWith(taskName + "-")) {
						taskIdString = value.asText().replace(taskName + "-", "");
						break;
					}
				}
			}
		}
		if (taskIdString != null) {
			return Long.valueOf(taskIdString);
		} else {
			LOG.warn("Could not parse taskId from json");
			return null;
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T extends Iterable> T nullGuard(T item) {
		return (item == null) ? (T) JsonNodeFactory.instance.arrayNode() : item;
	}

	/*
	 * This implementation uses a lot of reflection because the actual classloader that is holding the
	 * class references we need is not the webapp, but the service.
	 */
	private Task toJbpmTask(Task internalTask, String json, URLClassLoader classLoader) throws Exception {
		Class<?> ctxClass = classLoader.loadClass("ca.uhn.fhir.context.FhirContext");
		Object fhirCtx = ctxClass.getMethod("forR4").invoke(null);
		Object fhirParser = ctxClass.getMethod("newJsonParser").invoke(fhirCtx);
		Class<?> parserClass = classLoader.loadClass("ca.uhn.fhir.parser.IParser");
		Object fhirTask = parserClass.getMethod("parseResource", String.class).invoke(fhirParser, json);
		Class<?> listenerClass = classLoader.loadClass("io.elimu.task.fhir.r4.R4FhirTaskEventListener");
		Object listener = listenerClass.getConstructor(RuntimeManager.class, String.class, String.class, String.class).newInstance(null, null, null, null);
		for (Method m : listenerClass.getDeclaredMethods()) {
			if (m.getName().equals("toJbpmTask")) {
				return (Task) m.invoke(listener, fhirTask, internalTask);
			}
		}
		return null;
	}
}
