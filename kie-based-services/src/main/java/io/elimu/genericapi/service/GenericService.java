package io.elimu.genericapi.service;

import io.elimu.a2d2.genericmodel.ServiceRequest;
import io.elimu.a2d2.genericmodel.ServiceResponse;

import org.kie.api.task.model.Task;

import io.elimu.a2d2.cdsmodel.Dependency;

public interface GenericService {

	String getId();
	ServiceResponse execute(ServiceRequest request) throws GenericServiceException;
	Dependency getDependency();
	String getDefaultCustomer();
	void updateTask(Task task) throws GenericServiceException;
	Task getTask(Long taskId);
}
