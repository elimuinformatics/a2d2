package io.elimu.a2d2.cql;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.execution.Library;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.opencds.cqf.cds.hooks.R4HookEvaluator;
import org.opencds.cqf.cds.response.CdsCard;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.execution.InMemoryLibraryLoader;
import org.opencds.cqf.cql.engine.execution.JsonCqlLibraryReader;
import org.opencds.cqf.cql.engine.fhir.model.R4FhirModelResolver;
import org.opencds.cqf.cql.engine.model.ModelResolver;

import com.google.gson.Gson;

import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.PlanDefinition;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.SimpleRequestHeaderInterceptor;

public class PlanDefCdsInlineWorkItemHandler implements WorkItemHandler {

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		Map<String, Object> results = workItem.getResults();
		String fhirServerUrl = (String) workItem.getParameter("fhirServerUrl");
		String planDefId = (String) workItem.getParameter("planDefinitionId");
		String planDefUrl = (String) workItem.getParameter("planDefinitionUrl");
		String patientId = (String) workItem.getParameter("patientId");
		//TODO validate required fhirServerUrl
		//TODO validate required planDefinitionId or planDefinitionUrl
		//TODO validate required patientId
		String fhirServerAuth = (String) workItem.getParameter("fhirServerAuth");
		Map<String, Object> prefetchData = new HashMap<>();
		Map<String, String> contextData = new HashMap<>();
		for (String key : workItem.getParameters().keySet()) {
			if (key.startsWith("context_")) {
				contextData.put(key.replace("context_", ""), String.valueOf(workItem.getParameter(key)));
			}
			if (key.startsWith("prefetch_")) {
				prefetchData.put(key.replace("prefetch_", ""), workItem.getParameter(key));
			}
		}
		IGenericClient fhirClient = FhirContext.forR4().newRestfulGenericClient(fhirServerUrl);
		PlanDefinition planDefinition = null;
		if (planDefId != null) {
			planDefinition = fhirClient.fetchResourceFromUrl(PlanDefinition.class, fhirServerUrl + "/PlanDefinition/" + planDefId);
			if (planDefinition == null) {
				//TODO error
			}
		} else {
			Bundle bundle = fhirClient.fetchResourceFromUrl(Bundle.class, fhirServerUrl + "/PlanDefinition?url=" + planDefUrl);
			if (!bundle.getEntryFirstRep().hasResource()) {
				//TODO error
			}
			planDefinition = (PlanDefinition) bundle.getEntryFirstRep().getResource();
		}
		Bundle bundle2 = fhirClient.fetchResourceFromUrl(Bundle.class, fhirServerUrl + "/Library");
		List<org.hl7.fhir.r4.model.Library> r4libraries = bundle2.getEntry().stream().map(e -> { return (org.hl7.fhir.r4.model.Library) e.getResource(); }).collect(Collectors.toList());
		List<Library> libraries = new LinkedList<>();
		ModelManager modelManager = new ModelManager();
		LibraryManager libraryManager = new LibraryManager(modelManager);
		for (org.hl7.fhir.r4.model.Library library : r4libraries) {
			for (Attachment content : library.getContent()) {
				if ("text/cql".equals(content.getContentType())) {
					try {
						byte[] base64Data = content.getData();
						String cql = new String(Base64.decodeBase64(base64Data));
						CqlTranslator translator = CqlTranslator.fromText(cql, modelManager, libraryManager);
						libraries.add(JsonCqlLibraryReader.read(new StringReader(translator.toJxson())));
					} catch (IOException e) {
						//TODO error
					}
				}
			}
		}
		Context context = new Context(libraries.get(0));
		context.registerLibraryLoader(new InMemoryLibraryLoader(libraries));
		if (fhirServerAuth != null && !"".equals(fhirServerAuth.trim())) {
			fhirClient.registerInterceptor(new SimpleRequestHeaderInterceptor("Authorization", fhirServerAuth));
		}
		ModelResolver resolver = new R4FhirModelResolver();
		R4HookEvaluator evaluator = new R4HookEvaluator(resolver);
		List<CdsCard> cards = evaluator.evaluateCdsHooksPlanDefinition(context, planDefinition, patientId, fhirClient);
		results.put("cardsJson", new Gson().toJson(cards));
		manager.completeWorkItem(workItem.getId(), results);
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// do nothing
	}

}
