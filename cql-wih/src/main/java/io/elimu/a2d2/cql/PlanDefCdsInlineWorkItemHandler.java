package io.elimu.a2d2.cql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import io.elimu.a2d2.exception.WorkItemHandlerException;

public class PlanDefCdsInlineWorkItemHandler implements WorkItemHandler {

	private static Map<String, DecoratedPlanDefinitionProcessor> CACHED_PROCESSORS = new HashMap<>();
	private static Map<String, Object> CACHED_PLANDEFS = new HashMap<>();
	private static HashMap<?, ?> LIBRARY_CACHE = new HashMap<>();
	
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
			e.printStackTrace();
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
		try {
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
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			if (CACHED_PROCESSORS.containsKey(planDefId)) {
				key = planDefId;
				pdProcessor = CACHED_PROCESSORS.get(planDefId);
			} else if (CACHED_PROCESSORS.containsKey(planDefUrl)) {	
				key = planDefUrl;
				pdProcessor = CACHED_PROCESSORS.get(planDefUrl);
			} else {
				Object fhirClient = initClient(fhirServerUrl, fhirServerAuth); 
				Object fhirTerminologyClient = initClient(fhirTerminologyServerUrl, fhirTerminologyServerAuth);
				Object planDefinition = null;
				if (planDefId != null) {
					key = planDefId;
					planDefinition = fhirTerminologyClient.getClass().getMethod("fetchResourceFromUrl", Class.class, String.class).
							invoke(fhirTerminologyClient, cl.loadClass("org.hl7.fhir.r4.model.PlanDefinition"), fhirServerUrl + "/PlanDefinition/" + planDefId);
					if (planDefinition == null) {
						throw new WorkItemHandlerException("PlanDefinition cannot be found by ID " + planDefId);
					}
					CACHED_PLANDEFS.put(key, planDefinition);
				} else {
					key = planDefUrl;
					Object bundle = fhirTerminologyClient.getClass().getMethod("fetchResourceFromUrl", Class.class, String.class).
							invoke(cl.loadClass("org.hl7.fhir.r4.model.Bundle"), fhirServerUrl + "/PlanDefinition?url=" + planDefUrl);
					Object firstRep = bundle.getClass().getMethod("getEntryFirstRep").invoke(bundle);
					boolean hasRes = (boolean) firstRep.getClass().getMethod("hasResource").invoke(firstRep);
					if (hasRes) {
						throw new WorkItemHandlerException("PlanDefinition cannot be found by Url " + planDefUrl);
					}
					planDefinition = firstRep.getClass().getMethod("getResource").invoke(firstRep);
					CACHED_PLANDEFS.put(key, planDefinition);
				}
				Object fdFactory = cl.loadClass("org.opencds.cqf.cql.evaluator.builder.dal.FhirRestFhirDalFactory").getConstructor(this.clientFactory.getClass()).newInstance(this.clientFactory);
				Object fhirDal = fdFactory.getClass().getMethod("create", String.class, List.class).invoke(fdFactory, fhirServerUrl, Collections.singletonList("Content-Type: application/json"));
				Class<?> ctxClass = cl.loadClass("ca.uhn.fhir.context.FhirContext");
				Class<?> fhirDalClass = cl.loadClass("org.opencds.cqf.cql.evaluator.fhir.dal.FhirDal");
				Class<?> libProcessorClass = cl.loadClass("org.opencds.cqf.cql.evaluator.library.LibraryProcessor");
				Object activityDefinitionProcessor = cl.loadClass("org.opencds.cqf.cql.evaluator.activitydefinition.r4.ActivityDefinitionProcessor").
						getConstructor(ctxClass, fhirDalClass, libProcessorClass).newInstance(this.ctx, fhirDal, this.libProcessor);
				pdProcessor = new DecoratedPlanDefinitionProcessor(this.ctx, fhirDal,
						this.libProcessor, this.expressionEvaluator, activityDefinitionProcessor, this.operationParametersParser);
				CACHED_PROCESSORS.put(key, pdProcessor);
			}
			Class<?> endpointClass = cl.loadClass("org.hl7.fhir.r4.model.Endpoint");
			Object terminologyEndpoint = endpointClass.getConstructor().newInstance();
			endpointClass.getMethod("setAddress", String.class).invoke(terminologyEndpoint, fhirTerminologyServerUrl);
			Class<?> codingClass = cl.loadClass("org.hl7.fhir.r4.model.Coding");
			Object restCoding = codingClass.getConstructor().newInstance();
			codingClass.getMethod("setCode", String.class).invoke(restCoding, "hl7-fhir-rest");
			endpointClass.getMethod("setConnectionType", codingClass).invoke(terminologyEndpoint, restCoding);
			Object dataEndpoint = endpointClass.getConstructor().newInstance();
			endpointClass.getMethod("setAddress", String.class).invoke(dataEndpoint, fhirServerUrl);
			endpointClass.getMethod("setConnectionType", codingClass).invoke(dataEndpoint, restCoding);

			Object planIdType = CACHED_PLANDEFS.get(key).getClass().getMethod("getIdElement").invoke(CACHED_PLANDEFS.get(key));
			Object carePlan = pdProcessor.apply(planIdType, 
				"Patient/"+patientId, encounterId == null ? null : "Encounter/" + encounterId, 
				practitionerId == null ? null : "Practitioner/" + practitionerId,
				organizationId == null ? null : "Organization/" + organizationId, 
				userType, userLanguage, userTaskContext, setting, settingContext, Boolean.TRUE, 
				cl.loadClass("org.hl7.fhir.r4.model.Parameters").getConstructor().newInstance(), 
				Boolean.TRUE, null, asParameters(prefetchData), dataEndpoint, dataEndpoint, terminologyEndpoint);
			List<Object> cards = convert(carePlan);
			results.put("cards", cards);
			results.put("cardsJson", asJson(cards));
		} catch (RuntimeException e) {
			results.put("error", e.getMessage());
		} catch (Exception e) {
			results.put("error", e.getMessage());
		} finally {
			manager.completeWorkItem(workItem.getId(), results);
		}
	}

	private Object initClient(String fhirServerUrl, String auth) throws ReflectiveOperationException {
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
		return fhirClient;
	}

	private boolean isEmpty(String value) {
		return value == null || "".equals(value.trim()) || "null".equalsIgnoreCase(value.trim());
	}

	private String asJson(List<Object> cards) throws ReflectiveOperationException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Object parser = ctx.getClass().getMethod("newJsonParser").invoke(ctx);
		List<String> retval = new ArrayList<>();
		Class<?> cdsCardClass = cl.loadClass("org.opencds.cqf.cds.response.CdsCard");
		if (cards != null) {
			for (Object card : cards) {
				JsonObject obj = new JsonObject();
				Object detail = cdsCardClass.getMethod("getDetail").invoke(card);
				if (detail != null) {
					obj.add("detail", new JsonPrimitive(String.valueOf(detail)));
				}
				Object indicator = cdsCardClass.getMethod("getIndicator").invoke(card);
				if (indicator != null) {
					Object code = indicator.getClass().getField("code").get(indicator);
					obj.add("indicator", new JsonPrimitive(String.valueOf(code)));
				}
				List<?> links = (List<?>) cdsCardClass.getMethod("getLinks").invoke(card);
				if (links != null) {
					JsonArray arr = new JsonArray();
					for (Object link : links) {
						Object appContext = link.getClass().getMethod("getAppContext").invoke(link);
						boolean hasAppContext = (boolean) link.getClass().getMethod("hasAppContext").invoke(link);
						JsonObject linkJson = new JsonObject();
						if (hasAppContext) {
							linkJson.add("appContext", new JsonPrimitive(String.valueOf(appContext)));
						}
						Object label = link.getClass().getMethod("getLabel").invoke(link);
						boolean hasLabel = (boolean) link.getClass().getMethod("hasLabel").invoke(link);
						if (hasLabel) {
							linkJson.add("label", new JsonPrimitive(String.valueOf(label)));
						}
						Object type = link.getClass().getMethod("getType").invoke(link);
						boolean hasType = (boolean) link.getClass().getMethod("hasType").invoke(link);
						if (hasType) {
							linkJson.add("type", new JsonPrimitive(String.valueOf(type)));
						}
						Object url = link.getClass().getMethod("getUrl").invoke(link);
						boolean hasUrl = (boolean) link.getClass().getMethod("hasUrl").invoke(link);
						if (hasUrl) {
							String urlEF = (String) url.getClass().getMethod("toExternalForm").invoke(url);
							linkJson.add("url", new JsonPrimitive(urlEF));
						}
						arr.add(linkJson);
					}
					obj.add("links", arr);
				}
				Object selectionBehavior = cdsCardClass.getMethod("getSelectionBehavior").invoke(card);
				if (selectionBehavior != null) {
					obj.add("selectionBehavior", new JsonPrimitive(String.valueOf(selectionBehavior)));
				}
				Object sourceOrig = cdsCardClass.getMethod("getSource").invoke(card);
				if (sourceOrig != null) {
					JsonObject source = new JsonObject();
					Object icon = sourceOrig.getClass().getMethod("getIcon").invoke(sourceOrig);
					if (icon != null) {
						String iconEF = (String) icon.getClass().getMethod("toExternalForm").invoke(icon);
						source.add("icon", new JsonPrimitive(iconEF));
					}
					Object label = sourceOrig.getClass().getMethod("getLabel").invoke(sourceOrig);
					if (label != null) {
						source.add("label", new JsonPrimitive(String.valueOf(label)));
					}
					Object url = sourceOrig.getClass().getMethod("getUrl").invoke(sourceOrig);
					if (url != null) {
						String urlEF = (String) url.getClass().getMethod("toExternalForm").invoke(url);
						source.add("url", new JsonPrimitive(urlEF));
					}
					obj.add("source", source);
				}
				List<?> suggestions = (List<?>) cdsCardClass.getMethod("getSuggestions").invoke(card); 
				if (suggestions != null) {
					JsonArray arr = new JsonArray();
					for (Object sug : suggestions) {
						JsonObject sugJson = new JsonObject();
						List<?> actions = (List<?>) sug.getClass().getMethod("getActions").invoke(sug);
						if (actions != null) {
							JsonArray actArray = new JsonArray();
							for (Object a : actions) {
								JsonObject ajson = new JsonObject();
								Object description = a.getClass().getMethod("getDescription").invoke(a);
								if (description != null) {
									ajson.add("description", new JsonPrimitive(String.valueOf(description)));
								}
								Object type = a.getClass().getMethod("getType").invoke(a);
								if (type != null) {
									String typename = (String) type.getClass().getMethod("name").invoke(type);
									ajson.add("type", new JsonPrimitive(String.valueOf(typename)));
								}
								Object resource = a.getClass().getMethod("getResource").invoke(a);
								if (resource != null) {
									String json = (String) parser.getClass().getMethod("encodeResourceToString", 
											cl.loadClass("org.hl7.fhir.instance.model.api.IBaseResource")).invoke(parser, resource);
									JsonObject jsonMap = new Gson().fromJson(json, JsonObject.class);
									ajson.add("resource", jsonMap);
								}
								actArray.add(ajson);
							}
							sugJson.add("actions", actArray);
						}
						Object label = sug.getClass().getMethod("getLabel").invoke(sug);
						if (label != null) {
							sugJson.add("label", new JsonPrimitive(String.valueOf(label)));
						}
						Object uuid = sug.getClass().getMethod("getUuid").invoke(sug);
						if (uuid != null) {
							sugJson.add("uuid", new JsonPrimitive(String.valueOf(uuid)));
						}
						arr.add(sugJson);
					}
					obj.add("suggestions", arr);
				}
				Object summary = cdsCardClass.getMethod("getSummary").invoke(card);
				if (summary != null) {
					obj.add("summary", new JsonPrimitive(String.valueOf(summary)));
				}
				retval.add(new Gson().toJson(obj));
			}
		}
		return retval.toString();
	}

	private List<Object> convert(Object carePlan) throws ReflectiveOperationException {
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
        return (List<Object>) cl.loadClass("org.opencds.cqf.cds.response.R4CarePlanToCdsCard").getMethod("convert", carePlan.getClass()).invoke(null, carePlan);
	}

	private Object asParameters(Map<String, Object> prefetchData) throws ReflectiveOperationException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Object retval = cl.loadClass("org.hl7.fhir.r4.model.Parameters").getConstructor().newInstance();
		Class<?> resClass = cl.loadClass("org.hl7.fhir.r4.model.Resource");
		if (prefetchData != null) {
			for (Map.Entry<String, Object> entry : prefetchData.entrySet()) {
				if (resClass.isInstance(entry.getValue())) {
					Object param = retval.getClass().getMethod("addParameter").invoke(retval);
					param.getClass().getMethod("setName", String.class).invoke(param, entry.getKey());
					param.getClass().getMethod("setResource", resClass).invoke(param, entry.getValue());
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
