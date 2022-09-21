package io.elimu.a2d2.cql;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

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
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Class<?> modelManagerClass = cl.loadClass("org.cqframework.cql.cql2elm.ModelManager");
			Object modelManager = modelManagerClass.getConstructor().newInstance();
			Class<?> libraryManagerClass = cl.loadClass("org.cqframework.cql.cql2elm.LibraryManager");
			Object libraryManager = libraryManagerClass.getConstructor(modelManagerClass).newInstance(modelManager);
			Class<?> cqlTranslatorClass = cl.loadClass("org.cqframework.cql.cql2elm.CqlTranslator");
			Object translator = extractMethod(cl, cqlTranslatorClass, "fromText", 4).invoke(null, cql, modelManager, libraryManager, null);
			String translatorJson = (String) translator.getClass().getMethod("toJxson").invoke(translator);
			List<?> libraries = Arrays.asList(cl.loadClass("org.opencds.cqf.cql.engine.execution.JsonCqlLibraryReader").getMethod("read", Reader.class).invoke(null, new StringReader(translatorJson)));
			Map<String, Object> dataProviders = new HashMap<>();
			Class<?> modelResolverBaseClass = cl.loadClass("org.opencds.cqf.cql.engine.model.ModelResolver");
			Object resolver = cl.loadClass("org.opencds.cqf.cql.engine.fhir.model.R4FhirModelResolver").getConstructor().newInstance();
			Object provider = cl.loadClass("org.opencds.cqf.cds.providers.PrefetchDataProviderR4").getConstructor(
					List.class, modelResolverBaseClass).
					newInstance(new ArrayList<>(workItem.getParameters().values()), resolver);
			Object compDataProvider = cl.loadClass("org.opencds.cqf.cql.engine.data.CompositeDataProvider").
					getConstructor(modelResolverBaseClass,
							cl.loadClass("org.opencds.cqf.cql.engine.retrieve.RetrieveProvider")).
					newInstance(resolver, provider);
			dataProviders.put("http://hl7.org/fhir", compDataProvider);
			Class<?> termProviderBaseClass = cl.loadClass("org.opencds.cqf.cql.engine.terminology.TerminologyProvider");
			Class<?> libraryLoaderBaseClass = cl.loadClass("org.opencds.cqf.cql.engine.execution.LibraryLoader");
			Object termProvider = cl.loadClass("org.opencds.cqf.cql.engine.fhir.terminology.R4FhirTerminologyProvider").getConstructor().newInstance();
			Object engine = cl.loadClass("org.opencds.cqf.cql.engine.execution.CqlEngine").getConstructor(libraryLoaderBaseClass, Map.class, termProviderBaseClass)
					.newInstance(cl.loadClass("org.opencds.cqf.cql.engine.execution.InMemoryLibraryLoader").getConstructor(Collection.class).newInstance(libraries), 
							dataProviders, termProvider);
			Object result = engine.getClass().getMethod("evaluate", cl.loadClass("org.cqframework.cql.elm.execution.VersionedIdentifier"), Map.class).
					invoke(engine, libraries.get(0).getClass().getMethod("getIdentifier").invoke(libraries.get(0)), parameters);
			Map<String, Object> results = workItem.getResults();
			results.putAll((Map<? extends String,? extends Object>) result.getClass().getField("expressionResults").get(result));
			manager.completeWorkItem(workItem.getId(), results);
		} catch (Exception e) {
			throw new WorkItemHandlerException("Error executing CQL", e);
		}
	}

	private Method extractMethod(ClassLoader cl, Class<?> clazz, String methodName, int paramCount) {
		for (Method m : clazz.getMethods()) {
			if (m.getName().equals(methodName)) {
				if (m.getParameterCount() == paramCount) {
					return m;
				}
			}
		}
		return null;
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// do nothing
	}
}
