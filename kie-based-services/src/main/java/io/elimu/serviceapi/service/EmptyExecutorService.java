package io.elimu.serviceapi.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ErrorInfo;
import org.kie.api.executor.ExecutorService;
import org.kie.api.executor.RequestInfo;
import org.kie.api.executor.STATUS;
import org.kie.api.runtime.query.QueryContext;

public class EmptyExecutorService implements ExecutorService {

	@Override
	public List<RequestInfo> getQueuedRequests(QueryContext queryContext) {
		return null;
	}

	@Override
	public List<RequestInfo> getCompletedRequests(QueryContext queryContext) {
		return null;
	}

	@Override
	public List<RequestInfo> getInErrorRequests(QueryContext queryContext) {
		return null;
	}

	@Override
	public List<RequestInfo> getCancelledRequests(QueryContext queryContext) {
		return null;
	}

	@Override
	public List<ErrorInfo> getAllErrors(QueryContext queryContext) {
		return null;
	}

	@Override
	public List<RequestInfo> getAllRequests(QueryContext queryContext) {
		return null;
	}

	@Override
	public List<RequestInfo> getRequestsByStatus(List<STATUS> statuses, QueryContext queryContext) {
		return null;
	}

	@Override
	public List<RequestInfo> getRequestsByBusinessKey(String businessKey, QueryContext queryContext) {
		return null;
	}

	@Override
	public List<RequestInfo> getRequestsByBusinessKey(String businessKey, List<STATUS> statuses,
			QueryContext queryContext) {
		return null;
	}

	@Override
	public List<RequestInfo> getRequestsByCommand(String command, QueryContext queryContext) {
		return null;
	}

	@Override
	public List<RequestInfo> getRequestsByCommand(String command, List<STATUS> statuses, QueryContext queryContext) {
		return null;
	}

	@Override
	public List<RequestInfo> getRequestsByDeployment(String deploymentId, List<STATUS> statuses,
			QueryContext queryContext) {
		return null;
	}

	@Override
	public List<RequestInfo> getRequestsByProcessInstance(Long processInstanceId, List<STATUS> statuses,
			QueryContext queryContext) {
		return null;
	}

	@Override
	public int clearAllRequests() {
		return 0;
	}

	@Override
	public int clearAllErrors() {
		return 0;
	}

	@Override
	public Long scheduleRequest(String commandName, CommandContext ctx) {
		return null;
	}

	@Override
	public void cancelRequest(Long requestId) {
		
	}

	@Override
	public void updateRequestData(Long requestId, Map<String, Object> data) {
		
	}

	@Override
	public void init() {
		
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public boolean isActive() {
		return false;
	}

	@Override
	public int getInterval() {
		return 0;
	}

	@Override
	public void setInterval(int waitTime) {
		
	}

	@Override
	public int getRetries() {
		return 0;
	}

	@Override
	public void setRetries(int defaultNroOfRetries) {
		
	}

	@Override
	public int getThreadPoolSize() {
		return 0;
	}

	@Override
	public void setThreadPoolSize(int nroOfThreads) {
		
	}

	@Override
	public TimeUnit getTimeunit() {
		return null;
	}

	@Override
	public void setTimeunit(TimeUnit timeunit) {
		
	}

	@Override
	public List<RequestInfo> getPendingRequests(QueryContext queryContext) {
		return null;
	}

	@Override
	public List<RequestInfo> getPendingRequestById(Long id) {
		return null;
	}

	@Override
	public Long scheduleRequest(String commandId, Date date, CommandContext ctx) {
		return null;
	}

	@Override
	public List<RequestInfo> getRunningRequests(QueryContext queryContext) {
		return null;
	}

	@Override
	public List<RequestInfo> getFutureQueuedRequests(QueryContext queryContext) {
		return null;
	}

	@Override
	public RequestInfo getRequestById(Long requestId) {
		return null;
	}

	@Override
	public List<ErrorInfo> getErrorsByRequestId(Long requestId) {
		return null;
	}

}
