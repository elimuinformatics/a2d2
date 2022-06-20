package io.elimu.a2d2.cql;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cqframework.cql.elm.execution.Library;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.opencds.cqf.cql.engine.execution.CqlEngine;
import org.opencds.cqf.cql.engine.execution.EvaluationResult;
import org.opencds.cqf.cql.engine.execution.InMemoryLibraryLoader;
import org.opencds.cqf.cql.engine.execution.JsonCqlLibraryReader;

import io.elimu.a2d2.exception.WorkItemHandlerException;

public class CqlEngineWorkItemHandler implements WorkItemHandler {

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		try {
			String cql = (String) workItem.getParameter("cql");
			Map<String, Object> parameters = new HashMap<>();
			for (String key : workItem.getParameters().keySet()) {
				if (key.startsWith("cql_param_")) {
					parameters.put(key.replace("cql_param_",  ""), workItem.getParameter(key));
				}
			}
			List<Library> libraries = new ArrayList<>();
			Library library = JsonCqlLibraryReader.read(new StringReader(cql));
			libraries.add(library);
			CqlEngine engine = new CqlEngine(new InMemoryLibraryLoader(libraries));
			EvaluationResult result = engine.evaluate(library.getIdentifier(), parameters);
			Map<String, Object> results = workItem.getResults();
			results.putAll(result.expressionResults);
			manager.completeWorkItem(workItem.getId(), results);
		} catch (IOException e) {
			throw new WorkItemHandlerException("Error parsing CQL", e);
		}
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// do nothing
	}
}
