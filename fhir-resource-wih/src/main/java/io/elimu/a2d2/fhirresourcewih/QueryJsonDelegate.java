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

package io.elimu.a2d2.fhirresourcewih;

import java.util.Map;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import io.elimu.a2d2.exception.WorkItemHandlerException;

public class QueryJsonDelegate implements WorkItemHandler {

	private static final Logger log = LoggerFactory.getLogger(QueryJsonDelegate.class);

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String jsonString = (String) workItem.getParameter("json");
		String query = (String) workItem.getParameter("query");
		String resultData = null;
		Map<String, Object> results = workItem.getResults();

		try {
			if (jsonString == null || query == null ||
					jsonString.trim().equals("") || query.trim().equals("") ) {
				log.error("Either json or query is null");
				throw new WorkItemHandlerException("Either json or query is null");
			}

			Gson gson = new Gson();
			Object document = Configuration.defaultConfiguration().jsonProvider().parse(jsonString);
			Object obj = JsonPath.read(document, query);
			resultData = gson.toJson(obj);
			results.put("result", resultData);
			manager.completeWorkItem(workItem.getId(), results);
			log.trace("QueryJsonDelegate execution is complete");

		} catch (InvalidJsonException ex) {

			log.error("invalid json jsonString:" + jsonString);
			throw new WorkItemHandlerException("The input json parameter is not valid json :: "+jsonString,ex);

		} catch (PathNotFoundException ex) {

			log.error("Query/Jsonpath is not valid :" + query);
			throw new WorkItemHandlerException("The query syntax is incorrect ::"+ query,ex);

		} catch (Exception ex) {
			log.error("Exception in Query or json :" + ex);
			throw new WorkItemHandlerException("Query is not valid",ex);

		}
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		manager.abortWorkItem(workItem.getId());
	}

}
