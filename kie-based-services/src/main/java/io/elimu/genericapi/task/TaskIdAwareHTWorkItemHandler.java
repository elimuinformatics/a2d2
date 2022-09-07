package io.elimu.genericapi.task;

import java.util.Date;
import java.util.Map;

import org.jbpm.services.task.prediction.PredictionServiceRegistry;
import org.jbpm.services.task.utils.OnErrorAction;
import org.jbpm.services.task.wih.LocalHTWorkItemHandler;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.task.model.Task;
import org.kie.internal.runtime.manager.InternalRuntimeEngine;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.internal.task.api.prediction.PredictionOutcome;
import org.kie.internal.task.api.prediction.PredictionService;
import org.kie.internal.task.exception.TaskException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.elimu.serviceapi.service.RuntimeEngineAware;

public class TaskIdAwareHTWorkItemHandler extends LocalHTWorkItemHandler implements RuntimeEngineAware {

    private static final Logger logger = LoggerFactory.getLogger(TaskIdAwareHTWorkItemHandler.class);
    private RuntimeManager runtimeManager;
    private PredictionService predictionService = PredictionServiceRegistry.get().getService();

    @Override
    public RuntimeManager getRuntimeManager() {
        return runtimeManager;
    }

    @Override
    public void setRuntimeManager(RuntimeManager runtimeManager) {
        this.runtimeManager = runtimeManager;
        super.setRuntimeManager(runtimeManager);
    }

    public TaskIdAwareHTWorkItemHandler() {
    }


	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        RuntimeEngine runtime = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get(workItem.getProcessInstanceId()));
        KieSession ksessionById = ((InternalRuntimeEngine) runtime).internalGetKieSession();

        Task task = createTaskBasedOnWorkItemParams(ksessionById, workItem);
        Map<String, Object> content = createTaskDataBasedOnWorkItemParams(ksessionById, workItem);
        try {

            PredictionOutcome outcome = predictionService.predict(task, content);

            if (outcome.isCertain()) {
                logger.debug("Prediction service returned certain outcome (confidence level {}) for task {}, completing directly",
                        outcome.getConfidenceLevel(), task);

                manager.completeWorkItem(workItem.getId(), outcome.getData());
            } else if (outcome.isPresent()) {
                logger.debug("Prediction service returned uncertain outcome (confidence level {}) for task {}, setting suggested data",
                        outcome.getConfidenceLevel(), task);

                long taskId = createTaskInstance((InternalTaskService) runtime.getTaskService(), task, workItem, ksessionById, content);
                if (workItem.getParameter("signalName") != null) {
                	runtime.getKieSession().signalEvent(workItem.getParameter("signalName").toString(), taskId, workItem.getProcessInstanceId());
                }
                ((InternalTaskService) runtime.getTaskService()).setOutput(taskId, ADMIN_USER, outcome.getData());
            } else {
                logger.debug("Not outcome present from prediction service, creating user task");
                long taskId = createTaskInstance((InternalTaskService) runtime.getTaskService(), task, workItem, ksessionById, content);
                if (workItem.getParameter("signalName") != null) {
                	runtime.getKieSession().signalEvent(workItem.getParameter("signalName").toString(), taskId, workItem.getProcessInstanceId());
                }
            }
        } catch (Exception e) {
            if (action.equals(OnErrorAction.ABORT)) {
                manager.abortWorkItem(workItem.getId());
            } else if (action.equals(OnErrorAction.RETHROW)) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new RuntimeException(e);
                }
            } else if (action.equals(OnErrorAction.LOG)) {
                StringBuilder logMsg = new StringBuilder();
                logMsg.append(new Date()).append(": Error when creating task on task server for work item id ").append(workItem.getId());
                logMsg.append(". Error reported by task server: ").append(e.getMessage());
                logger.error(logMsg.toString(), e);
                // rethrow to cancel processing if the exception is not recoverable
                if (!(e instanceof TaskException) || ((e instanceof TaskException) && !((TaskException) e).isRecoverable())) {
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    } else {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
	}
}
