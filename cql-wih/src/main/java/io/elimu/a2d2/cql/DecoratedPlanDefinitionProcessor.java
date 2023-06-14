package io.elimu.a2d2.cql;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecoratedPlanDefinitionProcessor {
	protected Object activityDefinitionProcessor;
	protected Object libraryProcessor;
	protected Object expressionEvaluator;
	protected Object operationParametersParser;
	protected Object fhirContext;
	protected Object fhirDal;
	protected Object fhirPath;

	private Map<String, Object> libraryResults = new HashMap<>();
  
	private static final Logger logger = LoggerFactory.getLogger(DecoratedPlanDefinitionProcessor.class);
	private static final String alternateExpressionExtension = "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-alternativeExpression";

	private ThreadLocal<Map<String, Object>> evaluatedCqlResults = new ThreadLocal<>() {
		protected Map<String, Object> initialValue() {
			return new HashMap<>();
		}
	};
	
	public DecoratedPlanDefinitionProcessor(Object fhirContext, Object fhirDal, Object libraryProcessor, Object expressionEvaluator,
			Object activityDefinitionProcessor, Object operationParametersParser) throws ReflectiveOperationException {
		requireNonNull(fhirContext, "fhirContext can not be null");
		requireNonNull(fhirDal, "fhirDal can not be null");
		requireNonNull(libraryProcessor, "LibraryProcessor can not be null");
		requireNonNull(operationParametersParser, "OperationParametersParser can not be null");
		this.fhirContext = fhirContext;
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		this.fhirPath = cl.loadClass("org.opencds.cqf.cql.evaluator.fhir.util.FhirPathCache").getMethod("cachedForContext", fhirContext.getClass()).invoke(null, fhirContext);
		this.fhirDal = fhirDal;
		this.libraryProcessor = libraryProcessor;
		this.expressionEvaluator = expressionEvaluator;
		this.activityDefinitionProcessor = activityDefinitionProcessor;
		this.operationParametersParser = operationParametersParser;
	}

	public static Optional<Object> castOrThrow(Object obj, Class<?> type, String errorMessage) {
		if (obj == null) return Optional.empty();
		if (type.isInstance(obj)) {
			return Optional.of(type.cast(obj));
		}
		throw new IllegalArgumentException(errorMessage);
	}
	
	public Object apply(Object paramPlanDefinition, Object theId, String patientId, String encounterId, String practitionerId,
			String organizationId, String userType, String userLanguage, String userTaskContext, String setting,
			String settingContext, Boolean mergeNestedCarePlans, Object parameters, Boolean useServerData,
			Object bundle, Object prefetchData, Object dataEndpoint, Object contentEndpoint,
			Object terminologyEndpoint) throws ReflectiveOperationException {

		// warn if prefetchData exists
		// if no data anywhere blow up
		evaluatedCqlResults.get().clear();
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		//Class<?> idTypeClass = cl.loadClass("org.hl7.fhir.instance.model.api.IIdType");
		Class<?> planDefClass = cl.loadClass("org.hl7.fhir.r4.model.PlanDefinition");
		Object basePlanDefinition = paramPlanDefinition;
		requireNonNull(basePlanDefinition, "Couldn't find PlanDefinition " + theId);
		Object planDefinition = castOrThrow(basePlanDefinition, planDefClass,
				"The planDefinition passed to FhirDal was not a valid instance of PlanDefinition.class").get();
		logger.info("Performing $apply operation on PlanDefinition/" + theId);
		Class<?> cpClass = cl.loadClass("org.hl7.fhir.r4.model.CarePlan");
		Object builder = cpClass.getConstructor().newInstance();
		Object planDefId = planDefinition.getClass().getMethod("getIdElement").invoke(planDefinition);
		Object planDefIdPart = planDefId.getClass().getMethod("getIdPart").invoke(planDefId);
		cpClass.getMethod("addInstantiatesCanonical", String.class).invoke(builder, planDefIdPart);
		Class<?> refClass = cl.loadClass("org.hl7.fhir.r4.model.Reference");
		Class<?> cpStatusClass = cl.loadClass("org.hl7.fhir.r4.model.CarePlan$CarePlanStatus");
		cpClass.getMethod("setSubject", refClass).invoke(builder, refClass.getConstructor(String.class).newInstance(patientId));
		cpClass.getMethod("setStatus", cpStatusClass).invoke(builder, cpStatusClass.getField("DRAFT").get(null));
		List<?> goals = (List<?>) planDefClass.getMethod("getGoal").invoke(planDefinition);
		Class<?> anyResClass = cl.loadClass("org.hl7.fhir.instance.model.api.IAnyResource");
		for (Object goal: goals) {
			cpClass.getMethod("addGoal", refClass).invoke(builder, refClass.getConstructor(anyResClass).newInstance(convertGoal(goal)));
		}
		if (encounterId != null) {
			cpClass.getMethod("setEncounter", refClass).invoke(builder, refClass.getConstructor(String.class).newInstance(encounterId));
		}
		if (practitionerId != null) {
			cpClass.getMethod("setAuthor", refClass).invoke(builder, refClass.getConstructor(String.class).newInstance(practitionerId));
		}
		if (organizationId != null) {
			cpClass.getMethod("setAuthor", refClass).invoke(builder, refClass.getConstructor(String.class).newInstance(organizationId));
		}
		if (userLanguage != null) {
			cpClass.getMethod("setLanguage", String.class).invoke(builder, userLanguage);
		}
		// Each Group of actions shares a RequestGroup
		Class<?> rgroupClass = cl.loadClass("org.hl7.fhir.r4.model.RequestGroup");
		Class<?> rstatusClass = cl.loadClass("org.hl7.fhir.r4.model.RequestGroup$RequestStatus");
		Class<?> rintentClass = cl.loadClass("org.hl7.fhir.r4.model.RequestGroup$RequestIntent");
	    Class<?> iparamsClass = cl.loadClass("org.hl7.fhir.instance.model.api.IBaseParameters");
	    Object requestGroup = rgroupClass.getConstructor().newInstance();
	    rgroupClass.getMethod("setStatus", rstatusClass).invoke(requestGroup, rstatusClass.getField("DRAFT").get(null));
	    rgroupClass.getMethod("setIntent", rintentClass).invoke(requestGroup, rintentClass.getField("PROPOSAL").get(null));
	    Method valueChildMethod = operationParametersParser.getClass().getDeclaredMethod("getValueChild", iparamsClass, String.class);
	    valueChildMethod.setAccessible(true);
	    Object valueChildKey = valueChildMethod.invoke(operationParametersParser, prefetchData, "key");
	    Optional<?> valueChildKeyOpt = castOrThrow(valueChildKey, cl.loadClass("org.hl7.fhir.r4.model.StringType"), "prefetchData key must be a String");
	    String prefetchDataKey = !valueChildKeyOpt.isPresent() ? null : (String) valueChildKeyOpt.get().getClass().getMethod("asStringValue").invoke(valueChildKeyOpt.get());
	    Method resourceChildMethod = operationParametersParser.getClass().getDeclaredMethod("getResourceChild", iparamsClass, String.class);
	    resourceChildMethod.setAccessible(true);
	    Object prefDataDesc = resourceChildMethod.invoke(operationParametersParser, prefetchData, "descriptor");
	    Object prefetchDataDescription = castOrThrow(prefDataDesc, cl.loadClass("org.hl7.fhir.r4.model.DataRequirement"), "prefetchData descriptor must be a DataRequirement").orElse(null);
	    Object prefDataData = resourceChildMethod.invoke(operationParametersParser, prefetchData, "data");
	    Object prefetchDataData = castOrThrow(prefDataData, cl.loadClass("org.hl7.fhir.instance.model.api.IBaseBundle"), "prefetchData data must be a Bundle").orElse(null);
	    Session session = new Session(planDefinition, builder, patientId, encounterId, practitionerId, organizationId,
	    		userType, userLanguage, userTaskContext, setting, settingContext, requestGroup, parameters, prefetchData,
	    		contentEndpoint, terminologyEndpoint, dataEndpoint, bundle, useServerData, mergeNestedCarePlans,
	    		prefetchDataData, prefetchDataDescription, prefetchDataKey);
	    Class<?> drClass = cl.loadClass("org.hl7.fhir.r4.model.DomainResource");
	    return cl.loadClass("org.opencds.cqf.cql.evaluator.fhir.helper.ContainedHelper").getMethod("liftContainedResourcesToParent", drClass).invoke(null, resolveActions(session));
	}

	public Map<String, Object> getEvaluatedCqlResults() {
		return new HashMap<>(evaluatedCqlResults.get());
	}
	
	private Object convertGoal(Object goal) throws ReflectiveOperationException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Class<?> goalClass = cl.loadClass("org.hl7.fhir.r4.model.Goal");
		Class<?> goalTargetClass = cl.loadClass("org.hl7.fhir.r4.model.Goal$GoalTargetComponent");
		Class<?> ccClass = cl.loadClass("org.hl7.fhir.r4.model.CodeableConcept");
		Class<?> typeClass = cl.loadClass("org.hl7.fhir.r4.model.Type");
		Object myGoal = goalClass.getConstructor().newInstance();
		Object category = goal.getClass().getMethod("getCategory").invoke(goal);
		goalClass.getMethod("setCategory", List.class).invoke(myGoal, Collections.singletonList(category));
		Object description = goal.getClass().getMethod("getDescription").invoke(goal);
		goalClass.getMethod("setDescription", ccClass).invoke(myGoal, description);
		Object priority = goal.getClass().getMethod("getPriority").invoke(goal);
		goalClass.getMethod("setPriority", ccClass).invoke(myGoal, priority);
		Object start = goal.getClass().getMethod("getStart").invoke(goal);
		goalClass.getMethod("setStart", typeClass).invoke(myGoal, start);
		List<Object> targets = new ArrayList<>();
		List<?> origTargets = (List<?>) goal.getClass().getMethod("getTarget").invoke(goal);
		for (Object target : origTargets) {
			Object myTarget = goalTargetClass.getConstructor().newInstance();
			Object detail = target.getClass().getMethod("getDetail").invoke(target);
			goalTargetClass.getMethod("setDetail", typeClass).invoke(myTarget, detail);
			Object measure = target.getClass().getMethod("getMeasure").invoke(target);
			goalTargetClass.getMethod("setMeasure", ccClass).invoke(myTarget, measure);
			Object due = target.getClass().getMethod("getDue").invoke(target);
			goalTargetClass.getMethod("setDue", typeClass).invoke(myTarget, due);
			Object extension = target.getClass().getMethod("getExtension").invoke(target);
			goalTargetClass.getMethod("setExtension", List.class).invoke(myTarget, extension);
			targets.add(myTarget);
		}
		return myGoal;
	}

	private Object resolveActions(Session session) throws ReflectiveOperationException {
		Map<String, Object> metConditions = new HashMap<String, Object>();//PlanDefinition.PlanDefinitionActionComponent
		List<?> actions = (List<?>) session.planDefinition.getClass().getMethod("getAction").invoke(session.planDefinition);
		int index = 0;
		for (Object action : actions) {
			resolveAction(session, metConditions, action, index, false);
			index++;
		}	
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Class<?> refClass = cl.loadClass("org.hl7.fhir.r4.model.Reference");
		Object result = session.requestGroup;
		//result.setAction(toRequestGroupActions(session.planDefinition.getAction()));
		Object activity = session.carePlan.getClass().getMethod("addActivity").invoke(session.carePlan);
		Class<?> anyResClass = cl.loadClass("org.hl7.fhir.instance.model.api.IAnyResource");
		activity.getClass().getMethod("setReference", refClass).invoke(activity, refClass.getConstructor(anyResClass).newInstance(result));
		Class<?> resClass = cl.loadClass("org.hl7.fhir.r4.model.Resource");
		session.carePlan.getClass().getMethod("addContained", resClass).invoke(session.carePlan, result);
		return session.carePlan;
	}

	private void resolveAction(Session session, Map<String, Object> metConditions, Object action, int index, boolean isSubItem) throws ReflectiveOperationException {
		if (meetsConditions(session, action, index)) {
			boolean hasRelatedAction = (boolean) action.getClass().getMethod("hasRelatedAction").invoke(action);
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Class<?> artClass = cl.loadClass("org.hl7.fhir.r4.model.PlanDefinition$ActionRelationshipType");
			String id = (String) action.getClass().getMethod("getId").invoke(action);
			Object after = artClass.getField("AFTER").get(null);
			if (hasRelatedAction) {
				List<?> relatedActions = (List<?>) action.getClass().getMethod("getRelatedAction").invoke(action);
				for (Object relatedActionComponent : relatedActions) {
					Object relationship = relatedActionComponent.getClass().getMethod("getRelationship").invoke(relatedActionComponent);
					if (relationship.equals(after)) {
						String actionId = (String) relatedActionComponent.getClass().getMethod("getActionId").invoke(relatedActionComponent);
						if (metConditions.containsKey(actionId)) {
							metConditions.put(id, action);
							resolveDynamicActions(session, action, index, isSubItem);
							resolveDefinition(session, action, index);
						}
					}
				}
			}
			metConditions.put(id, action);
			resolveDefinition(session, action, index);
			resolveDynamicActions(session, action, index, isSubItem);
		}
	}

	private void resolveDefinition(Session session, Object action, int index) throws ReflectiveOperationException {
		boolean hasDefCanType = (boolean) action.getClass().getMethod("hasDefinitionCanonicalType").invoke(action);
		if (hasDefCanType) {
			Object defCanType = action.getClass().getMethod("getDefinitionCanonicalType").invoke(action);
			Object defCanTypeValue = defCanType.getClass().getMethod("getValue").invoke(defCanType);
			logger.debug("Resolving definition " + defCanTypeValue);
			switch (getResourceName(defCanType)) {
			case "PlanDefinition":
				applyNestedPlanDefinition(session, defCanType, action, index);
				break;
			case "ActivityDefinition":
				applyActivityDefinition(session, defCanType, action, index);
				break;
			case "Questionnaire":
				applyQuestionnaireDefinition(session, defCanType, action, index);
				break;
			default:
				throw new RuntimeException(String.format("Unknown action definition: ", defCanType));
			}
		} else {
			boolean hasDefUriType = (boolean) action.getClass().getMethod("hasDefinitionUriType").invoke(action);
			if (hasDefUriType) {
				Object definition = action.getClass().getMethod("getDefinitionUriType").invoke(action);
				applyUriDefinition(session, definition, action, index);
			}
		}
	}

	private void applyUriDefinition(Session session, Object definition, Object action, int index) throws ReflectiveOperationException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Class<?> refClass = cl.loadClass("org.hl7.fhir.r4.model.Reference");
		Class<?> typeClass = cl.loadClass("org.hl7.fhir.r4.model.Type");
		List<?> acts = (List<?>) session.requestGroup.getClass().getMethod("getAction").invoke(session.requestGroup);
		while (index >= acts.size()) {
			session.requestGroup.getClass().getMethod("addAction").invoke(session.requestGroup);
			acts = (List<?>) session.requestGroup.getClass().getMethod("getAction").invoke(session.requestGroup);
		}
		Object act = acts.get(index);
		Object rgAction = act.getClass().getMethod("addAction").invoke(act);
		Object reference = refClass.getConstructor(String.class).newInstance(definition.getClass().getMethod("asStringValue").invoke(definition));
		rgAction.getClass().getMethod("setResource", refClass).invoke(rgAction, reference);
		rgAction.getClass().getMethod("setTitle", String.class).invoke(rgAction, action.getClass().getMethod("getTitle").invoke(action));
		rgAction.getClass().getMethod("setDescription", String.class).invoke(rgAction, action.getClass().getMethod("getDescription").invoke(action));
		rgAction.getClass().getMethod("setTextEquivalent", String.class).invoke(rgAction, action.getClass().getMethod("getTextEquivalent").invoke(action));
		rgAction.getClass().getMethod("setCode", List.class).invoke(rgAction, action.getClass().getMethod("getCode").invoke(action));
		rgAction.getClass().getMethod("setTiming", typeClass).invoke(rgAction, action.getClass().getMethod("getTiming").invoke(action));
	}

	private void applyQuestionnaireDefinition(Session session, Object definition, Object action, int index) throws ReflectiveOperationException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Object result; //IBaseResource
		try {
			String value = (String) definition.getClass().getMethod("getValue").invoke(definition);
			boolean referenceToContained = value.startsWith("#");
			if (referenceToContained) {
				result = resolveContained(session.planDefinition, value);
			} else {
				Object defStringValue = definition.getClass().getMethod("asStringValue").invoke(definition);
				Iterable<?> iter = (Iterable<?>) fhirDal.getClass().getMethod("searchByUrl", String.class, String.class).invoke("Questionnaire", defStringValue);
				Iterator<?> iterator = iter.iterator();
				if (!iterator.hasNext()) {
					throw new RuntimeException("No questionnaire found for definition: " + definition);
				}
				result = iterator.next();
			}
			applyAction(session, result, action);
			List<?> acts = (List<?>) session.requestGroup.getClass().getMethod("getAction").invoke(session.requestGroup);
			while (index >= acts.size()) {
				session.requestGroup.getClass().getMethod("addAction").invoke(session.requestGroup);
				acts = (List<?>) session.requestGroup.getClass().getMethod("getAction").invoke(session.requestGroup);
			}
			Object act = acts.get(index);
			Object rgAction = act.getClass().getMethod("addAction").invoke(act);
			Class<?> anyResClass = cl.loadClass("org.hl7.fhir.instance.model.api.IAnyResource");
			Class<?> refClass = cl.loadClass("org.hl7.fhir.r4.model.Reference");
			Class<?> resClass = cl.loadClass("org.hl7.fhir.r4.model.Resource");
			Object prefix = result.getClass().getMethod("getTitle").invoke(result);
			Object desc = result.getClass().getMethod("getDescription").invoke(result);
			if (prefix == null) {
				prefix = desc;
			}
			rgAction.getClass().getMethod("setPrefix", String.class).invoke(rgAction, prefix);
			rgAction.getClass().getMethod("setDescription", String.class).invoke(rgAction, desc);
			rgAction.getClass().getMethod("setResource", refClass).invoke(rgAction, refClass.getConstructor(anyResClass).newInstance(result));
			session.requestGroup.getClass().getMethod("addContained", resClass).invoke(session.requestGroup, result);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("ERROR: Questionnaire {} could not be applied and threw exception {}", definition, e.toString());
		}
	}

	private void applyActivityDefinition(Session session, Object definition, Object action, int index) throws ReflectiveOperationException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Class<?> iparamsClass = cl.loadClass("org.hl7.fhir.instance.model.api.IBaseParameters");
		Class<?> iresClass = cl.loadClass("org.hl7.fhir.instance.model.api.IBaseResource");
		Object result; //IBaseResource
		try {
			String value = (String) definition.getClass().getMethod("getValue").invoke(definition);
			boolean referenceToContained = value.startsWith("#");
			Object activityDefinition = null;
			if (referenceToContained) {
				activityDefinition = resolveContained(session.planDefinition, value);
				Class<?> actDefClass = cl.loadClass("org.hl7.fhir.r4.model.ActivityDefinition");
				Class<?>[] paramTypes = new Class<?>[] {actDefClass, String.class, String.class, String.class, 
					iparamsClass, iresClass, iresClass, iresClass};
				result = this.activityDefinitionProcessor.getClass().getMethod("resolveActivityDefinition", paramTypes).
						invoke(this.activityDefinitionProcessor, activityDefinition, session.patientId, session.practitionerId, 
								session.organizationId, session.parameters, session.contentEndpoint,
								session.terminologyEndpoint, session.dataEndpoint);
			} else {	
				Object defStringValue = definition.getClass().getMethod("asStringValue").invoke(definition);
				Iterable<?> iter = (Iterable<?>) fhirDal.getClass().getMethod("searchByUrl", String.class, String.class).invoke(fhirDal, "ActivityDefinition", defStringValue);
				Iterator<?> iterator = iter.iterator();
				if (!iterator.hasNext()) {
					throw new RuntimeException("No activity definition found for definition: " + definition);
				}
				activityDefinition = iterator.next();
				Class<?> idtypeClass = cl.loadClass("org.hl7.fhir.r4.model.IdType");
				Class<?>[] paramTypes = new Class<?>[] { idtypeClass, String.class, String.class, String.class,
					String.class, String.class, String.class, String.class, String.class,
					String.class, iparamsClass, iresClass, iresClass, iresClass};
					Object idElement = activityDefinition.getClass().getMethod("getIdElement").invoke(activityDefinition);
				result = this.activityDefinitionProcessor.getClass().getMethod("apply", paramTypes).invoke(
						this.activityDefinitionProcessor, idElement, session.patientId,
						session.encounterId, session.practitionerId, session.organizationId, session.userType, session.userLanguage,
						session.userTaskContext, session.setting, session.settingContext, session.parameters,
						session.contentEndpoint, session.terminologyEndpoint, session.dataEndpoint);
				if (hasMethod(activityDefinition, "getIntent") && hasMethod(result, "getIntent")) {
					try {
						Class<?> intentClass = result.getClass().getMethod("getIntent").getReturnType();
						Object srcIntent = activityDefinition.getClass().getMethod("getIntent").invoke(activityDefinition);
						String intentCode = (String) srcIntent.getClass().getMethod("toCode").invoke(srcIntent);
						if (intentCode != null) {
							Object tgtIntent = intentClass.getMethod("fromCode", String.class).invoke(null, intentCode);
							result.getClass().getMethod("setIntent", intentClass).invoke(result, tgtIntent);
						}
					} catch (Exception e) {
						logger.warn("Cannot override intent enum for result " + result + ": " + e.getMessage());
					}
				}
			}
			applyAction(session, result, action);
			List<?> acts = (List<?>) session.requestGroup.getClass().getMethod("getAction").invoke(session.requestGroup);
			while (index >= acts.size()) {
				session.requestGroup.getClass().getMethod("addAction").invoke(session.requestGroup);
				acts = (List<?>) session.requestGroup.getClass().getMethod("getAction").invoke(session.requestGroup);
			}
			Object act = acts.get(index);
			Object rgAction = act.getClass().getMethod("addAction").invoke(act);
			Class<?> anyResClass = cl.loadClass("org.hl7.fhir.instance.model.api.IAnyResource");
			Class<?> refClass = cl.loadClass("org.hl7.fhir.r4.model.Reference");
			Class<?> resClass = cl.loadClass("org.hl7.fhir.r4.model.Resource");
			rgAction.getClass().getMethod("setResource", refClass).invoke(rgAction, refClass.getConstructor(anyResClass).newInstance(result));
			Object prefix = activityDefinition.getClass().getMethod("getTitle").invoke(activityDefinition);
			Object desc = activityDefinition.getClass().getMethod("getDescription").invoke(activityDefinition);
			if (prefix == null) {
				prefix = desc;
			}
			rgAction.getClass().getMethod("setPrefix", String.class).invoke(rgAction, prefix);
			rgAction.getClass().getMethod("setTextEquivalent", String.class).invoke(rgAction, desc);
			Object type = rgAction.getClass().getMethod("getType").invoke(rgAction);
			Object coding = type.getClass().getMethod("addCoding").invoke(type);
			coding.getClass().getMethod("setCode", String.class).invoke(coding, "fire-event");
			session.requestGroup.getClass().getMethod("addContained", resClass).invoke(session.requestGroup, result);
		} catch (Exception e) {
			logger.error("ERROR: ActivityDefinition {} could not be applied and threw exception {}", definition, e.toString(), e);
		}
	}

	private boolean hasMethod(Object object, String methodName) {
		for (Method m : object.getClass().getMethods()) {
			if (m.getName().equals(methodName)) {
				return true;
			}
		}
		return false;
	}

	private void applyNestedPlanDefinition(Session session, Object definition, Object action, int index) throws ReflectiveOperationException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Object carePlan; //CarePlan
		Object defStringValue = definition.getClass().getMethod("asStringValue").invoke(definition);
		Iterable<?> iter = (Iterable<?>) fhirDal.getClass().getMethod("searchByUrl", String.class, String.class).invoke("PlanDefinition", defStringValue);
		Iterator<?> iterator = iter.iterator();
		if (!iterator.hasNext()) {
			throw new RuntimeException("No plan definition found for definition: " + definition);
		}
		Object planDefinition = iterator.next();
		Object idElement = planDefinition.getClass().getMethod("getIdElement").invoke(planDefinition);
		carePlan = apply(planDefinition, idElement, session.patientId, session.encounterId,
				session.practitionerId, session.organizationId, session.userType, session.userLanguage, session.userTaskContext,
				session.setting, session.settingContext, session.mergeNestedCarePlans, session.parameters,
				session.useServerData, session.bundle, session.prefetchData, session.dataEndpoint, session.contentEndpoint,
				session.terminologyEndpoint);
		applyAction(session, carePlan, action);
		// Add an action to the request group which points to this CarePlan
		List<?> acts = (List<?>) session.requestGroup.getClass().getMethod("getAction").invoke(session.requestGroup);
		while (index >= acts.size()) {
			session.requestGroup.getClass().getMethod("addAction").invoke(session.requestGroup);
			acts = (List<?>) session.requestGroup.getClass().getMethod("getAction").invoke(session.requestGroup);
		}
		Object act = acts.get(index);
		Object rgAction = act.getClass().getMethod("addAction").invoke(act);
		Class<?> anyResClass = cl.loadClass("org.hl7.fhir.instance.model.api.IAnyResource");
		Class<?> refClass = cl.loadClass("org.hl7.fhir.r4.model.Reference");
		Class<?> resClass = cl.loadClass("org.hl7.fhir.r4.model.Resource");
		rgAction.getClass().getMethod("setResource", refClass).invoke(rgAction, refClass.getConstructor(anyResClass).newInstance(carePlan));
		session.requestGroup.getClass().getMethod("addContained", resClass).invoke(session.requestGroup, carePlan);
		List<?> instCanonical = (List<?>) carePlan.getClass().getMethod("getInstantiatesCanonical").invoke(carePlan);
		for (Object c : instCanonical) {
			Object valueString = c.getClass().getMethod("getValueAsString").invoke(c);
			session.carePlan.getClass().getMethod("addInstantiatesCanonical", String.class).invoke(session.carePlan, valueString);
		}
	}

	private void applyAction(Session session, Object result, Object action) throws ReflectiveOperationException {
		String type = (String) result.getClass().getMethod("fhirType").invoke(result);
		switch (type) {
		case "Task":
			result = resolveTask(session, result, action);
			break;
		}
	}

	private Object resolveTask(Session session, Object task, Object action) throws ReflectiveOperationException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Class<?> extClass = cl.loadClass("org.hl7.fhir.r4.model.Extension");
		Class<?> typeClass = cl.loadClass("org.hl7.fhir.r4.model.Type");
		Class<?> refClass = cl.loadClass("org.hl7.fhir.r4.model.Reference");
		Class<?> anyResClass = cl.loadClass("org.hl7.fhir.instance.model.api.IAnyResource");
		Object actionId = action.getClass().getMethod("getId").invoke(action);
		Object idType = cl.loadClass("org.hl7.fhir.r4.model.IdType").getConstructor(String.class).newInstance(actionId);
		task.getClass().getMethod("setId", idType.getClass()).invoke(task, idType);
		boolean hasRelatedAction = (boolean) action.getClass().getMethod("hasRelatedAction").invoke(action);
	    if (hasRelatedAction) {
	    	List<?> relatedActions = (List<?>) action.getClass().getMethod("getRelatedAction").invoke(action);
	        for (Object relatedAction : relatedActions) {
	        	Object next = extClass.getConstructor().newInstance();
	        	extClass.getMethod("setUrl", String.class).invoke(next, "http://hl7.org/fhir/aphl/StructureDefinition/next");
	        	boolean hasOffset = (boolean) relatedAction.getClass().getMethod("hasOffset").invoke(relatedAction);
	        	if (hasOffset) {
	        		Object offsetExtension = extClass.getConstructor().newInstance();
	        		extClass.getMethod("setUrl", String.class).invoke(offsetExtension, "http://hl7.org/fhir/aphl/StructureDefinition/offset");
	        		Object offset = relatedAction.getClass().getMethod("getOffset").invoke(relatedAction);
	        		extClass.getMethod("setValue", typeClass).invoke(offsetExtension, offset);
	        		extClass.getMethod("addExtension", extClass).invoke(next, offsetExtension);
	        	}
	        	Object target = extClass.getConstructor().newInstance();
	        	Object relActionId = relatedAction.getClass().getMethod("getActionId").invoke(relatedAction); 
	        	Object targetRef = refClass.getConstructor(String.class).newInstance("#" + relActionId);
	        	extClass.getMethod("setUrl", String.class).invoke(target, "http://hl7.org/fhir/aphl/StructureDefinition/target");
	        	extClass.getMethod("setValue", typeClass).invoke(target, targetRef);
	        	extClass.getMethod("addExtension", extClass).invoke(next, target);
	        	task.getClass().getMethod("addExtension", extClass).invoke(task, next);
	        }
	    }
	    boolean hasCondition = (boolean) action.getClass().getMethod("hasCondition").invoke(action);
	    if (hasCondition) {
	    	List<?> conditionComponents = (List<?>) action.getClass().getMethod("getCondition").invoke(action);
	    	for (Object conditionComponent : conditionComponents) {
	    		Object condition = extClass.getConstructor().newInstance();
	    		extClass.getMethod("setUrl", String.class).invoke(condition, "http://hl7.org/fhir/aphl/StructureDefinition/condition");
	    		Object expression = conditionComponent.getClass().getMethod("getExpression").invoke(conditionComponent);
	    		extClass.getMethod("setValue", typeClass).invoke(condition, expression);
	    		boolean hasExtension = (boolean) conditionComponent.getClass().getMethod("hasExtension", String.class).invoke(conditionComponent, alternateExpressionExtension);
	    		if (hasExtension) {
	    			Object extensionByUrl = conditionComponent.getClass().getMethod("getExtensionByUrl", String.class).invoke(conditionComponent, alternateExpressionExtension);
	    			condition.getClass().getMethod("addExtension", extClass).invoke(condition, extensionByUrl);
	    		}
	    		task.getClass().getMethod("addExtension", extClass).invoke(task, condition);
	    	}
	    }
	    boolean hasInput = (boolean) action.getClass().getMethod("hasInput").invoke(action);
	    if (hasInput) {
	    	List<?> dataRequirements = (List<?>) action.getClass().getMethod("getInput").invoke(action);
	    	for (Object dataRequirement : dataRequirements) {
	    		Object input = extClass.getConstructor().newInstance();
	    		extClass.getMethod("setUrl", String.class).invoke(input, "http://hl7.org/fhir/aphl/StructureDefinition/input");
	    		extClass.getMethod("setValue", typeClass).invoke(input, dataRequirement);
	    		task.getClass().getMethod("addExtension", extClass).invoke(task, input);
	      }
	    }
	    Object basedOnRef = refClass.getConstructor(anyResClass).newInstance(session.requestGroup);
	    Object fhirType = session.requestGroup.getClass().getMethod("fhirType").invoke(session.requestGroup);
	    basedOnRef.getClass().getMethod("setType", String.class).invoke(basedOnRef, fhirType);
	    task.getClass().getMethod("addBasedOn", refClass).invoke(task, basedOnRef);
	    return task;
	}

	private boolean resolveDynamicActions(Session session, Object action, int index, boolean isSubItem) throws ReflectiveOperationException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Class<?> expClass = cl.loadClass("org.hl7.fhir.r4.model.Expression");
		Class<?> extClass = cl.loadClass("org.hl7.fhir.r4.model.Extension");
		//Class<?> strTypeClass = cl.loadClass("org.hl7.fhir.r4.model.StringType");
		Class<?> typeClass = cl.loadClass("org.hl7.fhir.r4.model.Type");
		Class<?> baseClass = cl.loadClass("org.hl7.fhir.r4.model.Base");
		boolean somethingFound = false;
		List<?> dynamicValues = (List<?>) action.getClass().getMethod("getDynamicValue").invoke(action);
		for (Object dynamicValue : dynamicValues) {
			String path = (String) dynamicValue.getClass().getMethod("getPath").invoke(dynamicValue);
			Object expression = dynamicValue.getClass().getMethod("getExpression").invoke(dynamicValue);
			logger.info("Resolving dynamic value {} {}", path, expression);
			ensureDynamicValueExpression(dynamicValue);
			boolean hasLanguage = (boolean) expClass.getMethod("hasLanguage").invoke(expression);
			if (hasLanguage) {
				String libraryToBeEvaluated = ensureLibrary(session, expression);
				String language = (String) expClass.getMethod("getLanguage").invoke(expression);
				String exp = (String) expClass.getMethod("getExpression").invoke(expression);
				List<?> input = (List<?>) action.getClass().getMethod("getInput").invoke(action);
				Object result = evaluateConditionOrDynamicValue(exp, language, libraryToBeEvaluated, session, input);
				boolean hasExtension = (boolean) expClass.getMethod("hasExtension", String.class).invoke(expression, alternateExpressionExtension);
				if (result == null && hasExtension) {
					Object altext = expClass.getMethod("getExtensionByUrl", String.class).invoke(expression, alternateExpressionExtension);
					Object alternateExpressionValue = altext.getClass().getMethod("getValue").invoke(altext);
					if (!expClass.isInstance(alternateExpressionValue)) {
						throw new RuntimeException("Expected alternateExpressionExtensionValue to be of type Expression");
					}
					boolean expHasLanguage = (boolean) expClass.getMethod("hasLanguage").invoke(alternateExpressionValue);
					if (expHasLanguage) {
						libraryToBeEvaluated = ensureLibrary(session, alternateExpressionValue);
						language = (String) expClass.getMethod("getLanguage").invoke(alternateExpressionValue);
						result = evaluateConditionOrDynamicValue((String) expClass.getMethod("getExpression").invoke(alternateExpressionValue), language, libraryToBeEvaluated, session, input);
					}
				}
				Class<?> dtClass = cl.loadClass("org.opencds.cqf.cql.engine.runtime.DateTime");
				if (dtClass.isInstance(result)) {
					Object dateTime = result.getClass().getMethod("getDateTime").invoke(result);
					Instant instant = (Instant) dateTime.getClass().getMethod("toInstant").invoke(dateTime);
					result = Date.from(instant);
				}
				
				if (path != null && path.equals("$this")) {
					session.carePlan = result;
				} else if (path != null && (path.startsWith("action") || path.startsWith("%action"))) {
					try {
						String propertyType = path.substring(path.indexOf(".")+1);
						if (propertyType.startsWith("extension") && propertyType.contains(".")) {
							propertyType = propertyType.substring(0, propertyType.indexOf("."));
						}
						if ("extension".equals(propertyType) && typeClass.isInstance(result)) {
							Object ext = extClass.getConstructor().newInstance();
							extClass.getMethod("setValue", typeClass).invoke(ext, result);
							extClass.getMethod("setUrl", String.class).invoke(ext, path);
							result = ext;
						}
						action.getClass().getMethod("setProperty", String.class, baseClass).invoke(action, propertyType, result);
						somethingFound = true;
					} catch (Exception e) {
						throw new RuntimeException(String.format("Could not set path %s to value: %s", path, result));
					}
				} else {
					try {
						session.carePlan.getClass().getMethod("setProperty", String.class, baseClass).invoke(session.carePlan, path, result);
					} catch (Exception e) {
						throw new RuntimeException(String.format("Could not set path %s to value: %s", path, result));
					}
				}
			}
		}
		if (somethingFound == true) {
			List<?> acts = (List<?>) session.requestGroup.getClass().getMethod("getAction").invoke(session.requestGroup);
			while (acts.size() <= index) {
				session.requestGroup.getClass().getMethod("addAction").invoke(session.requestGroup);
				acts = (List<?>) session.requestGroup.getClass().getMethod("getAction").invoke(session.requestGroup);
			}
			Object act = acts.get(index);
			if (isSubItem) {
				act = act.getClass().getMethod("addAction").invoke(act);
			}
			Object title = action.getClass().getMethod("getTitle").invoke(action);
			Object description = action.getClass().getMethod("getDescription").invoke(action);
			Object textEquivalent = action.getClass().getMethod("getTextEquivalent").invoke(action);
			Object prefix = action.getClass().getMethod("getPrefix").invoke(action);
			Object code = action.getClass().getMethod("getCode").invoke(action);
			Object timing = action.getClass().getMethod("getTiming").invoke(action);
			Object id = action.getClass().getMethod("getId").invoke(action);
			act.getClass().getMethod("setTitle", String.class).invoke(act, title);
			act.getClass().getMethod("setDescription", String.class).invoke(act, description);
			if (prefix == null) {
				if (act.getClass().getMethod("getPrefix").invoke(act) == null) {
					act.getClass().getMethod("setPrefix", String.class).invoke(act, description);
				}
			} else {
				act.getClass().getMethod("setPrefix", String.class).invoke(act, prefix);
			}
			boolean alreadyHasTextEq = (boolean) act.getClass().getMethod("hasTextEquivalent").invoke(act);
			if (!alreadyHasTextEq) { // needed to preserve ActivityDefinition's description
				act.getClass().getMethod("setTextEquivalent", String.class).invoke(act, textEquivalent);
			}
			Object cctype = action.getClass().getMethod("getType").invoke(action);
			if (cctype != null) {
				act.getClass().getMethod("setType", cl.loadClass("org.hl7.fhir.r4.model.CodeableConcept")).invoke(act, cctype);
			}
			act.getClass().getMethod("setId", String.class).invoke(act, id);
			act.getClass().getMethod("setCode", List.class).invoke(act, code);
			act.getClass().getMethod("setTiming", typeClass).invoke(act, timing);
			boolean hasExtension = (boolean) action.getClass().getMethod("hasExtension").invoke(action);
			if (hasExtension) {
				List<?> exts = (List<?>) action.getClass().getMethod("getExtension").invoke(action);
				for (Object ext : exts) {
					act.getClass().getMethod("addExtension", extClass).invoke(act, ext);
				}
			}
			boolean hasDocumentation = (boolean) action.getClass().getMethod("hasDocumentation").invoke(action);
			Object publisher = session.planDefinition.getClass().getMethod("getPublisher").invoke(session.planDefinition);
			if (hasDocumentation) {
				Class<?> relArtClass = cl.loadClass("org.hl7.fhir.r4.model.RelatedArtifact");
				Object docFirstRep = action.getClass().getMethod("getDocumentationFirstRep").invoke(action);
				Object label = docFirstRep.getClass().getMethod("getLabel").invoke(docFirstRep);
				if (label == null) {
					label = publisher;
					docFirstRep.getClass().getMethod("setLabel", String.class).invoke(docFirstRep, label);
				}
				act.getClass().getMethod("addDocumentation", relArtClass).invoke(act, docFirstRep);
			} else if (publisher != null) {
				Object doc = act.getClass().getMethod("addDocumentation").invoke(act);
				doc.getClass().getMethod("setLabel", String.class).invoke(doc, publisher);
			}
			
			boolean hasSelectionBehavior = (boolean) action.getClass().getMethod("hasSelectionBehavior").invoke(action);
			if (hasSelectionBehavior) {
				Class<?> asbClass = cl.loadClass("org.hl7.fhir.r4.model.PlanDefinition$ActionSelectionBehavior");
				Class<?> rgasbClass = cl.loadClass("org.hl7.fhir.r4.model.RequestGroup$ActionSelectionBehavior");
				Object actSelBehavior = action.getClass().getMethod("getSelectionBehavior").invoke(action);
				Object actSelBehaviorName = asbClass.getMethod("name").invoke(actSelBehavior);
				Object selBehavior = rgasbClass.getMethod("valueOf", String.class).invoke(null, actSelBehaviorName);
				act.getClass().getMethod("setSelectionBehavior", rgasbClass).invoke(act, selBehavior);
			}
		}
		return somethingFound;
	}
	
	private Boolean meetsConditions(Session session, Object action, int index) throws ReflectiveOperationException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Class<?> expClass = cl.loadClass("org.hl7.fhir.r4.model.Expression");
		Class<?> boolClass = cl.loadClass("org.hl7.fhir.r4.model.BooleanType");
		boolean hasAction = (boolean) action.getClass().getMethod("hasAction").invoke(action);
		if (hasAction) {
			List<?> actions = (List<?>) action.getClass().getMethod("getAction").invoke(action);
			for (Object containedAction : actions) {
				Map<String, Object> metConditions = new HashMap<String, Object>();
				resolveAction(session, metConditions, containedAction, index, true);
			}
		}
		Object type = session.planDefinition.getClass().getMethod("getType").invoke(session.planDefinition);
		boolean hasCoding = (boolean) type.getClass().getMethod("hasCoding").invoke(type);
		if (hasCoding) {
			List<?> planDefinitionTypeCoding = (List<?>) type.getClass().getMethod("getCoding").invoke(type);
			for (Object coding : planDefinitionTypeCoding) {
				String code = (String) coding.getClass().getMethod("getCode").invoke(coding);
				if (code.equals("workflow-definition")) {
					// logger.info(String.format("Found a workflow definition type for PlanDefinition % conditions should be evaluated at task execution time."), session.planDefinition.getUrl());
					return true;
				}
			}
		}
		List<?> conditions = (List<?>) action.getClass().getMethod("getCondition").invoke(action);
		for (Object condition : conditions) {
			ensureConditionExpression(condition);
			Object expression = condition.getClass().getMethod("getExpression").invoke(condition);
			boolean hasLanguage = (boolean) expClass.getMethod("hasLanguage").invoke(expression);
			if (hasLanguage) {
				String libraryToBeEvaluated = ensureLibrary(session, expression);
				String language = (String) expClass.getMethod("getLanguage").invoke(expression);
				String exp = (String) expClass.getMethod("getExpression").invoke(expression);
				List<?> input = (List<?>) action.getClass().getMethod("getInput").invoke(action);
				Object result = evaluateConditionOrDynamicValue(exp, language, libraryToBeEvaluated, session, input);
				boolean hasExtension = (boolean) expClass.getMethod("hasExtension", String.class).invoke(expression, alternateExpressionExtension);
				if (result == null && hasExtension) {
					Object altext = expClass.getMethod("getExtensionByUrl", String.class).invoke(expression, alternateExpressionExtension);
					Object alternateExpressionValue = altext.getClass().getMethod("getValue").invoke(altext);
					if (!expClass.isInstance(alternateExpressionValue)) {
						throw new RuntimeException("Expected alternateExpressionExtensionValue to be of type Expression");
					}
					boolean expHasLanguage = (boolean) expClass.getMethod("hasLanguage").invoke(alternateExpressionValue);
					if (expHasLanguage) {
						libraryToBeEvaluated = ensureLibrary(session, alternateExpressionValue);
						language = (String) expClass.getMethod("getLanguage").invoke(alternateExpressionValue);
						result = evaluateConditionOrDynamicValue((String) expClass.getMethod("getExpression").invoke(alternateExpressionValue), language, libraryToBeEvaluated, session, input);
					}
				}
				if (result == null) {
					logger.warn("Expression Returned null");
					return false;
				}
				if (!(boolClass.isInstance(result))) {
					logger.warn("The condition returned a non-boolean value: " + result.getClass().getSimpleName());
					continue;
				}
				boolean value = (boolean) boolClass.getMethod("booleanValue").invoke(result);
				if (!value) {
					String id = (String) condition.getClass().getMethod("getId").invoke(condition);
					logger.info("The result of condition id {} expression language {} is false", id, language);
					return false;
				}
			}
		}
		return true;
	}

	protected String ensureLibrary(Session session, Object expression) throws ReflectiveOperationException {
		boolean hasReference = (boolean) expression.getClass().getMethod("hasReference").invoke(expression);
		if (hasReference) {
			return (String) expression.getClass().getMethod("getReference").invoke(expression);
		}
		Object exp = expression.getClass().getMethod("getExpression").invoke(expression);
		logger.warn(String.format("No library reference for expression: %s", exp));
		List<?> library = (List<?>) session.planDefinition.getClass().getMethod("getLibrary").invoke(session.planDefinition);
		if (library.size() == 1) {
			Object firstItem = library.get(0);
			return (String) firstItem.getClass().getMethod("asStringValue").invoke(firstItem);
		}
		logger.warn("No primary library defined");
		return null;
	}

	protected void ensureConditionExpression(Object condition) throws ReflectiveOperationException {
		boolean hasExpression = (boolean) condition.getClass().getMethod("hasExpression").invoke(condition);
		if (!hasExpression) {
			logger.error("Missing condition expression");
			throw new RuntimeException("Missing condition expression");
		}
	}

	protected void ensureDynamicValueExpression(Object dynamicValue) throws ReflectiveOperationException {
		boolean hasExpression = (boolean) dynamicValue.getClass().getMethod("hasExpression").invoke(dynamicValue);
		if (!hasExpression) {
			logger.error("Missing dynamicValue expression");
			throw new RuntimeException("Missing dynamicValue expression");
		}
	}

	protected static String getResourceName(Object canonical) throws ReflectiveOperationException {
		boolean hasValue = (boolean) canonical.getClass().getMethod("hasValue").invoke(canonical);
		if (hasValue) {
			String id = (String) canonical.getClass().getMethod("getValue").invoke(canonical);
			if (id.contains("/")) {
				id = id.replace(id.substring(id.lastIndexOf("/")), "");
				return id.contains("/") ? id.substring(id.lastIndexOf("/") + 1) : id;
			}
			return null;
		}
		throw new RuntimeException("CanonicalType must have a value for resource name extraction");
	}

	public Object getParameterComponentByName(Object params, String name) throws ReflectiveOperationException {
		List<?> stream = (List<?>) params.getClass().getMethod("getParameter").invoke(params);
		Object item = null;
		for (Object x : stream) {
			String xname = (String) x.getClass().getMethod("getName").invoke(x);
			if (xname.equals(name)) {
				item = x;
				break;
			}
		}
		if (item != null) {
			boolean hasValue = (boolean) item.getClass().getMethod("hasValue").invoke(item);
			String method = hasValue ? "getValue" : "getResource";
			return item.getClass().getMethod(method).invoke(item);
		}
		return null;
	}
  
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected Object evaluateConditionOrDynamicValue(String expression, String language, String libraryToBeEvaluated, Session session, List<?> dataRequirements) throws ReflectiveOperationException { //List<DataRequirement> 
		Object params = resolveInputParameters(dataRequirements);
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Class<?> paramsClass = cl.loadClass("org.hl7.fhir.r4.model.Parameters");
		Class<?> iparamsClass = cl.loadClass("org.hl7.fhir.instance.model.api.IBaseParameters");
		Class<?> iresClass = cl.loadClass("org.hl7.fhir.instance.model.api.IBaseResource");
		Class<?> ibundleClass = cl.loadClass("org.hl7.fhir.instance.model.api.IBaseBundle");
		Class<?> ibaseClass = cl.loadClass("org.hl7.fhir.instance.model.api.IBase"); 
		Class<?>[] libEvalParamTypes = new Class[] {String.class, String.class, iparamsClass, iresClass, iresClass, iresClass, ibundleClass, Set.class};
		if (session.parameters != null && paramsClass.isInstance(session.parameters)) {
			List paramsParams = (List) params.getClass().getMethod("getParameter").invoke(params);
			List<?> sessionParams = (List<?>) session.parameters.getClass().getMethod("getParameter").invoke(session.parameters);
			paramsParams.addAll(sessionParams);
		}
		if (libraryResults.isEmpty()) {
			Object libraryResult = libraryProcessor.getClass().getMethod("evaluate", libEvalParamTypes).
					invoke(libraryProcessor, libraryToBeEvaluated, session.patientId,
					params, session.contentEndpoint, session.terminologyEndpoint,
					session.dataEndpoint, session.bundle, null);
			if (paramsClass.isInstance(libraryResult)) {
				List<?> libParams = (List<?>) libraryResult.getClass().getMethod("getParameter").invoke(libraryResult);
				for (Object param : libParams) {
					boolean hasName = (boolean) param.getClass().getMethod("hasName").invoke(param);
					boolean hasValue = (boolean) param.getClass().getMethod("hasValue").invoke(param);
					boolean hasResource = (boolean) param.getClass().getMethod("hasResource").invoke(param);
					if (hasName) {
						String name = (String) param.getClass().getMethod("getName").invoke(param);
						if (hasValue) {
							Object value = param.getClass().getMethod("getValue").invoke(param);
							libraryResults.put(name, value);
						} else if (hasResource) {
							Object resource = param.getClass().getMethod("getResource").invoke(param);
							libraryResults.put(name, resource);
						} else {
							logger.warn("Expression " + name + " has no value");
						}
					}
						
					
				}
			}
		}
		Object result = null;
		switch (language) {
		case "text/cql":
		case "text/cql.expression":
		case "text/cql-expression": 
			String key = generateExpressionKey(expression, params);
			if (libraryResults.containsKey(key)) {
				result = libraryResults.get(key);
			} else if (libraryResults.containsKey(expression)) {
				result = libraryResults.get(expression);
			} else {	
			    result = expressionEvaluator.getClass().getMethod("evaluate", String.class, iparamsClass).invoke(expressionEvaluator, expression, params);
			    libraryResults.put(key, result);
			}
			break;
		case "text/cql-identifier":
		case "text/cql.identifier":
		case "text/cql.name":
		case "text/cql-name":
			String keyL = generateExpressionKey(expression, params);
			if (libraryResults.containsKey(keyL)) {
				result = libraryResults.get(keyL);
			} else if (libraryResults.containsKey(expression)) {
				result = libraryResults.get(expression);
			} else {	
				result = libraryProcessor.getClass().getMethod("evaluate", libEvalParamTypes).invoke(libraryProcessor, 
					libraryToBeEvaluated, session.patientId, params, session.contentEndpoint, 
					session.terminologyEndpoint, session.dataEndpoint, session.bundle, Collections.singleton(expression));
				libraryResults.put(keyL, result);
			}
			break;
		case "text/fhirpath":
			List<?> outputs;
			try {
				//<T extends IBase> List<T> evaluate(IBase theInput, String thePath, Class<T> theReturnType);
				outputs = (List<?>) fhirPath.getClass().getMethod("evaluate", ibaseClass, String.class, Class.class).invoke(null, expression, ibaseClass);
			} catch (RuntimeException e) {
				throw new IllegalArgumentException("Error evaluating FHIRPath expression", e);
			}
			if (outputs == null || outputs.isEmpty()) {
				result = null;
			} else if (outputs.size() == 1) {
				result = outputs.get(0);
			} else {
				throw new IllegalArgumentException("Expected only one value when evaluating FHIRPath expression: " + expression);
			}
			break;
		default:
			logger.warn("An action language other than CQL was found: " + language);
		}
		if (result != null) {
			if (paramsClass.isInstance(result)) {
				result = getParameterComponentByName(result, expression);
			}
		}
		Map<String, Object> allresults = evaluatedCqlResults.get();
		Object idTypeObj = cl.loadClass("org.hl7.fhir.r4.model.IdType").getConstructor(String.class).newInstance(libraryToBeEvaluated);
		String keyName = (String) idTypeObj.getClass().getMethod("getIdPart").invoke(idTypeObj) + "_" + expression;
		if (allresults.containsKey(keyName)) {
			Object value = allresults.get(keyName);
			if (!allresults.containsKey(keyName + "_multiple")) {
				List<Object> list = new ArrayList<>();
				list.add(result);
				allresults.put(keyName, list);
				allresults.put(keyName + "_multiple", true);
			} else {
				@SuppressWarnings("unchecked")
				List<Object> list = (List<Object>) value;
				list.add(result);
			}
		} else {
			allresults.put(keyName, result);
		}
		return result;
	}

	private String generateExpressionKey(String expression, Object params) throws ReflectiveOperationException {
		StringBuilder sb = new StringBuilder();
		List<?> parameters = (List<?>) params.getClass().getMethod("getParameter").invoke(params);
		for (Object param : parameters) {
			boolean hasName = (boolean) param.getClass().getMethod("hasName").invoke(param);
			boolean hasValue = (boolean) param.getClass().getMethod("hasValue").invoke(param);
			boolean hasResource = (boolean) param.getClass().getMethod("hasResource").invoke(param);
			if (hasName && hasValue) {
				Object name = param.getClass().getMethod("getName").invoke(param);
				Object value = param.getClass().getMethod("getValue").invoke(param);
				sb.append(name).append("=").append(value);
			} else if (hasName && hasResource) {
				Object name = param.getClass().getMethod("getName").invoke(param);
				Object resource = param.getClass().getMethod("getResource").invoke(param);
				Object resourceType = resource.getClass().getMethod("getResourceType").invoke(resource);
				Object id = resource.getClass().getMethod("getId").invoke(resource);
				sb.append(name).append("=>").append(resourceType).append("/").append(id);
			}
		}
		return expression + ":" + sb.toString();
	}

	private Object resolveInputParameters(List<?> dataRequirements) throws ReflectiveOperationException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Class<?> resClass = cl.loadClass("org.hl7.fhir.r4.model.Resource");
		Class<?> paramsClass = cl.loadClass("org.hl7.fhir.r4.model.Parameters");
		Class<?> paramCompClass = cl.loadClass("org.hl7.fhir.r4.model.Parameters$ParametersParameterComponent");
		Class<?> typeClass = cl.loadClass("org.hl7.fhir.r4.model.Type");
		Class<?> boolClass = cl.loadClass("org.hl7.fhir.r4.model.BooleanType");
		Class<?> paramDefClass = cl.loadClass("org.hl7.fhir.r4.model.ParameterDefinition");
		Class<?> iparamsClass = cl.loadClass("org.hl7.fhir.instance.model.api.IBaseParameters");
		Object params = paramsClass.getConstructor().newInstance();
		if (dataRequirements == null) {
			return params;
		}
		for (Object req : dataRequirements) {
			Object dataReqType = req.getClass().getMethod("getType").invoke(req);
			Iterable<?> iter = (Iterable<?>) fhirDal.getClass().getMethod("search", String.class).invoke(fhirDal, dataReqType);
			Iterator<?> resources = iter.iterator();
			if (resources != null && resources.hasNext()) {
				int index = 0;
				Boolean found = true;
				while (resources.hasNext()) {
					Object resource = resources.next();
					Object parameter = paramCompClass.getConstructor().newInstance();
					String reqId = (String) req.getClass().getMethod("getId").invoke(req);
					paramCompClass.getMethod("setName", String.class).invoke(parameter, "%" + String.format("%s", reqId));
					boolean hasCodeFilter = (boolean) req.getClass().getMethod("hasCodeFilter").invoke(req);
					if (hasCodeFilter) {
						List<?> codeFilter = (List<?>) req.getClass().getMethod("getCodeFilter").invoke(req);
						for (Object filter : codeFilter) {//DataRequirement.DataRequirementCodeFilterComponent filter
							Object codeFilterParam = paramsClass.getConstructor().newInstance();
							Object codeFilterParamParam = paramsClass.getMethod("addParameter").invoke(codeFilterParam);
							paramCompClass.getMethod("setName", String.class).invoke(codeFilterParamParam, "%resource");
							paramCompClass.getMethod("setResource", resClass).invoke(codeFilterParamParam, resource);
							if (filter != null) {
								boolean hasPath = (boolean) filter.getClass().getMethod("hasPath").invoke(filter);
								boolean hasValueSet = (boolean) filter.getClass().getMethod("hasValueSet").invoke(filter);
								if(hasPath && hasValueSet) {
									String filterValueSet = (String) filter.getClass().getMethod("getValueSet").invoke(filter);
									Iterable<?> valueset = (Iterable<?>) fhirDal.getClass().getMethod("searchByUrl", String.class, String.class).invoke(fhirDal, "ValueSet", filterValueSet);
									if (valueset != null && valueset.iterator().hasNext()) {
										Object codeFilterparamItem = paramsClass.getMethod("addParameter").invoke(codeFilterParam);
										paramCompClass.getMethod("setName", String.class).invoke(codeFilterparamItem, "%valueset");
										paramCompClass.getMethod("setResource", resClass).invoke(codeFilterparamItem, valueset.iterator().next());
										String filterPath = (String) filter.getClass().getMethod("getPath").invoke(filter);
										String codeFilterExpression = "%" + String.format("resource.%s.where(code.memberOf(\'%s\'))", filterPath, "%" + "valueset");
										Object codeFilterResult = expressionEvaluator.getClass().getMethod("evaluate", String.class, iparamsClass).
												invoke(expressionEvaluator, codeFilterExpression, codeFilterParam);
										Method valueChildMethod = operationParametersParser.getClass().getDeclaredMethod("getValueChild", iparamsClass, String.class);
										valueChildMethod.setAccessible(true);
										Object tempResult = valueChildMethod.invoke(operationParametersParser, codeFilterResult, "return");
										if (tempResult != null && boolClass.isInstance(tempResult)) {
											found = Boolean.valueOf(String.valueOf(boolClass.getMethod("booleanValue").invoke(tempResult)));
										}
									}
									logger.debug(String.format("Could not find ValueSet with url %s on the local server.", filterValueSet));
								}
							}
						}
					}
					if (!resources.hasNext() && index == 0) {
						Object paramDef = paramDefClass.getConstructor().newInstance();
						paramDefClass.getMethod("setMax", String.class).invoke(paramDef, "*");
						paramDefClass.getMethod("setName", String.class).invoke(paramDef, "%" + reqId);
						paramCompClass.getMethod("addExtension", typeClass).invoke(parameter, "http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-parameterDefinition", paramDef);
						if (found) {
							paramCompClass.getMethod("setResource", resClass).invoke(parameter, resource);
						}
					} else {
						if (!found) {
							index++;
							continue;
						}
						paramCompClass.getMethod("setResource", resClass).invoke(parameter, resource);
					}
					paramsClass.getMethod("addParameter", paramCompClass).invoke(params, parameter);
					index++;
				}
			} else {
				Object parameter = params.getClass().getMethod("addParameter").invoke(params);
				String reqId = (String) req.getClass().getMethod("getId").invoke(req);
				parameter.getClass().getMethod("setName", String.class).invoke(parameter, "%" + String.format("%s", reqId));
				Object paramDef = paramDefClass.getConstructor().newInstance();
				paramDefClass.getMethod("setMax", String.class).invoke(paramDef, "*");
				paramDefClass.getMethod("setName", String.class).invoke(paramDef, ("%" + reqId));
				parameter.getClass().getMethod("addExtension", String.class, typeClass).invoke(parameter, "http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-parameterDefinition", paramDef);
			}
		}
		return params;
	}

	protected Object resolveContained(Object resource, String id) throws ReflectiveOperationException {
		List<?> stream = (List<?>) resource.getClass().getMethod("getContained").invoke(resource);
		for (Object item : stream) {
			boolean hasIdElement = (boolean) item.getClass().getMethod("hasIdElement").invoke(item);
			if (hasIdElement) {
				Object idElement = item.getClass().getMethod("getIdElement").invoke(item);
				String idpart = (String) idElement.getClass().getMethod("getIdPart").invoke(idElement);
				if (idpart.equals(id)) {
					return item;
				}
			}
		}
		return null;
	}
}

class Session {
	public final String patientId;
	public final Object planDefinition;
	public final String practitionerId;
	public final String organizationId;
	public final String userType;
	public final String userLanguage;
	public final String userTaskContext;
	public final String setting;
	public final String settingContext;
	public final String prefetchDataKey;
	public Object carePlan;
	public final String encounterId;
	public final Object requestGroup;
	public Object parameters, prefetchData;
	public Object contentEndpoint, terminologyEndpoint, dataEndpoint;
	public Object bundle, prefetchDataData;
	public Object prefetchDataDescription;
	public Boolean useServerData, mergeNestedCarePlans;
	
	public Session(Object planDefinition, Object carePlan, String patientId, String encounterId,
			String practitionerId, String organizationId, String userType, String userLanguage, String userTaskContext,
			String setting, String settingContext, Object requestGroup, Object parameters,
			Object prefetchData, Object contentEndpoint, Object terminologyEndpoint,
			Object dataEndpoint, Object bundle, Boolean useServerData, Boolean mergeNestedCarePlans,
			Object prefetchDataData, Object prefetchDataDescription, String prefetchDataKey) {

		this.patientId = patientId;
		this.planDefinition = planDefinition;
		this.carePlan = carePlan;
		this.encounterId = encounterId;
		this.practitionerId = practitionerId;
		this.organizationId = organizationId;
		this.userType = userType;
		this.userLanguage = userLanguage;
		this.userTaskContext = userTaskContext;
		this.setting = setting;
		this.settingContext = settingContext;
		this.requestGroup = requestGroup;
		this.parameters = parameters;
		this.contentEndpoint = contentEndpoint;
		this.terminologyEndpoint = terminologyEndpoint;
		this.dataEndpoint = dataEndpoint;
		this.bundle = bundle;
		this.useServerData = useServerData;
		this.mergeNestedCarePlans = mergeNestedCarePlans;
		this.prefetchDataData = prefetchDataData;
		this.prefetchDataDescription = prefetchDataDescription;
		this.prefetchData = prefetchData;
		this.prefetchDataKey = prefetchDataKey;
	}
}
