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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletResponse;

import org.jbpm.persistence.processinstance.ProcessInstanceInfo;
import org.jbpm.runtime.manager.impl.AbstractRuntimeManager;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.elimu.a2d2.genericmodel.ServiceRequest;
import io.elimu.a2d2.genericmodel.ServiceResponse;
import io.elimu.a2d2.web.WebUtils;
import io.elimu.cdshookapi.service.RuntimeManagerHelper;
import io.elimu.genericapi.service.GenericService;
import io.elimu.genericapi.service.RunningServices;
import io.swagger.annotations.Api;

@RestController
@CrossOrigin(maxAge = 6000)
@RequestMapping(value = "/api/v2")
@Api(tags = { "service-processes" })
public class ServiceProcessController extends AbstractRestHandler {

	private static final Logger log = LoggerFactory.getLogger(ServiceProcessController.class);
	private static final String ERROR_MSG = "Error executing process";
	private static final String PROCESS_NOT_FOUND = "Process instance id not found";

	@CrossOrigin
	@PostMapping(value = "/service-processes/defs/{processId}/start", produces = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ServiceResponse startProcess(@PathVariable("processId") String processId,
			@RequestBody ServiceRequest request) {
		ServiceResponse response = new ServiceResponse();
		try {
			RuntimeManager manager = RuntimeManagerHelper.findRuntimeManagerByProcessId(processId);
			if (manager != null) {
				GenericService service = RunningServices.getInstance().get(manager.getIdentifier());
				if (service != null) {
					response = service.execute(request);
				} else {
					response.setBody("Service id not found");
					response.setResponseCode(404);
				}
			} else {
				response.setBody("Process id not found");
				response.setResponseCode(404);
			}
			return response;
		} catch (Exception e) {
			log.error(ERROR_MSG, e);
			response.setBody("Error executing process: " + e.getMessage());
			response.setResponseCode(500);
			return response;
		}

	}

	@CrossOrigin
	@RequestMapping(value = "/service-processes/defs/{processId}/list-instances", method = RequestMethod.GET, produces = {
			"application/json", "application/xml" })
	@ResponseStatus(HttpStatus.OK)
	public void getProcessInstances(@PathVariable("processId") String processId, HttpServletResponse resp) {
		ServiceResponse response = new ServiceResponse();
		try {
			RuntimeManager manager = RuntimeManagerHelper.findRuntimeManagerByProcessId(processId);
			if (manager != null) {
				EntityManagerFactory emf = (EntityManagerFactory) ((AbstractRuntimeManager) manager).
						getEnvironment().getEnvironment().get(EnvironmentName.ENTITY_MANAGER_FACTORY);
				EntityManager em = emf.createEntityManager();
				List<ProcessInstanceInfo> instances = em.createQuery("select p from ProcessInstanceInfo p where p.processId = :pid", 
						ProcessInstanceInfo.class).setParameter("pid", processId).getResultList();
				List<Map<String, Object>> mapList = new ArrayList<>(instances.size());
				for (ProcessInstanceInfo info : instances) {
					Map<String, Object> map = new HashMap<>();
					map.put("id", info.getId());
					map.put("processId", info.getProcessId());
					map.put("status", getStatusFromProcess(info.getState()));
					mapList.add(map);
				}
				response.setBody(new Gson().toJson(mapList));
				em.close();
				response.addHeaderValue("Content-Type", "application/json");
				response.setResponseCode(200);
			} else {
				response.setBody("Process id not found");
				response.setResponseCode(404);
			}
			WebUtils.populate(resp, response);
		} catch (Exception e) {
			log.error("Error listing process instances", e);
			response.setBody("Error listing process instances: " + e.getMessage());
			response.setResponseCode(500);
			try {
				WebUtils.populate(resp, response);
			} catch (IOException ignore) {
				//no-op
			}
		}
	}

	@CrossOrigin
	@GetMapping(value = "/service-processes/instances/{processInstanceId}", produces = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ServiceResponse getProcessStatusByProcessInstance(
			@PathVariable("processInstanceId") Long processInstanceId) {
		ServiceResponse response = new ServiceResponse();
		try {
			WorkflowProcessInstanceImpl instance = findProcessInstance(processInstanceId);
			response.setBody("{ 'status': '" + getStatusFromProcess(instance.getState()) + "'}");
			response.addHeaderValue(HttpHeaders.CONTENT_TYPE , MediaType.APPLICATION_JSON_VALUE);
			response.setResponseCode(200);
		} catch (IllegalArgumentException e) {
			response.setBody(e.getMessage());
			response.addHeaderValue(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
			response.setResponseCode(500);
		}
		return response;
	}

	private WorkflowProcessInstanceImpl findProcessInstance(Long processInstanceId) {
		RuntimeManager manager = RuntimeManagerHelper.findRuntimeManagerByProcessInstanceId(processInstanceId);
		if (manager != null) {
			RuntimeEngine engine = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
			if (engine != null) {
				return (WorkflowProcessInstanceImpl) engine.getKieSession().getProcessInstance(processInstanceId);
			}
		}
		throw new IllegalArgumentException(PROCESS_NOT_FOUND);
	}

        @CrossOrigin
        @RequestMapping(value = "service-processes/instances/{processInstanceId}/abort", method = RequestMethod.POST, produces = {
                        "application/json", "application/xml" })
        @ResponseStatus(HttpStatus.OK)
        public @ResponseBody ServiceResponse abortProcess(@PathVariable("processInstanceId") Long processInstanceId) {
                ServiceResponse response = new ServiceResponse();
                try {
                        RuntimeManager manager = RuntimeManagerHelper.findRuntimeManagerByProcessInstanceId(processInstanceId);
                        if (manager == null) {
                                response.setBody("Process instance manager not found");
                                response.addHeaderValue("Content-Type", "text/plain");
                                response.setResponseCode(404);
                                return response;
                        }
                        RuntimeEngine engine = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
                        if (engine == null)  {
                                response.setBody("Process instance engine not found");
                                response.addHeaderValue("Content-Type", "text/plain");
                                response.setResponseCode(404);
                                return response;
                        }
                        ProcessInstance instance = engine.getKieSession().getProcessInstance(processInstanceId);
                        if (instance != null && instance.getState() == ProcessInstance.STATE_ACTIVE) {
                                engine.getKieSession().abortProcessInstance(processInstanceId);
                                response.setBody("{ 'status': '" + getStatusFromProcess(ProcessInstance.STATE_ABORTED) + "'}");
                                response.addHeaderValue("Content-Type", "application/json");
                                response.setResponseCode(200);
                        } else if (instance == null) {
                                response.setBody("Process instance id not found");
                                response.addHeaderValue("Content-Type", "text/plain");
                                response.setResponseCode(404);
                        } else {
                                response.setBody("Cant stop process instance that is in state " + getStatusFromProcess(instance.getState()));
                                response.addHeaderValue("Content-Type", "text/plain");
                                response.setResponseCode(400);
                        }
                        return response;
                } catch (Exception e) {
                        log.error("Error executing process", e);
                        response.setBody("{'status': 'Process already aborted or completed'}");
                        response.addHeaderValue("Content-Type", "application/json");
                        response.setResponseCode(500);
                        return response;
                }

        }

	@CrossOrigin
	@GetMapping(value = "service-processes/instances/{processInstanceId}/variables", produces = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ServiceResponse getProcessVariables(@PathVariable("processInstanceId") Long processInstanceId) {
		WorkflowProcessInstanceImpl instance = findProcessInstance(processInstanceId);
		ServiceResponse response = new ServiceResponse();
		if (instance != null) {
			Map<String, Object> variables = instance.getVariables();
			Gson gsonObj = new Gson();
			response.setBody(gsonObj.toJson(variables));
			response.addHeaderValue(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
			response.setResponseCode(200);
			return response;
		} else {
			response.setBody(PROCESS_NOT_FOUND);
			response.addHeaderValue(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
			response.setResponseCode(404);
			return response;
		}
	}

	@CrossOrigin
	@PostMapping(value = "service-processes/instances/{processInstanceId}/signal/{eventName}", produces = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody Object getSignalEvent(@PathVariable("processInstanceId") Long processInstanceId,
			@PathVariable("eventName") String eventName, @RequestBody JsonObject jsonObject) {
		ServiceResponse response = new ServiceResponse();
		try {
			WorkflowProcessInstanceImpl instance = findProcessInstance(processInstanceId);
			if (instance != null) {
				instance.getKnowledgeRuntime().signalEvent(eventName, jsonObject, processInstanceId);
				instance = findProcessInstance(processInstanceId);
				response.setBody("{'status':'" + getStatusFromProcess(instance.getState()) + "'}");
				response.addHeaderValue(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
				response.setResponseCode(200);
			} else {
				response.setBody("{'status':'Process instance id not found'}");
				response.addHeaderValue(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
				response.setResponseCode(404);
			}
		} catch (IllegalArgumentException e) {
			response.setBody(e.getMessage());
			response.addHeaderValue(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
			response.setResponseCode(500);
		} catch (Exception e) {
			log.error(ERROR_MSG, e);
		}
		return response;
	}

	private String getStatusFromProcess(int state) {
		String s = "";
		switch (state) {
		case ProcessInstance.STATE_ABORTED:
			s = "STATE_ABORTED"; break;
		case ProcessInstance.STATE_ACTIVE:
			s = "STATE_ACTIVE"; break;
		case ProcessInstance.STATE_COMPLETED:
			s = "STATE_COMPLETED"; break;
		case ProcessInstance.STATE_PENDING:
			s = "STATE_PENDING"; break;
		case ProcessInstance.STATE_SUSPENDED:
			s = "STATE_SUSPENDED"; break;
		default: 
			s = "STATE_UNKONWN"; break;
		}
		return s;
	}
	

	/*
	 * TODO implement the following
	 * 
	 * POST cds-processes/defs/{processId}/start body: CDSRequest return:
	 * CDSProcessResponse about process status
	 * 
	 * GET cds-processes/instances/{processInstanceId} return: CDSProcessResponse
	 * about process status
	 * 
	 * POST cds-processes/instances/{processInstanceId}/signal/{eventName} body:
	 * JSON body of the signal component return: CDSProcessResponse about process
	 * status
	 * 
	 * POST cds-processes/instances/{processInstanceId}/abort return:
	 * CDSProcessResponse about process status
	 * 
	 * GET cds-processes/instances/{processInstanceId}/variables return: JSON array
	 * of the process variables
	 * 
	 * 
	 * IMPORTANT TIPS:
	 * RuntimeManagerHelper.findRuntimeManagerByProcessId(processId).getKieSession()
	 * .startProcess(processId, mapOfParameters)
	 * RuntimeManagerHelper.findRuntimeManagerByInstanceId(processInstanceId).
	 * getKieSession().getProcessInstance(processInstanceId)
	 * RuntimeManagerHelper.findRuntimeManagerByInstanceId(processInstanceId).
	 * getKieSession().signalEvent(eventName, mapOfParameters)
	 * RuntimeManagerHelper.findRuntimeManagerByInstanceId(processInstanceId).
	 * getKieSession().abortProcessInstance(processInstanceId) Map<String, Object>
	 * variables = ((WorkflowProcessInstanceImpl) processInstance).getVariables()
	 */

}
