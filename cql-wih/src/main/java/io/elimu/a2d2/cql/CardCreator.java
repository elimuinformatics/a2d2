package io.elimu.a2d2.cql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import io.elimu.a2d2.cdsresponse.entity.Card;
import io.elimu.a2d2.cdsresponse.entity.LinkCard;
import io.elimu.a2d2.cdsresponse.entity.Suggestion;


public class CardCreator {

	public static List<Card> convert(Object carePlan, ClassLoader cl) throws ParseException, ReflectiveOperationException {
        List<Card> cards = new ArrayList<>();
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

	@SuppressWarnings("unchecked")
    private static List<Card> _convert(Object requestGroup, ClassLoader cl) throws ParseException, ReflectiveOperationException {
    	List<Card> cards = new ArrayList<>();
    	// links
    	List<LinkCard> links = new ArrayList<>();
    	boolean hasExtension = (boolean) requestGroup.getClass().getMethod("hasExtension").invoke(requestGroup);
        if (hasExtension) {
        	List<?> extensions = (List<?>) requestGroup.getClass().getMethod("getExtension").invoke(requestGroup);
        	Class<?> attachmentClass = cl.loadClass("org.hl7.fhir.r4.model.Attachment");
            for (Object extension : extensions) {
            	LinkCard link = new LinkCard();
            	Object extValue = extension.getClass().getMethod("getValue").invoke(extension);
                if (attachmentClass.isInstance(extValue)) {
                	boolean hasUrl = (boolean) extValue.getClass().getMethod("hasUrl").invoke(extValue);
                    if (hasUrl) {
                    	link.setUrl((String) extValue.getClass().getMethod("getUrl").invoke(extValue));
                    }
                    boolean hasTitle = (boolean) extValue.getClass().getMethod("hasTitle").invoke(extValue);
                    if (hasTitle) {
                    	link.setTitle((String) extValue.getClass().getMethod("getTitle").invoke(extValue));
                    }
                    boolean hasExtension2 = (boolean) extValue.getClass().getMethod("hasExtension").invoke(extValue);
                    if (hasExtension2) {
                    	Object extIter = extValue.getClass().getMethod("getExtensionFirstRep").invoke(extValue);
                    	Object type = extIter.getClass().getMethod("getValue").invoke(extIter);
                    	Object primType = type.getClass().getMethod("primitiveValue").invoke(type);
                    	link.setType(String.valueOf(primType));
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
        	Object r4En = enumClass.getField("R4").get(null);
        	Object ctx = ctxClass.getMethod("forCached", enumClass).invoke(null, r4En);
            for (Object action : actionsList) {
            	Class<?> r4actionClass = action.getClass();
            	Object parser = ctx.getClass().getMethod("newJsonParser").invoke(ctx);
            	jsonParserClass.getMethod("setPrettyPrint", boolean.class).invoke(parser, true);
            	Card card = new Card();
                // basic
            	boolean hasTitle = (boolean) r4actionClass.getMethod("hasTitle").invoke(action);
                if (hasTitle) {
                	card.setSummary((String) r4actionClass.getMethod("getTitle").invoke(action));
                }
                boolean hasDescription = (boolean) r4actionClass.getMethod("hasDescription").invoke(action);
                if (hasDescription) {
                	card.setDetail((String) r4actionClass.getMethod("getDescription").invoke(action));
                }
                boolean hasExtension2 = (boolean) r4actionClass.getMethod("hasExtension").invoke(action);
                if (hasExtension2) {
                	Object ext = r4actionClass.getMethod("getExtensionFirstRep").invoke(action);
                	Object extValue = ext.getClass().getMethod("getValue").invoke(ext);
                	card.setIndicator(String.valueOf(extValue));
                }

                // source
                boolean hasDoc = (boolean) r4actionClass.getMethod("hasDocumentation").invoke(action);
                JSONObject source = new JSONObject();
                card.setSource(source);
                if (hasDoc) {
                    // Assuming first related artifact has everything
                	Object documentation = r4actionClass.getMethod("getDocumentationFirstRep").invoke(action);
                    boolean hasDisplay = (boolean) documentation.getClass().getMethod("hasDisplay").invoke(documentation);
                    if (hasDisplay) {
                    	Object display = documentation.getClass().getMethod("getDisplay").invoke(documentation);
                    	source.put("label", display);
                    }
                    boolean hasUrl = (boolean) documentation.getClass().getMethod("hasUrl").invoke(documentation);
                    if (hasUrl) {
                    	Object url = documentation.getClass().getMethod("getUrl").invoke(documentation);
                    	source.put("url", url);
                    }
                    boolean hasDocument = (boolean) documentation.getClass().getMethod("hasDocument").invoke(documentation);
                    if (hasDocument) {
                    	Object doc = documentation.getClass().getMethod("getDocument").invoke(documentation);
                    	boolean docHasUrl = (boolean) doc.getClass().getMethod("hasUrl").invoke(doc);
                    	if (docHasUrl) {
                    		Object docUrl = doc.getClass().getMethod("getUrl").invoke(doc);
                    		source.put("icon", docUrl);
                    	}
                    }
                }

                boolean hasSelectionBehavior = (boolean) r4actionClass.getMethod("hasSelectionBehavior").invoke(action);
                if (hasSelectionBehavior) {
                	Object selBehavior = r4actionClass.getMethod("getSelectionBehavior").invoke(action);
                	Object code = selBehavior.getClass().getMethod("toCode").invoke(selBehavior);
                	card.setSelectionBehavior((String) code);
                }

                // suggestions
                List<Suggestion> suggestions = new ArrayList<>();
                boolean hasPrefix = (boolean) r4actionClass.getMethod("hasPrefix").invoke(action);
                if (hasPrefix) {
                	Suggestion suggestion = new Suggestion();
                	JSONObject actionsRet = new JSONObject();
                	suggestion.setUuid(UUID.randomUUID().toString());
                	Object prefix = r4actionClass.getMethod("getPrefix").invoke(action);
                	suggestion.setLabel((String) prefix);
                    boolean hasDescription2 = (boolean) r4actionClass.getMethod("hasDescription").invoke(action);
                    if (hasDescription2) {
                    	Object description = r4actionClass.getMethod("getDescription").invoke(action);
                    	actionsRet.put("description", description);
                    }
                    boolean actionHasType = (boolean) r4actionClass.getMethod("hasType").invoke(action);
                    if (actionHasType) {
                    	Object type = r4actionClass.getMethod("getType").invoke(action);
                    	Object codingFirstRep = type.getClass().getMethod("getCodingFirstRep").invoke(type);
                    	Object code = codingFirstRep.getClass().getMethod("getCode").invoke(codingFirstRep);
                    	if("fire-event".equals(code)) {
                    		actionsRet.put("type", "create");
                    	} else {
                    		Object actionTypeCode = code.equals("remove") ? "delete" : code;
                    		actionsRet.put("type", actionTypeCode);
                    	}
                    }
                    boolean hasResource = (boolean) r4actionClass.getMethod("hasResource").invoke(action);
                    if (hasResource) {
                    	Class<?> ibaseResClass = cl.loadClass("org.hl7.fhir.instance.model.api.IBaseResource");
                    	Object resource = r4actionClass.getMethod("getResource").invoke(action);
                    	Object resourceTarget = resource.getClass().getMethod("getResource").invoke(resource);
                    	if (resourceTarget != null) {
	                    	String json = (String) parser.getClass().getMethod("encodeResourceToString", ibaseResClass).invoke(parser, resourceTarget);
	                    	JSONObject obj = (JSONObject) new JSONParser().parse(json);
	                    	actionsRet.put("resource", obj);
                    	}
                    }
                    suggestions.add(suggestion);
                    suggestion.setActions(Arrays.asList(actionsRet));
                }
                card.setSuggestions(suggestions);
                if (!links.isEmpty()) {
                	card.setLinks(links);
                }
                cards.add(card);
            }
        }

        return cards;
    }
}
