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

package io.elimu.serviceapi.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.elimu.genericapi.service.GenericKieBasedService;
import io.elimu.service.dao.jpa.CDSServiceRepository;
import io.elimu.service.models.ServiceInfo;

@Component
public class ServiceStoreImpl implements ServiceStore {

	private static final Logger log = LoggerFactory.getLogger(ServiceStoreImpl.class);

	private static ServiceStoreImpl INSTANCE;

	@Autowired
	private CDSServiceRepository cdsRepository;

	@Autowired
	private List<CreateSubscription> createSubscriptions;

	private ServiceStoreImpl() {
		INSTANCE = this;
	}

	public static synchronized ServiceStoreImpl getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ServiceStoreImpl();
		}
		return INSTANCE;
	}

	@Override
	public void markUpdate(GenericKieBasedService service) {
		ServiceInfo lastVersion = cdsRepository.findLastVersion(service.getId());
		Long versionNumber = 1L;
		if (lastVersion != null) {
			versionNumber = lastVersion.getId().getVersion() + 1L;
		}
		cdsRepository.save(new ServiceInfo(service.getId(), versionNumber,
				service.getDependency().getExternalForm(), "generic",
				service.getDefaultCustomer(), service.getServiceCategory(),
				"in-progress"));
		if(service.getDefaultCustomer()==null) {
			log.error("kie-default customer is null");
			return;
		}
		createSubscriptions.forEach(e -> e.create(service, "generic"));
	}

	@Override
	public void updateFailed(String id) {
		_update(id, "failed");
	}

	@Override
	public void updateDone(GenericKieBasedService service) {
		_update(service.getId(), "done");
	}


	private void _update(String serviceId, String newStatus) {
		ServiceInfo info = cdsRepository.findLastVersionDoneOrNot(serviceId);
		if (info == null) {
			log.error("No service " + serviceId + " found in the database. Aborting '" + newStatus + "' marking");
			return;
		}
		if (!"in-progress".equalsIgnoreCase(info.getStatus())) {
			log.error("Service " + serviceId + " found in the database has status " + info.getStatus() + " instead of in-progress. Aborting '" + newStatus + "' marking");
			return;
		}
		info.setStatus("done");
		cdsRepository.save(info);

	}

	@Override
	public int delete(String id) {
		return cdsRepository.deleteByID(id);

	}
}
