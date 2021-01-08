package io.elimu.serviceapi.service;

import io.elimu.genericapi.service.GenericKieBasedService;

//mock implementation for testing
public class ServiceStoreImpl implements ServiceStore {

	private static final ServiceStoreImpl INSTANCE = new ServiceStoreImpl();

	public static ServiceStoreImpl getInstance() {
		return INSTANCE;
	}
	
	private ServiceStoreImpl() {
	}
	
	@Override
	public int delete(String id) {
		//do nothing
		return 0;
	}

	@Override
	public void updateFailed(String id) {
		//do nothing
	}

	@Override
	public void markUpdate(GenericKieBasedService service) {
		//do nothing
	}

	@Override
	public void updateDone(GenericKieBasedService service) {
		//do nothing
	}
}
