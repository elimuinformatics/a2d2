package io.elimu.genericapi.service;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.kie.api.task.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.elimu.a2d2.cdsmodel.Dependency;
import io.elimu.a2d2.genericmodel.ServiceRequest;
import io.elimu.a2d2.genericmodel.ServiceResponse;
import io.elimu.a2d2.process.ServiceUtils;
import io.elimu.serviceapi.service.AppContextUtils;

public class GenericTempService implements GenericService {

	private static final Logger LOG = LoggerFactory.getLogger(GenericTempService.class);
	
	private String id;
	private String defaultCustomer;
	private Dependency dependency;
	private Properties config;
	
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
	public Properties getConfig() {
		if (this.config == null) {
			try {
				this.config = ServiceUtils.getConfig(getDependency(), AppContextUtils.getInstance().getProfileName());
			} catch (Exception e) {
				LOG.warn("COULD NOT INITIALIZE CONFIG ON DEMAND", e);
			}
		}
		return config;
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
