package io.elimu.genericapi.service;

import java.util.Arrays;
import java.util.List;

import org.kie.api.task.model.Task;

import io.elimu.a2d2.cdsmodel.Dependency;
import io.elimu.a2d2.genericmodel.ServiceRequest;
import io.elimu.a2d2.genericmodel.ServiceResponse;

public class GenericTempService implements GenericService {

	private String id;
	private String defaultCustomer;
	private Dependency dependency;

	public GenericTempService(String releaseId, String defaultCustomer) {
		this.defaultCustomer = defaultCustomer;
		if (releaseId == null || releaseId.split(":").length < 3) {
			throw new IllegalArgumentException("releaseId '" + releaseId + "' is not a valid release ID");
		}
		String[] parts = releaseId.split(":");
		this.id = parts[1];
		this.dependency = new Dependency(parts[0], parts[1], parts[2]);
	}

	@Override
	public Dependency getDependency() {
		return dependency;
	}
	
	@Override
	public String getDefaultCustomer() {
		return defaultCustomer;
	}
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public ServiceResponse execute(ServiceRequest request) throws GenericServiceException {
		return new ServiceResponse("Service is still starting up. Please try again in a few minutes", 503);
	}

	@Override
	public void updateTask(Task task) throws GenericServiceException {
		//do nothing
	}
	
	@Override
	public Task getTask(Long taskId) {
		return null;
	}
	
	@Override
	public List<String> getOtherCustomers() {
		return Arrays.asList(getDefaultCustomer());
	}
}
