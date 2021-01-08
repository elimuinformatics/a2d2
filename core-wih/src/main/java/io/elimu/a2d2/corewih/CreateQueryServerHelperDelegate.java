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

/**
 *
 */
package io.elimu.a2d2.corewih;

import java.util.Map;
//import java.util.Optional;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import io.elimu.a2d2.cds.fhir.helper.QueryingServerHelperBase;
//import io.elimu.a2d2.cds.fhir.helper.r4.QueryingServerHelper;
//import io.elimu.a2d2.exception.WorkItemHandlerException;

/**
 * @author niraj
 *
 */
public class CreateQueryServerHelperDelegate  implements WorkItemHandler {


/*	private static final String FHIR3 = "FHIR3";
	private static final String FHIR4 = "FHIR4";
	private static final String BASE_URL = "baseUrl";
	private static final String TOKEN = "token";
	private static final String FHIR_VERSION = "fhirVersion";
	private static final String HELPER = "helper";
*/
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
/*
		Optional<String> fhirUrl = Optional.ofNullable((String)workItem.getParameter(BASE_URL));
		Optional<String> token = Optional.ofNullable((String)workItem.getParameter(TOKEN));
		Optional<String> fhirVersion = Optional.ofNullable((String)workItem.getParameter(FHIR_VERSION));*/
		Map<String, Object> workItemResult = workItem.getResults();
/*		QueryingServerHelperBase<?, ?> queryingserverHelper;

		if(!fhirUrl.isPresent()) {
			logger.error("Base Url is missing : Parameter baseUrl");
			throw new WorkItemHandlerException("Base Url is missing : Parameter baseUrl");
		}
		logger.debug("FhirUrl is " + fhirUrl.get());
		queryingserverHelper = getQueringServerHelper(fhirUrl, fhirVersion, token);

		workItemResult.put(HELPER, queryingserverHelper);*/
		manager.completeWorkItem(workItem.getId(), workItemResult);

		logger.trace("CreateQueryServerHelper delegate completed");
	}

/*
	private QueryingServerHelperBase<?, ?> getQueringServerHelper(Optional<String> fhirUrl, Optional<String> fhirVersion, Optional<String> token) {
		QueryingServerHelperBase<?, ?> queryingserverHelper = null;
		if(fhirVersion.isPresent()) {
			logger.debug("FhirVersion is {}", fhirVersion.get());
		}
		if(fhirVersion.get().equalsIgnoreCase(FHIR4)) {
			queryingserverHelper = new  io.elimu.a2d2.cds.fhir.helper.r4.QueryingServerHelper(fhirUrl.get());
		} else if(fhirVersion.get().equalsIgnoreCase(FHIR3)) {
			queryingserverHelper = new  io.elimu.a2d2.cds.fhir.helper.dstu3.QueryingServerHelper(fhirUrl.get());
		} else {
			queryingserverHelper = new  io.elimu.a2d2.cds.fhir.helper.QueryingServerHelper(fhirUrl.get());
		}
		if(token.isPresent()) {
			queryingserverHelper.
			addAuthentication(QueryingServerHelper.BEARER, token.get());
		}
		return queryingserverHelper;
	}

*/
	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		manager.abortWorkItem(workItem.getId());
		logger.warn("CreateQueryServerHelper delegate Aborted");
	}

}
