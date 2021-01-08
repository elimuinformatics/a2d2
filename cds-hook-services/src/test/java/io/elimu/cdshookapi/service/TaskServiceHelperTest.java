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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.definition.process.Node;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

import io.elimu.a2d2.genericmodel.ServiceRequest;
import io.elimu.a2d2.genericmodel.ServiceResponse;
import io.elimu.genericapi.service.GenericKieBasedService;
import io.elimu.genericapi.service.RunningServices;

@Ignore("Keeps failing because of expired hooks") //TODO
public class TaskServiceHelperTest {

	@Before
	public void setup() throws Exception {

		System.setProperty("cds.kie.db.className", "org.h2.jdbcx.JdbcDataSource");
		System.setProperty("cds.kie.db.url", "jdbc:h2:mem:test");
		System.setProperty("cds.kie.db.user", "sa");
		System.setProperty("cds.kie.db.password", "");
	}

	private RuntimeManager manager;

	public WorkflowProcessInstanceImpl getInstance() throws Exception {
		String processId = "single_pro_order";
		String jsonString = "{\"hook\":\"patient-view\",\"hookInstance\":\"d1577c69-dfbe-44ad-ba6d-3e05e953b2ea\","
				+ "\"fhirServer\":\"http://fhir3/baseDstu3\",\"user\":\"Practitioner/COREPRACTITIONER1\","
				+ "\"patient\":\"SMART-1288992\",\"context\":{\"proStartOffset\":1,\"patientId\":\"SMART-1288992\"},\"prefetch\":{}}";
		ServiceRequest request = new ServiceRequest();
		request.addHeaderValue("Content-Type", "application/json");
		request.setBody(jsonString);
		GenericKieBasedService service = new GenericKieBasedService("org.easi-pro.order:pro-single:1.0.4", "elimu");
		RunningServices.getInstance().register(service);
		this.manager = RuntimeManagerRegistry.get().getManager(service.getId());
		RuntimeEngine engine = this.manager.getRuntimeEngine(ProcessInstanceIdContext.get());
		KieSession ksession = engine.getKieSession();
		Map<String, Object> params = new HashMap<>();
		params.put("serviceRequest", request);
		params.put("serviceResponse", new ServiceResponse("Process is getting started", 200));
		WorkflowProcessInstanceImpl instance = (WorkflowProcessInstanceImpl) ksession.startProcess(processId, params);
		return instance;
	}

	@Test @Ignore("Task is always at reserved state by process definition structure. Ignoring this test until we have groups")
	public void claimTaskTest() throws Exception {

		WorkflowProcessInstanceImpl instance = getInstance();
		TaskService taskService = this.manager.getRuntimeEngine(ProcessInstanceIdContext.get(instance.getId())).getTaskService();
		Thread.sleep(30_000); //need to wait at least 15 seconds from timer for task to be created
		List<Long> tasklist = taskService.getTasksByProcessInstanceId(instance.getId());
		Assert.assertEquals(1, tasklist.size());
		Long taskId = tasklist.get(0);
		TaskServiceHelper taskServiceHelper = new TaskServiceHelper(taskId);
		taskServiceHelper.claimTask(taskId, (String) instance.getVariable("patientId"));
		Task task = taskServiceHelper.getTask(taskId);
		Assert.assertEquals(Status.Reserved, task.getTaskData().getStatus());
	}

	@Test
	public void startTaskTest() throws Exception {
		WorkflowProcessInstanceImpl instance = getInstance();
		RuntimeEngine engine = this.manager.getRuntimeEngine(ProcessInstanceIdContext.get(instance.getId()));
		TaskService taskService = engine.getTaskService();
		RuleFlowProcess proc = (RuleFlowProcess) engine.getKieSession().getKieBase().getProcess(instance.getProcessId());
		Node[] nodes = proc.getNodes();
		instance.getCompletedNodeIds();
		for (String uniqueId : instance.getCompletedNodeIds()) {
			for (Node node : nodes) {
				if (node.getMetaData().get("UniqueId").equals(uniqueId)) {
					System.out.println("NODE COMPLETED: " + node.getName());
					break;
				}
			}
		}
		Thread.sleep(30_000);//need to sleep over 15 seconds for task to execute
		List<Long> tasklist = taskService.getTasksByProcessInstanceId(instance.getId());
		Assert.assertTrue(tasklist.size() >= 1);
		Long taskId = tasklist.get(0);
		TaskServiceHelper taskServiceHelper = new TaskServiceHelper(taskId);
		taskServiceHelper.startTask(taskId, (String) instance.getVariable("patientId"));
		Task task = taskServiceHelper.getTask(taskId);
		Assert.assertEquals(Status.InProgress, task.getTaskData().getStatus());
	}

	@Test
	public void completeTaskTest() throws Exception {

		WorkflowProcessInstanceImpl instance = getInstance();
		TaskService taskService = this.manager.getRuntimeEngine(ProcessInstanceIdContext.get(instance.getId())).getTaskService();
		Thread.sleep(30_000); //need to wait at least 15 seconds from timer for task to be created
		List<Long> tasklist = taskService.getTasksByProcessInstanceId(instance.getId());
		Assert.assertNotNull(tasklist);
		Assert.assertEquals(1, tasklist.size());
		Task task = taskService.getTaskById(tasklist.get(0));
		Map<String, Object> resultData = task.getTaskData().getTaskInputVariables();
		assert (tasklist.size() > 0);
		if (tasklist.size() > 1) {
			Long taskId = tasklist.get(1);
			TaskServiceHelper taskServiceHelper = new TaskServiceHelper(taskId);
			taskServiceHelper.completeTask(taskId, null, resultData);
			Assert.assertEquals(Status.Completed, task.getTaskData().getStatus());
			TaskEntity entity = taskServiceHelper.buildTaskEntity(taskId);
			Assert.assertNotNull(entity);
			Assert.assertNotNull(entity.getTaskInputs());
			Assert.assertTrue(entity.getTaskInputs().size() > 0);
			Assert.assertNotNull(entity.getTaskOutputs());
			Assert.assertTrue(entity.getTaskOutputs().size() > 0);
		}
	}

	@Test
	public void getPatientTasksTest() throws Exception {
		WorkflowProcessInstanceImpl instance = getInstance();
		Thread.sleep(24_000); // it wil take 1 second to start the task
		List<TaskEntity> taskEntityList = new TaskServiceHelper().getTaskList((String) instance.getVariable("patientId"));
		Assert.assertEquals(1, taskEntityList.size());
		TaskEntity entity = taskEntityList.iterator().next();
		Assert.assertNotNull(entity);
		Assert.assertNotNull(entity.getTaskInputs());
		Assert.assertTrue(entity.getTaskInputs().size() > 0);
		Assert.assertNull(entity.getTaskOutputs());
	}
}
