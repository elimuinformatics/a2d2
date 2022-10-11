package io.elimu.a2d2.cql;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class CardCreator {

	public static List<Object> convert(Object carePlan, ClassLoader cl) throws ReflectiveOperationException {
        List<Object> cards = new ArrayList<>();
        List<?> activities = (List<?>) carePlan.getClass().getMethod("getActivity").invoke(carePlan);
        Class<?> reqGroupClass = cl.loadClass("org.hl7.fhir.r4.model.RequestGroup");
        for (Object activity : activities) {
        	Object refTarget = activity.getClass().getMethod("getReferenceTarget").invoke(activity);
            if (refTarget != null && reqGroupClass.isInstance(refTarget)) {
                cards = _convert(refTarget, cl);
            }
        }

        return cards;
    }

    private static List<Object> _convert(Object requestGroup, ClassLoader cl) throws ReflectiveOperationException {
    	List<Object> cards = new ArrayList<>();
    	// links
    	List<Object> links = new ArrayList<>();
    	boolean hasExtension = (boolean) requestGroup.getClass().getMethod("hasExtension").invoke(requestGroup);
        if (hasExtension) {
        	List<?> extensions = (List<?>) requestGroup.getClass().getMethod("getExtension").invoke(requestGroup);
        	Class<?> linkClass = cl.loadClass("org.opencds.cqf.cds.response.CdsCard.Links");
        	Class<?> attachmentClass = cl.loadClass("org.hl7.fhir.r4.model.Attachment");
            for (Object extension : extensions) {
            	Object link = linkClass.getConstructor().newInstance();
            	Object extValue = extension.getClass().getMethod("getValue").invoke(extension);
                if (attachmentClass.isInstance(extValue)) {
                	boolean hasUrl = (boolean) extValue.getClass().getMethod("hasUrl").invoke(extValue);
                    if (hasUrl) {
                    	link.getClass().getMethod("setUrl", String.class).invoke(link, extValue.getClass().getMethod("getUrl").invoke(extValue));
                    }
                    boolean hasTitle = (boolean) extValue.getClass().getMethod("hasTitle").invoke(extValue);
                    if (hasTitle) {
                    	link.getClass().getMethod("setTitle", String.class).invoke(link, extValue.getClass().getMethod("getTitle").invoke(extValue));
                    }
                    boolean hasExtension2 = (boolean) extValue.getClass().getMethod("hasExtension").invoke(extValue);
                    if (hasExtension2) {
                    	Object extIter = extValue.getClass().getMethod("getExtensionFirstRep").invoke(extValue);
                    	Object type = extIter.getClass().getMethod("getValue").invoke(extIter);
                    	Object primType = type.getClass().getMethod("primitiveValue").invoke(type);
                    	link.getClass().getMethod("setType", String.class).invoke(link, primType);
                    }
                }

                else {
                    throw new RuntimeException("Invalid link extension type: " + extValue.getClass().getMethod("fhirType").invoke(extValue));
                }
                links.add(link);
            }
        }

    	boolean hasAction = (boolean) requestGroup.getClass().getMethod("hasAction").invoke(requestGroup);
        if (hasAction) {
        	List<?> actionsList = (List<?>) requestGroup.getClass().getMethod("getAction").invoke(requestGroup);
        	Class<?> jsonParserClass = cl.loadClass("ca.uhn.fhir.parser.IParser");
        	Class<?> ctxClass = cl.loadClass("ca.uhn.fhir.context.FhirContext");
        	Class<?> enumClass = cl.loadClass("ca.uhn.fhir.context.FhirVersionEnum");
        	Class<?> cardClass = cl.loadClass("org.opencds.cqf.cds.response.CdsCard");
        	Object r4En = enumClass.getField("R4").get(null);
        	Object ctx = ctxClass.getMethod("forCached", enumClass).invoke(null, r4En);
            for (Object action : actionsList) {
            	Class<?> r4actionClass = action.getClass();
            	Object parser = ctx.getClass().getMethod("newJsonParser").invoke(ctx);
            	jsonParserClass.getMethod("setPrettyPrint", boolean.class).invoke(parser, true);
            	Object card = cardClass.getConstructor(jsonParserClass).newInstance(parser);
                // basic
            	boolean hasTitle = (boolean) r4actionClass.getMethod("hasTitle").invoke(action);
                if (hasTitle) {
                	cardClass.getMethod("setSummary", String.class).invoke(card, r4actionClass.getMethod("getTitle").invoke(action));
                }
                boolean hasDescription = (boolean) r4actionClass.getMethod("hasDescription").invoke(action);
                if (hasDescription) {
                	cardClass.getMethod("setDetail", String.class).invoke(card, r4actionClass.getMethod("getDescription").invoke(action));
                }
                boolean hasExtension2 = (boolean) r4actionClass.getMethod("hasExtension").invoke(action);
                if (hasExtension2) {
                	Object ext = r4actionClass.getMethod("getExtensionFirstRep").invoke(action);
                	Object extValue = ext.getClass().getMethod("getValue").invoke(ext);
                	cardClass.getMethod("setIndicator", String.class).invoke(card, String.valueOf(extValue));
                }

                // source
                boolean hasDoc = (boolean) r4actionClass.getMethod("hasDocumentation").invoke(action);
                if (hasDoc) {
                    // Assuming first related artifact has everything
                	Object documentation = r4actionClass.getMethod("getDocumentationFirstRep").invoke(action);
                	Class<?> sourceClass = cl.loadClass("org.opencds.cqf.cds.response.CdsCard.Source");
                    Object source = sourceClass.getConstructor().newInstance();
                    boolean hasDisplay = (boolean) documentation.getClass().getMethod("hasDisplay").invoke(documentation);
                    if (hasDisplay) {
                    	Object display = documentation.getClass().getMethod("getDisplay").invoke(documentation);
                    	sourceClass.getMethod("setLabel", String.class).invoke(source, display);
                    }
                    boolean hasUrl = (boolean) documentation.getClass().getMethod("hasUrl").invoke(documentation);
                    if (hasUrl) {
                    	Object url = documentation.getClass().getMethod("getUrl").invoke(documentation);
                    	sourceClass.getMethod("setUrl", String.class).invoke(source, url);
                    }
                    boolean hasDocument = (boolean) documentation.getClass().getMethod("hasDocument").invoke(documentation);
                    if (hasDocument) {
                    	Object doc = documentation.getClass().getMethod("getDocument").invoke(documentation);
                    	boolean docHasUrl = (boolean) doc.getClass().getMethod("hasUrl").invoke(doc);
                    	if (docHasUrl) {
                    		Object docUrl = doc.getClass().getMethod("getUrl").invoke(doc);
                    		sourceClass.getMethod("setIcon", String.class).invoke(source, docUrl);
                    	}
                    }
                    cardClass.getMethod("setSource", sourceClass).invoke(card, source);
                }

                boolean hasSelectionBehavior = (boolean) r4actionClass.getMethod("hasSelectionBehavior").invoke(action);
                if (hasSelectionBehavior) {
                	Object selBehavior = r4actionClass.getMethod("getSelectionBehavior").invoke(action);
                	Object code = selBehavior.getClass().getMethod("toCode").invoke(selBehavior);
                	cardClass.getMethod("setSelectionBehavior", String.class).invoke(card, code);
                }

                // suggestions
                boolean hasSuggestions = false;
                Class<?> suggClass = cl.loadClass("org.opencds.cqf.cds.response.CdsCard$Suggestions");
                Class<?> actionsClass = cl.loadClass("org.opencds.cqf.cds.response.CdsCard$Suggestions$Action");
                Object suggestions = suggClass.getConstructor().newInstance();
                suggClass.getMethod("setUuid", String.class).invoke(suggestions, UUID.randomUUID().toString());
                Object actions = actionsClass.getConstructor().newInstance();
                boolean hasPrefix = (boolean) r4actionClass.getMethod("hasPrefix").invoke(action);
                if (hasPrefix) {
                	Object prefix = r4actionClass.getMethod("getPrefix").invoke(action);
                	suggClass.getMethod("setLabel", String.class).invoke(suggestions, prefix);
                    hasSuggestions = true;
                    boolean hasDescription2 = (boolean) r4actionClass.getMethod("hasDescription").invoke(action);
                    if (hasDescription2) {
                    	Object description = r4actionClass.getMethod("getDescription").invoke(action);
                    	actionsClass.getMethod("setDescription", String.class).invoke(actions, description);
                    }
                    boolean actionHasType = (boolean) r4actionClass.getMethod("hasType").invoke(action);
                    Class<?> actionTypeClass = cl.loadClass("org.opencds.cqf.cds.response.CdsCard$Suggestions$Action$ActionType");
                    if (actionHasType) {
                    	Object type = r4actionClass.getMethod("getType").invoke(action);
                    	Object codingFirstRep = type.getClass().getMethod("getCodingFirstRep").invoke(type);
                    	Object code = codingFirstRep.getClass().getMethod("getCode").invoke(codingFirstRep);
                    	if(!"fire-event".equals(code)) {
                    		Object actionTypeCode = actionTypeClass.getMethod("valueOf", String.class).invoke(null, (code.equals("remove") ? "delete" : code));
                    		actionsClass.getMethod("setType", actionTypeClass).invoke(actions, actionTypeCode);
                    	}
                    }
                    boolean hasResource = (boolean) r4actionClass.getMethod("hasResource").invoke(action);
                    if (hasResource) {
                    	Object actionType = actionTypeClass.getField("create").get(null);
                    	actionsClass.getMethod("setType", actionTypeClass).invoke(actions, actionType);
                    	Class<?> ibaseResClass = cl.loadClass("org.hl7.fhir.instance.model.api.IBaseResource");
                    	Object resourceTarget = r4actionClass.getMethod("getResourceTarget").invoke(action);
                    	actionsClass.getMethod("setResource", ibaseResClass).invoke(actions, resourceTarget);
                    }
                }
                if (hasSuggestions) {
                	suggClass.getMethod("addAction", actionsClass).invoke(suggestions, actions);
                	cardClass.getMethod("addSuggestion", suggClass).invoke(card, suggestions);
                }
                if (!links.isEmpty()) {
                	cardClass.getMethod("setLinks", List.class).invoke(card,  links);
                }
                cards.add(card);
            }
        }

        return cards;
    }
}
