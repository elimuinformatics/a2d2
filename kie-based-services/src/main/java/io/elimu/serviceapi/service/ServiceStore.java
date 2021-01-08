package io.elimu.serviceapi.service;

import io.elimu.genericapi.service.GenericKieBasedService;

public interface ServiceStore {

	static ServiceStore get() {
		try {
			Class<?> c = Class.forName("io.elimu.serviceapi.service.ServiceStoreImpl");
			return (ServiceStore) c.getMethod("getInstance").invoke(null);
		} catch (Exception e) {
			throw new RuntimeException("Couldn't obtain ServiceStoreImpl instance", e);
		}
	}

	int delete(String id);

	void updateFailed(String id);

	void markUpdate(GenericKieBasedService service);

	void updateDone(GenericKieBasedService service);

}
