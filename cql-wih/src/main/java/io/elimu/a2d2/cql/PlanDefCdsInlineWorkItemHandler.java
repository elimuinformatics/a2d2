package io.elimu.a2d2.cql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import org.json.simple.parser.ParseException;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.elimu.a2d2.cdsresponse.entity.Card;
import io.elimu.a2d2.exception.WorkItemHandlerException;

public class PlanDefCdsInlineWorkItemHandler implements WorkItemHandler {
	
	private static final Logger LOG = LoggerFactory.getLogger(PlanDefCdsInlineWorkItemHandler.class);

	private static final long MAX_AGE_CACHE = Long.valueOf(System.getProperty("plandef.max.lifetime", "900000"));
	
	private static Map<String, TimeObject<DecoratedPlanDefinitionProcessor>> CACHED_PROCESSORS = new HashMap<>();
	private static Map<String, TimeObject<Object>> CACHED_PLANDEFS = new HashMap<>();
	private static Map<?, ?> LIBRARY_CACHE = new TimeoutMap<>(MAX_AGE_CACHE);
	
	private Object ctx;
	private Object clientFactory;
	private Object adapterFactory;
	private Object fhirTypeConverter;
	private Object cqlFhirParametersConverter;
	private Object libraryVersionSelector;
	private Object frlcpFactory;
	private Set<Object> libraryContentProviderFactories;
	private Object libraryLoaderFactory;
	private Object mrFactory;
	private Set<Object> modelResolverFactories;
	private Object trpFactory;
	private Set<Object> retrieveProviderFactories;
	private Object dataProviderFactory;
	private Set<Object> terminologyProviderFactories;
	private Object terminologyProviderFactory;
	private Object endpointConverter;
	private Object libProcessor;
	private Object operationParametersParser;
	private Object expressionEvaluator;

	public PlanDefCdsInlineWorkItemHandler() {
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Class<?> ctxClass = cl.loadClass("ca.uhn.fhir.context.FhirContext");
			Class<?> fhirTypeConverterClass = cl.loadClass("org.opencds.cqf.cql.engine.fhir.converter.FhirTypeConverter");
			Class<?> fhirVersionEnumClass = cl.loadClass("ca.uhn.fhir.context.FhirVersionEnum");
			Class<?> adapterFactoryClass = cl.loadClass("org.opencds.cqf.cql.evaluator.fhir.adapter.r4.AdapterFactory");
			Class<?> mrFactoryBaseClass = cl.loadClass("org.opencds.cqf.cql.evaluator.builder.ModelResolverFactory");
			Class<?> genericAdapterFactoryClass = cl.loadClass("org.opencds.cqf.cql.evaluator.fhir.adapter.AdapterFactory");
			Class<?> libraryContentProviderFactoryClass = cl.loadClass("org.opencds.cqf.cql.evaluator.builder.LibraryContentProviderFactory");
			Class<?> dataProviderFactoryClass = cl.loadClass("org.opencds.cqf.cql.evaluator.builder.DataProviderFactory");
			Class<?> terminologyProviderFactoryClass = cl.loadClass("org.opencds.cqf.cql.evaluator.builder.TerminologyProviderFactory");
			this.ctx = ctxClass.getMethod("forR4Cached").invoke(null);
			
			Object restClientFactory = ctxClass.getMethod("getRestfulClientFactory").invoke(this.ctx);
			Class<?> rcfClass = cl.loadClass("ca.uhn.fhir.rest.client.api.IRestfulClientFactory");
			rcfClass.getMethod("setHttpClient", Object.class).invoke(restClientFactory, new CachingHttpClient());
			rcfClass.getMethod("setConnectTimeout", int.class).invoke(restClientFactory, 30000);
			rcfClass.getMethod("setSocketTimeout", int.class).invoke(restClientFactory, 30000);
			Class<?> svmeClass = cl.loadClass("ca.uhn.fhir.rest.client.api.ServerValidationModeEnum");
			Object never = svmeClass.getField("NEVER").get(null);
			rcfClass.getMethod("setServerValidationMode", svmeClass).invoke(restClientFactory, never);
			
			Object ctxVersion = ctxClass.getMethod("getVersion").invoke(ctx);
			Object ctxVersionVersion = ctxVersion.getClass().getMethod("getVersion").invoke(ctxVersion);
			this.clientFactory = cl.loadClass("org.opencds.cqf.cql.evaluator.fhir.ClientFactory").getConstructor(ctxClass).newInstance(this.ctx);
			this.adapterFactory = adapterFactoryClass.getConstructor().newInstance();
			Object fhirTypeConverterFactory = cl.loadClass("org.opencds.cqf.cql.engine.fhir.converter.FhirTypeConverterFactory").getConstructor().newInstance();
			this.fhirTypeConverter = fhirTypeConverterFactory.getClass().getMethod("create", fhirVersionEnumClass).invoke(fhirTypeConverterFactory, ctxVersionVersion);
			this.cqlFhirParametersConverter = cl.loadClass("org.opencds.cqf.cql.evaluator.library.CqlFhirParametersConverter").
					getConstructor(ctxClass, genericAdapterFactoryClass, fhirTypeConverterClass).
					newInstance(this.ctx, this.adapterFactory, this.fhirTypeConverter);
			this.libraryVersionSelector = cl.loadClass("org.opencds.cqf.cql.evaluator.cql2elm.util.LibraryVersionSelector").getConstructor(genericAdapterFactoryClass).newInstance(this.adapterFactory);
			this.frlcpFactory = cl.loadClass("org.opencds.cqf.cql.evaluator.builder.library.FhirRestLibraryContentProviderFactory").
					getConstructor(clientFactory.getClass(), genericAdapterFactoryClass, libraryVersionSelector.getClass()).
					newInstance(this.clientFactory, this.adapterFactory, this.libraryVersionSelector);
			this.libraryContentProviderFactories = new HashSet<>();
			this.libraryContentProviderFactories.add(this.frlcpFactory);
			
			Class<?> libraryLoaderFactoryClass = cl.loadClass("org.opencds.cqf.cql.evaluator.builder.library.LibraryContentProviderFactory");
			this.libraryLoaderFactory = libraryLoaderFactoryClass.getConstructor(ctxClass, genericAdapterFactoryClass, Set.class, libraryVersionSelector.getClass()).
					newInstance(this.ctx, this.adapterFactory, this.libraryContentProviderFactories, this.libraryVersionSelector);
			this.mrFactory = cl.loadClass("org.opencds.cqf.cql.evaluator.builder.data.FhirModelResolverFactory").getConstructor().newInstance();
			this.modelResolverFactories = Collections.singleton(this.mrFactory);
			this.trpFactory = cl.loadClass("org.opencds.cqf.cql.evaluator.builder.data.FhirRestRetrieveProviderFactory").
				getConstructor(ctxClass, clientFactory.getClass()).
				newInstance(this.ctx, this.clientFactory);
			this.retrieveProviderFactories = new HashSet<>();
			this.retrieveProviderFactories.add(this.trpFactory);
			Object cqlOptions = cl.loadClass("org.opencds.cqf.cql.evaluator.CqlOptions").getConstructor().newInstance();
			Object cqlEngineOptions = cl.loadClass("org.opencds.cqf.cql.evaluator.engine.CqlEngineOptions").getConstructor().newInstance();
			Object opt = cl.loadClass("org.opencds.cqf.cql.engine.execution.CqlEngine$Options").getField("EnableExpressionCaching").get(null);
			cqlEngineOptions.getClass().getMethod("setOptions", Set.class).invoke(cqlEngineOptions, Collections.singleton(opt));
			cqlOptions.getClass().getMethod("setCqlEngineOptions", cqlEngineOptions.getClass()).invoke(cqlOptions, cqlEngineOptions);
			this.dataProviderFactory = cl.loadClass("org.opencds.cqf.cql.evaluator.builder.data.DataProviderFactory").
					getConstructor(ctxClass, Set.class, Set.class).
					newInstance(this.ctx, this.modelResolverFactories, this.retrieveProviderFactories);
			Object frtpFactory = cl.loadClass("org.opencds.cqf.cql.evaluator.builder.terminology.FhirRestTerminologyProviderFactory").
					getConstructor(ctxClass, clientFactory.getClass()).newInstance(this.ctx, this.clientFactory);
			this.terminologyProviderFactories = Collections.singleton(frtpFactory);
			this.terminologyProviderFactory = cl.loadClass("org.opencds.cqf.cql.evaluator.builder.terminology.TerminologyProviderFactory").
					getConstructor(ctxClass, Set.class).newInstance(this.ctx, this.terminologyProviderFactories);
			this.endpointConverter = cl.loadClass("org.opencds.cqf.cql.evaluator.builder.EndpointConverter").getConstructor(genericAdapterFactoryClass).newInstance(this.adapterFactory);
	
			Object supplier1 = new Supplier<Object>() {
				@Override
				public Object get() {
					try {
						Object builder = cl.loadClass("org.opencds.cqf.cql.evaluator.builder.CqlEvaluatorBuilder").getConstructor().newInstance();
						builder = builder.getClass().getMethod("withLibraryCache", HashMap.class).invoke(builder, LIBRARY_CACHE);
						builder = builder.getClass().getMethod("withCqlOptions", cqlOptions.getClass()).invoke(builder, cqlOptions);
						return builder;
					} catch (ReflectiveOperationException e) {
						return null;
					}
				}
			};
			Object supplier2 = new Supplier<Object>() {
				@Override
				public Object get() {
					try {
						Object builder = cl.loadClass("org.opencds.cqf.cql.evaluator.builder.CqlEvaluatorBuilder").getConstructor().newInstance();
						builder = builder.getClass().getDeclaredMethod("withLibraryCache", HashMap.class).invoke(builder, LIBRARY_CACHE);
						builder = builder.getClass().getDeclaredMethod("withCqlOptions", cqlOptions.getClass()).invoke(builder, cqlOptions);
						return builder;
					} catch (ReflectiveOperationException e) {
						return null;
					}
				}
			};
			this.libProcessor = cl.loadClass("org.opencds.cqf.cql.evaluator.library.LibraryProcessor").getConstructor(ctxClass, cqlFhirParametersConverter.getClass(),
							libraryContentProviderFactoryClass, dataProviderFactoryClass, terminologyProviderFactoryClass, endpointConverter.getClass(),
							mrFactoryBaseClass, Supplier.class).
					newInstance(this.ctx, this.cqlFhirParametersConverter, 
							this.libraryLoaderFactory, this.dataProviderFactory, this.terminologyProviderFactory, 
							this.endpointConverter, this.mrFactory, supplier1);
			this.expressionEvaluator = cl.loadClass("org.opencds.cqf.cql.evaluator.expression.ExpressionEvaluator").
				getConstructor(ctxClass, cqlFhirParametersConverter.getClass(), libraryContentProviderFactoryClass, 
					dataProviderFactoryClass, terminologyProviderFactoryClass, endpointConverter.getClass(), mrFactoryBaseClass, Supplier.class).
				newInstance(this.ctx, this.cqlFhirParametersConverter, this.libraryLoaderFactory, this.dataProviderFactory, 
					this.terminologyProviderFactory, this.endpointConverter, this.mrFactory, supplier2);
			Class<?> oppClass = cl.loadClass("org.opencds.cqf.cql.evaluator.plandefinition.r4.OperationParametersParser");
			Class<?> iparamsClass = cl.loadClass("org.hl7.fhir.instance.model.api.IBaseParameters");
			this.operationParametersParser = oppClass.getConstructor(genericAdapterFactoryClass, fhirTypeConverterClass).newInstance(this.adapterFactory, this.fhirTypeConverter);
			oppClass.getDeclaredMethod("getValueChild", iparamsClass, String.class).setAccessible(true);
			oppClass.getDeclaredMethod("getResourceChild", iparamsClass, String.class).setAccessible(true);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Cannot initialize " + getClass().getSimpleName(), e);
		}
	}
	
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		Map<String, Object> results = workItem.getResults();
		String fhirServerUrl = (String) workItem.getParameter("fhirServerUrl");
		String fhirTerminologyServerUrl = (String) workItem.getParameter("fhirTerminologyServerUrl");
		String planDefId = (String) workItem.getParameter("planDefinitionId");
		String planDefUrl = (String) workItem.getParameter("planDefinitionUrl");
		String planDefJson = (String) workItem.getParameter("planDefinitionJson");
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
		if (isEmpty(planDefId) && isEmpty(planDefUrl) && isEmpty(planDefJson)) {
			missingItems.add("planDefinitionId|planDefinitionUrl|planDefinitionJson");
		}
		if (!missingItems.isEmpty()) {
			throw new WorkItemHandlerException("Missing items: " + missingItems);
		}
		LOG.debug("Have all the necessary inputs for PlanDefCdsInlineWorkItmeHandler");
		try {
			String fhirServerAuth = (String) workItem.getParameter("fhirServerAuth");
			String fhirTerminologyServerAuth = (String) workItem.getParameter("fhirTerminologyServerAuth");
			Map<String, Object> prefetchData = new HashMap<>();
			Map<String, Object> contextData = new HashMap<>();
			Map<String, String> patientHeaderData = new HashMap<>();
			Map<String, String> terminologyHeaderData = new HashMap<>();
			for (String key : workItem.getParameters().keySet()) {
				if (key.startsWith("prefetch_")) {
					prefetchData.put(key.replace("prefetch_", ""), workItem.getParameter(key));
				} else if (key.startsWith("context_")) {
					contextData.put(key.replace("context_", ""), workItem.getParameter(key));
				} else if (key.startsWith("fhirServer_header_")) {
					patientHeaderData.put(key.replace("fhirServer_header_", "").replaceAll("__", "-"), 
							String.valueOf(workItem.getParameter(key)));
				} else if (key.startsWith("fhirTerminologyServer_header_")) {
					terminologyHeaderData.put(key.replace("fhirTerminologyServer_header_", "").replaceAll("__", "-"), 
							String.valueOf(workItem.getParameter(key)));
				}
			}
			DecoratedPlanDefinitionProcessor pdProcessor = null;
			Object planDefinition = null;
			String key = null;
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			if (CACHED_PROCESSORS.containsKey(planDefId) && !CACHED_PROCESSORS.get(planDefId).olderThan(MAX_AGE_CACHE)) {
				key = planDefId;
				pdProcessor = CACHED_PROCESSORS.get(planDefId).getValue();
				LOG.debug("Processor already cached by planDefId key " + key);
			} else if (CACHED_PROCESSORS.containsKey(planDefUrl) && !CACHED_PROCESSORS.get(planDefUrl).olderThan(MAX_AGE_CACHE)) {	
				key = planDefUrl;
				pdProcessor = CACHED_PROCESSORS.get(planDefUrl).getValue();
				LOG.debug("Processor already cached by planDefUrl key " + key);
			} else {
				if (!isEmpty(planDefJson)) {
					Object parser = this.ctx.getClass().getMethod("newJsonParser").invoke(this.ctx);
					Class<?> parserClass = cl.loadClass("ca.uhn.fhir.parser.IParser");
					Class<?> planDefClass = cl.loadClass("org.hl7.fhir.r4.model.PlanDefinition");
					planDefinition = parserClass.getMethod("parseResource",  Class.class, String.class).invoke(parser, planDefClass, planDefJson);
					Object idObj = planDefClass.getMethod("getIdElement").invoke(planDefinition);
					key = (String) idObj.getClass().getMethod("getIdPart").invoke(idObj);
					CACHED_PLANDEFS.put(key, new TimeObject<>(planDefinition));
				} else {
					LOG.debug("Creating PlanDefinitionProcessor...");
					//Object fhirClient = initClient(fhirServerUrl, fhirServerAuth, patientHeaderData); 
					Object fhirTerminologyClient = initClient(fhirTerminologyServerUrl, fhirTerminologyServerAuth, terminologyHeaderData);
					if (planDefId != null) {
						LOG.debug("Fetching PlanDefinition by ID: " + fhirTerminologyServerUrl + "/PlanDefinition/" + planDefId);
						key = planDefId;
						planDefinition = fhirTerminologyClient.getClass().getMethod("fetchResourceFromUrl", Class.class, String.class).
								invoke(fhirTerminologyClient, cl.loadClass("org.hl7.fhir.r4.model.PlanDefinition"), fhirTerminologyServerUrl + "/PlanDefinition/" + planDefId);
						if (planDefinition == null) {
							throw new WorkItemHandlerException("PlanDefinition cannot be found by ID " + planDefId);
						}
						CACHED_PLANDEFS.put(key, new TimeObject<>(planDefinition));
						String url = (String) planDefinition.getClass().getMethod("getUrl").invoke(planDefinition);
						if (url != null) {
							CACHED_PLANDEFS.put(url, new TimeObject<>(planDefinition));
						}
						LOG.debug("PlanDefinition fetch " + fhirTerminologyServerUrl + "/PlanDefinition/" + planDefId + " successful");
					} else {
						LOG.debug("Fetching PlanDefinition by URL: " + fhirTerminologyServerUrl + "/PlanDefinition?url=" + planDefUrl);
						key = planDefUrl;
						Object bundle = fhirTerminologyClient.getClass().getMethod("fetchResourceFromUrl", Class.class, String.class).
								invoke(fhirTerminologyClient, cl.loadClass("org.hl7.fhir.r4.model.Bundle"), fhirTerminologyServerUrl + "/PlanDefinition?url=" + planDefUrl);
						Object firstRep = bundle.getClass().getMethod("getEntryFirstRep").invoke(bundle);
						boolean hasRes = (boolean) firstRep.getClass().getMethod("hasResource").invoke(firstRep);
						if (!hasRes) {
							throw new WorkItemHandlerException("PlanDefinition cannot be found by Url " + planDefUrl);
						}
						planDefinition = firstRep.getClass().getMethod("getResource").invoke(firstRep);
						CACHED_PLANDEFS.put(key, new TimeObject<>(planDefinition));
						Object idObj = planDefinition.getClass().getMethod("getIdElement").invoke(planDefinition);
						String id = (String) idObj.getClass().getMethod("getIdPart").invoke(idObj);
						if (id != null) {
							CACHED_PLANDEFS.put(id, new TimeObject<>(planDefinition));
						}
						LOG.debug("PlanDefinition fetch " + fhirTerminologyServerUrl + "/PlanDefinition?url=" + planDefUrl + " successful");
					}
				}
				Object fdFactory = cl.loadClass("org.opencds.cqf.cql.evaluator.builder.dal.FhirRestFhirDalFactory").getConstructor(this.clientFactory.getClass()).newInstance(this.clientFactory);
				List<String> headers = new ArrayList<>();
				headers.add("Content-Type: application/json");
				if (fhirTerminologyServerAuth != null) {
					headers.add("Authorization:" + fhirTerminologyServerAuth);
				}
				if (terminologyHeaderData != null && !terminologyHeaderData.isEmpty()) {
					for (String headerName : terminologyHeaderData.keySet()) {
						headers.add(headerName + ": " + terminologyHeaderData.get(headerName));
					}
				}
				Object fhirDal = fdFactory.getClass().getMethod("create", String.class, List.class).invoke(fdFactory, fhirTerminologyServerUrl, headers);
				Class<?> ctxClass = cl.loadClass("ca.uhn.fhir.context.FhirContext");
				Class<?> fhirDalClass = cl.loadClass("org.opencds.cqf.cql.evaluator.fhir.dal.FhirDal");
				Class<?> libProcessorClass = cl.loadClass("org.opencds.cqf.cql.evaluator.library.LibraryProcessor");
				Object activityDefinitionProcessor = cl.loadClass("org.opencds.cqf.cql.evaluator.activitydefinition.r4.ActivityDefinitionProcessor").
						getConstructor(ctxClass, fhirDalClass, libProcessorClass).newInstance(this.ctx, fhirDal, this.libProcessor);
				pdProcessor = new DecoratedPlanDefinitionProcessor(this.ctx, fhirDal,
						this.libProcessor, this.expressionEvaluator, activityDefinitionProcessor, this.operationParametersParser);
				CACHED_PROCESSORS.put(key, new TimeObject<>(pdProcessor));
				LOG.debug("PlanDefinitionProcessor for key " + key + " created");
			}
			Class<?> endpointClass = cl.loadClass("org.hl7.fhir.r4.model.Endpoint");
			Object terminologyEndpoint = endpointClass.getConstructor().newInstance();
			endpointClass.getMethod("setAddress", String.class).invoke(terminologyEndpoint, fhirTerminologyServerUrl);
			Class<?> codingClass = cl.loadClass("org.hl7.fhir.r4.model.Coding");
			Object restCoding = codingClass.getConstructor().newInstance();
			codingClass.getMethod("setCode", String.class).invoke(restCoding, "hl7-fhir-rest");
			endpointClass.getMethod("setConnectionType", codingClass).invoke(terminologyEndpoint, restCoding);
			if (fhirTerminologyServerAuth != null) {
				endpointClass.getMethod("addHeader", String.class).invoke(terminologyEndpoint, "Authorization:" + fhirTerminologyServerAuth);
			}
			if (terminologyHeaderData != null && !terminologyHeaderData.isEmpty()) {
				for (String headerName : terminologyHeaderData.keySet()) {
					endpointClass.getMethod("addHeader", String.class).invoke(terminologyEndpoint, headerName + ": " + terminologyHeaderData.get(headerName));
				}
			}
			Object dataEndpoint = endpointClass.getConstructor().newInstance();
			endpointClass.getMethod("setAddress", String.class).invoke(dataEndpoint, fhirServerUrl);
			endpointClass.getMethod("setConnectionType", codingClass).invoke(dataEndpoint, restCoding);
			if (fhirServerAuth != null) {
				endpointClass.getMethod("addHeader", String.class).invoke(dataEndpoint, "Authorization:" + fhirServerAuth);
			}
			if (patientHeaderData != null && !patientHeaderData.isEmpty()) {
				for (String headerName : patientHeaderData.keySet()) {
					endpointClass.getMethod("addHeader", String.class).invoke(dataEndpoint, headerName + ": " + patientHeaderData.get(headerName));
				}
			}
			planDefinition = CACHED_PLANDEFS.get(key).getValue();
			Object planIdType = planDefinition.getClass().getMethod("getIdElement").invoke(planDefinition);
			LOG.debug("PlanDefinitionProcessor apply call starting...");
			Object carePlan = pdProcessor.apply(planDefinition, planIdType, 
				"Patient/"+patientId, encounterId == null ? null : "Encounter/" + encounterId, 
				practitionerId == null ? null : "Practitioner/" + practitionerId,
				organizationId == null ? null : "Organization/" + organizationId, 
				userType, userLanguage, userTaskContext, setting, settingContext, Boolean.TRUE, 
				asParameters(contextData), Boolean.TRUE, null, asParameters(prefetchData), dataEndpoint, 
				terminologyEndpoint, terminologyEndpoint);
			Map<String, Object> cqlResults = pdProcessor.getEvaluatedCqlResults();
			//populate CQL results
			if (cqlResults != null) {
				for (String cqlResultKey : cqlResults.keySet()) {
					results.put("cql_" + cqlResultKey, cqlResults.get(cqlResultKey));
				}
			}
			LOG.debug("PlanDefinitionProcessor apply call done");
			List<Card> cards = convert(carePlan);
			results.put("cards", cards);
			results.put("cardsJson", new Gson().toJson(cards));
		} catch (RuntimeException e) {
			e.printStackTrace();
			LOG.warn("PlanDefinitionProcessor execution threw a RuntimeException. Returning error output", e);
			results.put("error", e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			LOG.warn("PlanDefinitionProcessor execution threw an Exception. Returning error output", e);
			results.put("error", e.getMessage());
		} finally {
			manager.completeWorkItem(workItem.getId(), results);
		}
	}

	private Object initClient(String fhirServerUrl, String auth, Map<String, String> headerData) throws ReflectiveOperationException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Object fhirClient = ctx.getClass().getMethod("newRestfulGenericClient", String.class).invoke(ctx, fhirServerUrl);
		fhirClient.getClass().getMethod("setDontValidateConformance", boolean.class).invoke(fhirClient, true);
		Class<?> enumClass = cl.loadClass("ca.uhn.fhir.rest.api.EncodingEnum");
		Object jsonEnum = enumClass.getField("JSON").get(null);
		fhirClient.getClass().getMethod("setEncoding", enumClass).invoke(fhirClient, jsonEnum);
		if (auth != null && !"".equals(auth.trim())) {
			Object interceptor = cl.loadClass("ca.uhn.fhir.rest.client.interceptor.SimpleRequestHeaderInterceptor").
					getConstructor(String.class, String.class).newInstance("Authorization", auth);
			fhirClient.getClass().getMethod("registerInterceptor", Object.class).invoke(fhirClient, interceptor);
		} 
		if (headerData != null && !headerData.isEmpty()) {
			for (String key : headerData.keySet()) {
				String value = headerData.get(key);
				Object interceptor = cl.loadClass("ca.uhn.fhir.rest.client.interceptor.SimpleRequestHeaderInterceptor").
						getConstructor(String.class, String.class).newInstance(key, value);
				fhirClient.getClass().getMethod("registerInterceptor", Object.class).invoke(fhirClient, interceptor);
			}
		}
		return fhirClient;
	}

	private boolean isEmpty(String value) {
		return value == null || "".equals(value.trim()) || "null".equalsIgnoreCase(value.trim());
	}

	private List<Card> convert(Object carePlan) throws ParseException, ReflectiveOperationException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader(); 
		Class<?> reqGroupClass = cl.loadClass("org.hl7.fhir.r4.model.RequestGroup");
		Class<?> resClass = cl.loadClass("org.hl7.fhir.r4.model.Resource");
		List<?> activities = (List<?>) carePlan.getClass().getMethod("getActivity").invoke(carePlan);
		for (Object activity : activities) {
			Object refTarget = activity.getClass().getMethod("getReferenceTarget").invoke(activity);
			if (refTarget == null) {
				Object ref = activity.getClass().getMethod("getReference").invoke(activity);
				if (ref != null) {
					Object res = ref.getClass().getMethod("getResource").invoke(ref);
					if (res != null && reqGroupClass.isInstance(res)) {
						activity.getClass().getMethod("setReferenceTarget", resClass).invoke(activity, res);
					}
				}
			}
		}
		return CardCreator.convert(carePlan, cl);
	}

	private Object asParameters(Map<String, Object> prefetchData) throws ReflectiveOperationException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Object retval = cl.loadClass("org.hl7.fhir.r4.model.Parameters").getConstructor().newInstance();
		if (prefetchData != null) {
			for (Map.Entry<String, Object> entry : prefetchData.entrySet()) {
				if (entry.getValue() instanceof Collection) {
					//enter each element as a different ParameterComponent with same name
					Collection<?> col = (Collection<?>) entry.getValue();
					Class<?> extClass = cl.loadClass("org.hl7.fhir.r4.model.Extension");
					Object extension = extClass.getConstructor().newInstance();
					extClass.getMethod("setUrl", String.class).invoke(extension, "http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-parameterDefinition");
					Class<?> typeClass = cl.loadClass("org.hl7.fhir.r4.model.Type");
					Class<?> paramDefClass = cl.loadClass("org.hl7.fhir.r4.model.ParameterDefinition");
					Object paramDef = paramDefClass.getConstructor().newInstance();
					paramDefClass.getMethod("setMin", int.class).invoke(paramDef, 0);
					paramDefClass.getMethod("setMax", String.class).invoke(paramDef, String.valueOf(col.size() < 2 ? 2 : col.size()));
					paramDefClass.getMethod("setType", String.class).invoke(paramDef, "item");
					extClass.getMethod("setValue", typeClass).invoke(extension, paramDef);
					for (Object item : col) {
						Map<String, Object> hmap = new HashMap<>();
						hmap.put(entry.getKey(), item);
						Map.Entry<String, Object> mockEntry = hmap.entrySet().iterator().next();
						addSingleParam(cl, retval, mockEntry, extension);
					}
				} else {
					addSingleParam(cl, retval, entry, null);
				}
			}
		}
		return retval;
	}

	private void addSingleParam(ClassLoader cl, Object retval, Entry<String, Object> entry, Object extension) throws ReflectiveOperationException {
		Class<?> resClass = cl.loadClass("org.hl7.fhir.r4.model.Resource");
		Class<?> typeClass = cl.loadClass("org.hl7.fhir.r4.model.Type");
		Object param = null;
		if (resClass.isInstance(entry.getValue()) || entry.getValue() instanceof String || entry.getValue() instanceof Number || entry.getValue() instanceof Boolean || entry.getValue() instanceof Date) {
			param = retval.getClass().getMethod("addParameter").invoke(retval);
			param.getClass().getMethod("setName", String.class).invoke(param, entry.getKey());
			if (extension != null) {
				param.getClass().getMethod("addExtension", extension.getClass()).invoke(param, extension);
			}
		}
		if (resClass.isInstance(entry.getValue())) {
			param.getClass().getMethod("setResource", resClass).invoke(param, entry.getValue());
		} else if (entry.getValue() instanceof String) {
			Class<?> strTypeClass = cl.loadClass("org.hl7.fhir.r4.model.StringType");
			Object value = strTypeClass.getConstructor(String.class).newInstance(String.valueOf(entry.getValue()));
			param.getClass().getMethod("setValue", typeClass).invoke(param, value);
		} else if (entry.getValue() instanceof Number) {
			Number n = (Number) entry.getValue();
			if (n.toString().indexOf(".") == -1) {
				//integer
				Class<?> intTypeClass = cl.loadClass("org.hl7.fhir.r4.model.IntegerType");
				Object value = intTypeClass.getConstructor(int.class).newInstance(n.intValue());
				param.getClass().getMethod("setValue", typeClass).invoke(param, value);
			} else {
				//decimal
				Class<?> decTypeClass = cl.loadClass("org.hl7.fhir.r4.model.DecimalType");
				Object value = decTypeClass.getConstructor(double.class).newInstance(n.doubleValue());
				param.getClass().getMethod("setValue", typeClass).invoke(param, value);
			}
		} else if (entry.getValue() instanceof Boolean) {
			//boolean
			Class<?> boolTypeClass = cl.loadClass("org.hl7.fhir.r4.model.BooleanType");
			Object value = boolTypeClass.getConstructor(Boolean.class).newInstance(entry.getValue());
			param.getClass().getMethod("setValue", typeClass).invoke(param, value);
		} else if (entry.getValue() instanceof Date) {
			//datetime
			Class<?> dateTypeClass = cl.loadClass("org.hl7.fhir.r4.model.DateTimeType");
			Object value = dateTypeClass.getConstructor(Date.class).newInstance(entry.getValue());
			param.getClass().getMethod("setValue", typeClass).invoke(param, value);
		} else {
			//log parameter type not handled: type
			if (entry.getValue() == null ) {
				LOG.warn("Parameter type not supported for key '" + entry.getKey() + "': null");
			} else {
				LOG.warn("Parameter type not supported for key '" + entry.getKey() + "': " + entry.getValue().getClass().getName());
			}
		}

	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// do nothing
	}
}
