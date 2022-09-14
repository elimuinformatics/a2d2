package io.elimu.a2d2.cql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.PlanDefinition;
import org.hl7.fhir.r4.model.RequestGroup;
import org.hl7.fhir.r4.model.Resource;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.opencds.cqf.cds.response.CdsCard;
import org.opencds.cqf.cds.response.CdsCard.Links;
import org.opencds.cqf.cds.response.CdsCard.Suggestions;
import org.opencds.cqf.cds.response.CdsCard.Suggestions.Action;
import org.opencds.cqf.cds.response.R4CarePlanToCdsCard;
import org.opencds.cqf.cql.engine.exception.CqlException;
import org.opencds.cqf.cql.engine.execution.CqlEngine;
import org.opencds.cqf.cql.engine.fhir.converter.FhirTypeConverter;
import org.opencds.cqf.cql.engine.fhir.converter.FhirTypeConverterFactory;
import org.opencds.cqf.cql.evaluator.CqlOptions;
import org.opencds.cqf.cql.evaluator.activitydefinition.r4.ActivityDefinitionProcessor;
import org.opencds.cqf.cql.evaluator.builder.Constants;
import org.opencds.cqf.cql.evaluator.builder.CqlEvaluatorBuilder;
import org.opencds.cqf.cql.evaluator.builder.EndpointConverter;
import org.opencds.cqf.cql.evaluator.builder.ModelResolverFactory;
import org.opencds.cqf.cql.evaluator.builder.dal.FhirRestFhirDalFactory;
import org.opencds.cqf.cql.evaluator.builder.data.DataProviderFactory;
import org.opencds.cqf.cql.evaluator.builder.data.FhirModelResolverFactory;
import org.opencds.cqf.cql.evaluator.builder.data.FhirRestRetrieveProviderFactory;
import org.opencds.cqf.cql.evaluator.builder.data.TypedRetrieveProviderFactory;
import org.opencds.cqf.cql.evaluator.builder.library.FhirRestLibraryContentProviderFactory;
import org.opencds.cqf.cql.evaluator.builder.library.LibraryContentProviderFactory;
import org.opencds.cqf.cql.evaluator.builder.library.TypedLibraryContentProviderFactory;
import org.opencds.cqf.cql.evaluator.builder.terminology.FhirRestTerminologyProviderFactory;
import org.opencds.cqf.cql.evaluator.builder.terminology.TerminologyProviderFactory;
import org.opencds.cqf.cql.evaluator.builder.terminology.TypedTerminologyProviderFactory;
import org.opencds.cqf.cql.evaluator.cql2elm.util.LibraryVersionSelector;
import org.opencds.cqf.cql.evaluator.engine.CqlEngineOptions;
import org.opencds.cqf.cql.evaluator.expression.ExpressionEvaluator;
import org.opencds.cqf.cql.evaluator.fhir.ClientFactory;
import org.opencds.cqf.cql.evaluator.fhir.adapter.r4.AdapterFactory;
import org.opencds.cqf.cql.evaluator.fhir.dal.RestFhirDal;
import org.opencds.cqf.cql.evaluator.library.CqlFhirParametersConverter;
import org.opencds.cqf.cql.evaluator.library.LibraryProcessor;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.client.impl.GenericClient;
import ca.uhn.fhir.rest.client.interceptor.SimpleRequestHeaderInterceptor;
import io.elimu.a2d2.exception.WorkItemHandlerException;

public class PlanDefCdsInlineWorkItemHandler implements WorkItemHandler {

	private static Map<String, DecoratedPlanDefinitionProcessor> CACHED_PROCESSORS = new HashMap<>();
	private static Map<String, PlanDefinition> CACHED_PLANDEFS = new HashMap<>();
	private static HashMap<VersionedIdentifier, Library> LIBRARY_CACHE = new HashMap<>();
	
	private FhirContext ctx;
	private ClientFactory clientFactory;
	private AdapterFactory adapterFactory;
	private FhirTypeConverter fhirTypeConverter;
	private CqlFhirParametersConverter cqlFhirParametersConverter;
	private LibraryVersionSelector libraryVersionSelector;
	private FhirRestLibraryContentProviderFactory frlcpFactory;
	private Set<TypedLibraryContentProviderFactory> libraryContentProviderFactories;
	private LibraryContentProviderFactory libraryLoaderFactory;
	private FhirModelResolverFactory mrFactory;
	private Set<ModelResolverFactory> modelResolverFactories;
	private FhirRestRetrieveProviderFactory trpFactory;
	private Set<TypedRetrieveProviderFactory> retrieveProviderFactories;
	private DataProviderFactory dataProviderFactory;
	private Set<TypedTerminologyProviderFactory> terminologyProviderFactories;
	private TerminologyProviderFactory terminologyProviderFactory;
	private EndpointConverter endpointConverter;
	private LibraryProcessor libProcessor;
	private MyOperationsParametersParser operationParametersParser;
	private ExpressionEvaluator expressionEvaluator;

	public PlanDefCdsInlineWorkItemHandler() {
		this.ctx = FhirContext.forR4Cached();
		this.clientFactory = new ClientFactory(this.ctx);
		this.adapterFactory = new AdapterFactory();
		this.fhirTypeConverter = new FhirTypeConverterFactory().create(this.ctx.getVersion().getVersion());
		this.cqlFhirParametersConverter = new CqlFhirParametersConverter(this.ctx, this.adapterFactory, this.fhirTypeConverter);
		this.libraryVersionSelector = new LibraryVersionSelector(this.adapterFactory);
		this.frlcpFactory = new FhirRestLibraryContentProviderFactory(this.clientFactory, this.adapterFactory, this.libraryVersionSelector);
		this.libraryContentProviderFactories = new HashSet<>();
		this.libraryContentProviderFactories.add(this.frlcpFactory);
		this.libraryLoaderFactory = new LibraryContentProviderFactory(this.ctx, this.adapterFactory, this.libraryContentProviderFactories, this.libraryVersionSelector);
		this.mrFactory = new FhirModelResolverFactory();
		this.modelResolverFactories = Collections.singleton(this.mrFactory);
		this.trpFactory = new FhirRestRetrieveProviderFactory(this.ctx, this.clientFactory);
		this.retrieveProviderFactories = new HashSet<>();
		this.retrieveProviderFactories.add(this.trpFactory);
		CqlOptions cqlOptions = new CqlOptions();
		CqlEngineOptions cqlEngineOptions = new CqlEngineOptions();
		cqlEngineOptions.setOptions(Collections.singleton(CqlEngine.Options.EnableExpressionCaching));
		cqlOptions.setCqlEngineOptions(cqlEngineOptions);
		this.dataProviderFactory = new DataProviderFactory(this.ctx, this.modelResolverFactories, this.retrieveProviderFactories);
		this.terminologyProviderFactories = Collections.singleton(new FhirRestTerminologyProviderFactory(this.ctx, this.clientFactory));
		this.terminologyProviderFactory = new TerminologyProviderFactory(this.ctx, this.terminologyProviderFactories);
		this.endpointConverter = new EndpointConverter(this.adapterFactory);
		this.libProcessor = new LibraryProcessor(this.ctx, this.cqlFhirParametersConverter, 
				this.libraryLoaderFactory, this.dataProviderFactory, this.terminologyProviderFactory, 
				this.endpointConverter, this.mrFactory, () -> new CqlEvaluatorBuilder().withLibraryCache(LIBRARY_CACHE).withCqlOptions(cqlOptions));
		this.expressionEvaluator = new ExpressionEvaluator(this.ctx, this.cqlFhirParametersConverter, this.libraryLoaderFactory, this.dataProviderFactory, 
				this.terminologyProviderFactory, this.endpointConverter, this.mrFactory, () -> new CqlEvaluatorBuilder().withLibraryCache(LIBRARY_CACHE).withCqlOptions(cqlOptions));
		this.operationParametersParser = new MyOperationsParametersParser(this.adapterFactory, this.fhirTypeConverter);
	}
	
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		Map<String, Object> results = workItem.getResults();
		String fhirServerUrl = (String) workItem.getParameter("fhirServerUrl");
		String fhirTerminologyServerUrl = (String) workItem.getParameter("fhirTerminologyServerUrl");
		String planDefId = (String) workItem.getParameter("planDefinitionId");
		String planDefUrl = (String) workItem.getParameter("planDefinitionUrl");
		String patientId = (String) workItem.getParameter("patientId");
		String encounterId = (String) workItem.getParameter("encounterId");
		String practitionerId = (String) workItem.getParameter("practitionerId");
		String organizationId = (String) workItem.getParameter("");
		String userType = (String) workItem.getParameter("userType");
		String userLanguage = (String) workItem.getParameter("userLanguage");
		String userTaskContext = (String) workItem.getParameter("userTaskContext");
		String setting = (String) workItem.getParameter("setting");
		String settingContext = (String) workItem.getParameter("settingContext");
		List<String> missingItems = new ArrayList<>();
		//validate required fhirServerUrl
		if (isEmpty(fhirServerUrl)) {
			missingItems.add("fhirServerUrl");
		}
		//validate fhirTerminolgoyServerUrl or make it fhirServerUrl by default
		if (isEmpty(fhirTerminologyServerUrl)) {
			if (!isEmpty(fhirServerUrl)) {
				missingItems.add("fhirTerminologyServerUrl");
			} else {
				fhirTerminologyServerUrl = fhirServerUrl;
			}
		}
		//validate required patientId
		if (isEmpty(patientId)) {
			missingItems.add("patientId");
		}
		//validate required planDefinitionId or planDefinitionUrl
		if (isEmpty(planDefId) && isEmpty(planDefUrl)) {
			missingItems.add("planDefinitionId|planDefinitionUrl");
		}
		if (!missingItems.isEmpty()) {
			throw new WorkItemHandlerException("Missing items: " + missingItems);
		}
		String fhirServerAuth = (String) workItem.getParameter("fhirServerAuth");
		String fhirTerminologyServerAuth = (String) workItem.getParameter("fhirTerminologyServerAuth");
		Map<String, Object> prefetchData = new HashMap<>();
		for (String key : workItem.getParameters().keySet()) {
			if (key.startsWith("prefetch_")) {
				prefetchData.put(key.replace("prefetch_", ""), workItem.getParameter(key));
			}
		}
		DecoratedPlanDefinitionProcessor pdProcessor = null;
		String key = null;
		if (CACHED_PROCESSORS.containsKey(planDefId)) {
			key = planDefId;
			pdProcessor = CACHED_PROCESSORS.get(planDefId);
		} else if (CACHED_PROCESSORS.containsKey(planDefUrl)) {
			key = planDefUrl;
			pdProcessor = CACHED_PROCESSORS.get(planDefUrl);
		} else {
			GenericClient fhirClient = (GenericClient) FhirContext.forR4().newRestfulGenericClient(fhirServerUrl);
			fhirClient.setDontValidateConformance(true);
			fhirClient.setEncoding(EncodingEnum.JSON);
			GenericClient fhirTerminologyClient = (GenericClient) FhirContext.forR4().newRestfulGenericClient(fhirTerminologyServerUrl);
			fhirTerminologyClient.setDontValidateConformance(true);
			fhirTerminologyClient.setEncoding(EncodingEnum.JSON);
			if (fhirServerAuth != null && !"".equals(fhirServerAuth.trim())) {
				fhirClient.registerInterceptor(new SimpleRequestHeaderInterceptor("Authorization", fhirServerAuth));
			}
			if (fhirTerminologyServerAuth != null && !"".equals(fhirTerminologyServerAuth.trim())) {
				fhirTerminologyClient.registerInterceptor(new SimpleRequestHeaderInterceptor("Authorization", fhirTerminologyServerAuth));
			}
			PlanDefinition planDefinition = null;
			if (planDefId != null) {
				key = planDefId;
				planDefinition = fhirTerminologyClient.fetchResourceFromUrl(PlanDefinition.class, fhirServerUrl + "/PlanDefinition/" + planDefId);
				if (planDefinition == null) {
					throw new WorkItemHandlerException("PlanDefinition cannot be found by ID " + planDefId);
				}
				CACHED_PLANDEFS.put(key, planDefinition);
			} else {
				key = planDefUrl;
				Bundle bundle = fhirTerminologyClient.fetchResourceFromUrl(Bundle.class, fhirServerUrl + "/PlanDefinition?url=" + planDefUrl);
				if (!bundle.getEntryFirstRep().hasResource()) {
					throw new WorkItemHandlerException("PlanDefinition cannot be found by Url " + planDefUrl);
				}
				planDefinition = (PlanDefinition) bundle.getEntryFirstRep().getResource();
				CACHED_PLANDEFS.put(key, planDefinition);
			}
			RestFhirDal fhirDal = (RestFhirDal) new FhirRestFhirDalFactory(this.clientFactory).create(
				              fhirServerUrl, Collections.singletonList("Content-Type: application/json"));
			ActivityDefinitionProcessor activityDefinitionProcessor = new ActivityDefinitionProcessor(this.ctx, fhirDal, this.libProcessor);
			pdProcessor = new DecoratedPlanDefinitionProcessor(this.ctx, fhirDal, 
					this.libProcessor, this.expressionEvaluator, activityDefinitionProcessor, this.operationParametersParser);
			CACHED_PROCESSORS.put(key, pdProcessor);
		}
		Endpoint terminologyEndpoint = new Endpoint().setAddress(fhirTerminologyServerUrl)
		        .setConnectionType(new Coding().setCode(Constants.HL7_FHIR_REST));
		Endpoint dataEndpoint = new Endpoint().setAddress(fhirServerUrl)
		        .setConnectionType(new Coding().setCode(Constants.HL7_FHIR_REST));
		try {
			CarePlan carePlan = pdProcessor.apply(CACHED_PLANDEFS.get(key).getIdElement(), 
				"Patient/"+patientId, encounterId == null ? null : "Encounter/" + encounterId, 
				practitionerId == null ? null : "Practitioner/" + practitionerId,
				organizationId == null ? null : "Organization/" + organizationId, 
				userType, userLanguage, userTaskContext, setting, settingContext, Boolean.TRUE, new Parameters(), 
				Boolean.TRUE, null, asParameters(prefetchData), dataEndpoint, dataEndpoint, terminologyEndpoint);
			List<CdsCard> cards = convert(carePlan);
			results.put("cards", cards);
			results.put("cardsJson", asJson(cards));
		} catch (CqlException e) {
			results.put("error", e.getMessage());
		} finally {
			manager.completeWorkItem(workItem.getId(), results);
		}
	}

	private boolean isEmpty(String value) {
		return value == null || "".equals(value.trim()) || "null".equalsIgnoreCase(value.trim());
	}

	private String asJson(List<CdsCard> cards) {
		IParser parser = FhirContext.forR4().newJsonParser();
		List<String> retval = new ArrayList<>();
		if (cards != null) {
			for (CdsCard card : cards) {
				JsonObject obj = new JsonObject();
				if (card.getDetail() != null) {
					obj.add("detail", new JsonPrimitive(card.getDetail()));
				}
				if (card.getIndicator() != null) {
					obj.add("indicator", new JsonPrimitive(card.getIndicator().code));
				}
				if (card.getLinks() != null) {
					JsonArray arr = new JsonArray();
					for (Links link : card.getLinks()) {
						JsonObject linkJson = new JsonObject();
						if (link.hasAppContext()) {
							linkJson.add("appContext", new JsonPrimitive(link.getAppContext()));
						}
						if (link.hasLabel()) {
							linkJson.add("label", new JsonPrimitive(link.getLabel()));
						}
						if (link.hasType()) {
							linkJson.add("type", new JsonPrimitive(link.getType()));
						}
						if (link.hasUrl()) {
							linkJson.add("url", new JsonPrimitive(link.getUrl().toExternalForm()));
						}
						arr.add(linkJson);
					}
					obj.add("links", arr);
				}
				if (card.getSelectionBehavior() != null) {
					obj.add("selectionBehavior", new JsonPrimitive(card.getSelectionBehavior()));
				}
				if (card.getSource() != null) {
					JsonObject source = new JsonObject();
					if (card.getSource().getIcon() != null) {
						source.add("icon", new JsonPrimitive(card.getSource().getIcon().toExternalForm()));
					}
					if (card.getSource().getLabel() != null) {
						source.add("label", new JsonPrimitive(card.getSource().getLabel()));
					}
					if (card.getSource().getUrl() != null) {
						source.add("url", new JsonPrimitive(card.getSource().getUrl().toExternalForm()));
					}
					obj.add("source", source);
				}
				if (card.getSuggestions() != null) {
					JsonArray arr = new JsonArray();
					for (Suggestions sug : card.getSuggestions()) {
						JsonObject sugJson = new JsonObject();
						if (sug.getActions() != null) {
							JsonArray actArray = new JsonArray();
							for (Action a : sug.getActions()) {
								JsonObject ajson = new JsonObject();
								if (a.getDescription() != null) {
									ajson.add("description", new JsonPrimitive(a.getDescription()));
								}
								if (a.getType() != null) {
									ajson.add("type", new JsonPrimitive(a.getType().name()));
								}
								if (a.getResource() != null) {
									String json = parser.encodeResourceToString(a.getResource());
									JsonObject jsonMap = new Gson().fromJson(json, JsonObject.class);
									ajson.add("resource", jsonMap);
								}
								actArray.add(ajson);
							}
							sugJson.add("actions", actArray);
						}
						if (sug.getLabel() != null) {
							sugJson.add("label", new JsonPrimitive(sug.getLabel()));
						}
						if (sug.getUuid() != null) {
							sugJson.add("uuid", new JsonPrimitive(sug.getUuid()));
						}
						arr.add(sugJson);
					}
					obj.add("suggestions", arr);
				}
				if (card.getSummary() != null) {
					obj.add("summary", new JsonPrimitive(card.getSummary()));
				}
				retval.add(new Gson().toJson(obj));
			}
		}
		return retval.toString();
	}

	private List<CdsCard> convert(CarePlan carePlan) {
        for (CarePlan.CarePlanActivityComponent activity : carePlan.getActivity()) {
        	if (activity.getReferenceTarget() == null && activity.getReference() != null && activity.getReference().getResource() != null && activity.getReference().getResource() instanceof RequestGroup) {
        		activity.setReferenceTarget((RequestGroup) activity.getReference().getResource());
            }
        }
		return R4CarePlanToCdsCard.convert(carePlan);
	}

	private Parameters asParameters(Map<String, Object> prefetchData) {
		Parameters retval = new Parameters();
		if (prefetchData != null) {
			for (Map.Entry<String, Object> entry : prefetchData.entrySet()) {
				if (entry.getValue() instanceof Resource) {
					retval.addParameter().setName(entry.getKey()).setResource((Resource) entry.getValue());
				}
			}
		}
		return retval;
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// do nothing
	}
}
