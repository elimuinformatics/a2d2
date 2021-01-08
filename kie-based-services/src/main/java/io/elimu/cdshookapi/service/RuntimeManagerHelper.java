package io.elimu.cdshookapi.service;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.jbpm.persistence.processinstance.ProcessInstanceInfo;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Task;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.elimu.genericapi.service.GenericKieBasedService;
import io.elimu.genericapi.service.RunningServices;

public class RuntimeManagerHelper {
	
	private static final Logger log = LoggerFactory.getLogger(RuntimeManagerHelper.class);

	private RuntimeManagerHelper() {
		throw new IllegalStateException("Utility class");
	}
	
	public static RuntimeManager findRuntimeManagerByProcessId(String processId) {
		for (String serviceName : RunningServices.getInstance().serviceNames()) {
			GenericKieBasedService service = (GenericKieBasedService) RunningServices.getInstance().get(serviceName);
			for (org.kie.api.definition.process.Process p : service.getKieBase().getProcesses()) {
				if (p.getId().equalsIgnoreCase(processId)) {
					return RuntimeManagerRegistry.get().getManager(service.getId());
				}
			}
		}
		return null;
	}

	public static RuntimeManager findRuntimeManagerByProcessInstanceId(Long instanceId) {
		Set<String> serviceNames = RunningServices.getInstance().serviceNames();
		EntityManagerFactory emf = null;
		for (String serviceName : serviceNames) {
			//we need to find a service that is persistent
			RuntimeManager anyManager = RuntimeManagerRegistry.get().getManager(serviceName);
			RuntimeEnvironment renv = ((InternalRuntimeManager) anyManager).getEnvironment();
			emf = (EntityManagerFactory) renv.getEnvironment().get(EnvironmentName.ENTITY_MANAGER_FACTORY);
			if (emf != null) {
				break;
			}
		}
		if (emf == null) {
			//no persistent services, so no EMF already built
			return null;
		}
		EntityManager em = emf.createEntityManager();
		try {
			ProcessInstanceInfo info = em.find(ProcessInstanceInfo.class, instanceId);
			return findRuntimeManagerByProcessId(info.getProcessId());
		} catch (RuntimeException e) {
			log.error("Exception occured due to {}", e.getMessage(), e);
			return null;
		} finally {
			em.close();
		}
	}
	
	public static RuntimeManager findRuntimeManagerByTaskId(Long taskId) {
	
		Task task = null;
		
		for(String serviceName: RunningServices.getInstance().serviceNames()) {
			try {
				RuntimeManager anyManager = RuntimeManagerRegistry.get().getManager(serviceName);
				TaskService taskService = anyManager.getRuntimeEngine(ProcessInstanceIdContext.get()).getTaskService();
				task = taskService.getTaskById(taskId);
				break;
			} catch(Exception e) {
				log.error("Task Service is not configured for :: " + serviceName);
			}
		}
		
		if (task == null) {
			return null;
		}
		//from the deployment ID of the task we can get the right manager
		return RuntimeManagerRegistry.get().getManager(task.getTaskData().getDeploymentId());
	}
}
