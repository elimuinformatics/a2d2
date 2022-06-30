package io.elimu.a2d2.cql;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.execution.Library;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.opencds.cqf.cds.providers.PrefetchDataProviderR4;
import org.opencds.cqf.cql.engine.data.CompositeDataProvider;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.execution.CqlEngine;
import org.opencds.cqf.cql.engine.execution.EvaluationResult;
import org.opencds.cqf.cql.engine.execution.InMemoryLibraryLoader;
import org.opencds.cqf.cql.engine.execution.JsonCqlLibraryReader;
import org.opencds.cqf.cql.engine.fhir.model.R4FhirModelResolver;
import org.opencds.cqf.cql.engine.fhir.terminology.R4FhirTerminologyProvider;

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
			ModelManager modelManager = new ModelManager();
			LibraryManager libraryManager = new LibraryManager(modelManager);
			CqlTranslator translator = CqlTranslator.fromText(cql, modelManager, libraryManager);
			List<Library> libraries = Arrays.asList(JsonCqlLibraryReader.read(new StringReader(translator.toJxson())));
			Map<String, DataProvider> dataProviders = new HashMap<>();
			R4FhirModelResolver resolver = new R4FhirModelResolver();
			PrefetchDataProviderR4 provider = new PrefetchDataProviderR4(new ArrayList<>(workItem.getParameters().values()), resolver);
			dataProviders.put("http://hl7.org/fhir", new CompositeDataProvider(resolver, provider));
			CqlEngine engine = new CqlEngine(new InMemoryLibraryLoader(libraries), dataProviders, new R4FhirTerminologyProvider());
			EvaluationResult result = engine.evaluate(libraries.get(0).getIdentifier(), parameters);
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
