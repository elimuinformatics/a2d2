package io.elimu.a2d2.cql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.opencds.cqf.cds.response.R4CarePlanToCdsCard;
import org.opencds.cqf.cql.engine.fhir.converter.FhirTypeConverter;
import org.opencds.cqf.cql.engine.fhir.converter.FhirTypeConverterFactory;
import org.opencds.cqf.cql.evaluator.activitydefinition.r4.ActivityDefinitionProcessor;
import org.opencds.cqf.cql.evaluator.builder.Constants;
import org.opencds.cqf.cql.evaluator.builder.CqlEvaluatorBuilder;
import org.opencds.cqf.cql.evaluator.builder.EndpointConverter;
import org.opencds.cqf.cql.evaluator.builder.ModelResolverFactory;
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
import org.opencds.cqf.cql.evaluator.expression.ExpressionEvaluator;
import org.opencds.cqf.cql.evaluator.fhir.ClientFactory;
import org.opencds.cqf.cql.evaluator.fhir.adapter.r4.AdapterFactory;
import org.opencds.cqf.cql.evaluator.fhir.dal.RestFhirDal;
import org.opencds.cqf.cql.evaluator.library.CqlFhirParametersConverter;
import org.opencds.cqf.cql.evaluator.library.LibraryProcessor;

import com.google.gson.Gson;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.client.impl.GenericClient;
import ca.uhn.fhir.rest.client.interceptor.SimpleRequestHeaderInterceptor;

public class PlanDefCdsInlineWorkItemHandler implements WorkItemHandler {

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
		this.ctx = FhirContext.forR4();
		this.clientFactory = new ClientFactory(ctx);
		this.adapterFactory = new AdapterFactory();
		this.fhirTypeConverter = new FhirTypeConverterFactory().create(FhirVersionEnum.R4);
		this.cqlFhirParametersConverter = new CqlFhirParametersConverter(ctx, adapterFactory, fhirTypeConverter);
		libraryVersionSelector = new LibraryVersionSelector(adapterFactory);
		this.frlcpFactory = new FhirRestLibraryContentProviderFactory(clientFactory, adapterFactory, libraryVersionSelector);
		this.libraryContentProviderFactories = new HashSet<>();
		libraryContentProviderFactories.add(frlcpFactory);
		this.libraryLoaderFactory = new LibraryContentProviderFactory(ctx, adapterFactory, libraryContentProviderFactories, libraryVersionSelector);
		this.mrFactory = new FhirModelResolverFactory();
		this.modelResolverFactories = new HashSet<>();
		modelResolverFactories.add(mrFactory);
		this.trpFactory = new FhirRestRetrieveProviderFactory(ctx, clientFactory);
		this.retrieveProviderFactories = new HashSet<>();
		retrieveProviderFactories.add(trpFactory);
		this.dataProviderFactory = new DataProviderFactory(ctx, modelResolverFactories, retrieveProviderFactories);
		this.terminologyProviderFactories = new HashSet<>();
		terminologyProviderFactories.add(new FhirRestTerminologyProviderFactory(ctx, clientFactory));
		this.terminologyProviderFactory = new TerminologyProviderFactory(ctx, terminologyProviderFactories);
		this.endpointConverter = new EndpointConverter(adapterFactory);
		this.libProcessor = new LibraryProcessor(ctx, cqlFhirParametersConverter, 
				libraryLoaderFactory, dataProviderFactory, terminologyProviderFactory, 
				endpointConverter, mrFactory, () -> new CqlEvaluatorBuilder());
		this.operationParametersParser = new MyOperationsParametersParser(adapterFactory, fhirTypeConverter);
		this.expressionEvaluator = new ExpressionEvaluator(ctx, cqlFhirParametersConverter, libraryLoaderFactory, dataProviderFactory, 
				terminologyProviderFactory, endpointConverter, mrFactory, () -> new CqlEvaluatorBuilder());
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
		//TODO validate required fhirServerUrl
		//TODO validate fhirTerminolgoyServerUrl or make it fhirServerUrl by default
		//TODO validate required planDefinitionId or planDefinitionUrl
		//TODO validate required patientId
		String fhirServerAuth = (String) workItem.getParameter("fhirServerAuth");
		String fhirTerminologyServerAuth = (String) workItem.getParameter("fhirTerminologyServerAuth");
		Map<String, Object> prefetchData = new HashMap<>();
		for (String key : workItem.getParameters().keySet()) {
			if (key.startsWith("prefetch_")) {
				prefetchData.put(key.replace("prefetch_", ""), workItem.getParameter(key));
			}
		}
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
		/*org.hl7.fhir.r4.model.Library r4library = fhirClient.fetchResourceFromUrl(org.hl7.fhir.r4.model.Library.class, fhirServerUrl + "/Library/" + planDefinition.getIdElement().getIdPart());
		List<Library> libraries = new LinkedList<>();*/
		/*List<org.hl7.fhir.r4.model.Library> r4libraries = bundle2.getEntry().stream().map(e -> { return (org.hl7.fhir.r4.model.Library) e.getResource(); }).collect(Collectors.toList());
		Libary mainLibrary = 
		ModelManager modelManager = new ModelManager();
		LibraryManager libraryManager = new LibraryManager(modelManager);
		for (org.hl7.fhir.r4.model.Library library : r4libraries) {
			for (Attachment content : library.getContent()) {
				if ("text/cql".equals(content.getContentType())) {
					try {
						String cql = new String(content.getData());
						CqlTranslator translator = CqlTranslator.fromText(cql, modelManager, libraryManager);
						Library lib = JsonCqlLibraryReader.read(new StringReader(translator.toJxson()));
						libraries.add(lib);
					} catch (IOException e) {
						//TODO error
						e.printStackTrace();
					}
				}
			}
		}*/
		/*CacheAwareModelManager modelManager = new CacheAwareModelManager(new HashMap<>());
		LibraryManager libraryManager = new LibraryManager(modelManager);
		PriorityLibrarySourceLoader librarySourceLoader = new PriorityLibrarySourceLoader();
		librarySourceLoader.setNamespaceManager(new NamespaceManager());
		AdapterFactory adapterFactory = new AdapterFactory();
		FhirClientFhirLibraryContentProvider libraryContentProvider = new FhirClientFhirLibraryContentProvider(
				fhirClient, adapterFactory, new LibraryVersionSelector(adapterFactory));
		librarySourceLoader.registerProvider(libraryContentProvider);
		libraryManager.setLibrarySourceLoader(librarySourceLoader);
		
		for (Attachment content : r4library.getContent()) {
			if ("text/cql".equals(content.getContentType())) {
				try {
					String cql = new String(content.getData());
					CqlTranslator translator = CqlTranslator.fromText(cql, modelManager, libraryManager, Options.EnableLocators, Options.EnableResultTypes);
					Library lib = JsonCqlLibraryReader.read(new StringReader(translator.toJxson()));
					libraries.add(lib);
				} catch (IOException e) {
					//TODO error
					e.printStackTrace();
				}
			}
		}
		ModelResolver resolver = new R4FhirModelResolver();
		R4FhirTerminologyProvider terminologyProvider = new R4FhirTerminologyProvider(fhirClient);
		Context context = new Context(libraries.get(0));
		for (Map.Entry<String, Object> entry : prefetchData.entrySet()) {
			context.setContextValue(entry.getKey(), entry.getValue());
		}
		for (Map.Entry<String, String> entry : contextData.entrySet()) {
			context.setContextValue(entry.getKey(), entry.getValue());
		}
		context.registerLibraryLoader(new InMemoryLibraryLoader(libraries));
		R4HookEvaluator evaluator = new R4HookEvaluator(resolver);
		List<CdsCard> cards = evaluator.evaluateCdsHooksPlanDefinition(context, planDefinition, patientId, fhirClient);
		results.put("cards", cards);
		results.put("cardsJson", new Gson().toJson(cards));*/
		RestFhirDal fhirDal = new RestFhirDal(fhirClient);
		ActivityDefinitionProcessor activityDefinitionProcessor = new ActivityDefinitionProcessor(ctx, fhirDal, libProcessor);
		DecoratedPlanDefinitionProcessor pdProcessor = new DecoratedPlanDefinitionProcessor(FhirContext.forR4(), fhirDal, 
				libProcessor, expressionEvaluator, activityDefinitionProcessor, operationParametersParser);
		Endpoint terminologyEndpoint = new Endpoint().setAddress(fhirTerminologyClient.getServerBase())
		        .setConnectionType(new Coding().setCode(Constants.HL7_FHIR_REST));
		Endpoint dataEndpoint = new Endpoint().setAddress(fhirClient.getServerBase())
		        .setConnectionType(new Coding().setCode(Constants.HL7_FHIR_REST));
		CarePlan carePlan = pdProcessor.apply(planDefinition.getIdElement(), 
				"Patient/"+patientId, encounterId == null ? null : "Encounter/" + encounterId, 
				practitionerId == null ? null : "Practitioner/" + practitionerId,
				organizationId == null ? null : "Organization/" + organizationId, 
				userType, userLanguage, userTaskContext, setting, settingContext, Boolean.TRUE, new Parameters(), 
				Boolean.TRUE, null, asParameters(prefetchData), dataEndpoint, dataEndpoint, terminologyEndpoint);
		List<CdsCard> cards = convert(carePlan);
		results.put("cards", cards);
		results.put("cardsJson", new Gson().toJson(cards));
		manager.completeWorkItem(workItem.getId(), results);
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
