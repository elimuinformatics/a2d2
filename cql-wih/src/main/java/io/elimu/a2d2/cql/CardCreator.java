package io.elimu.a2d2.cql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.elimu.a2d2.cdsresponse.entity.Card;
import io.elimu.a2d2.cdsresponse.entity.LinkCard;
import io.elimu.a2d2.cdsresponse.entity.Suggestion;


public class CardCreator {

	private static final Logger LOG = LoggerFactory.getLogger(CardCreator.class);
	
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

    public static List<Card> _convert(Object requestGroup, ClassLoader cl) throws ParseException, ReflectiveOperationException {
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
            	List<Suggestion> suggestions = new ArrayList<>();
            	boolean isValidCard = false;
            	Class<?> r4actionClass = action.getClass();
            	Object parser = ctx.getClass().getMethod("newJsonParser").invoke(ctx);
            	jsonParserClass.getMethod("setPrettyPrint", boolean.class).invoke(parser, true);
            	Card card = new Card();
                // basic
            	boolean hasTitle = (boolean) r4actionClass.getMethod("hasTitle").invoke(action);
                if (hasTitle) {
                	isValidCard = true;
                	card.setSummary((String) r4actionClass.getMethod("getTitle").invoke(action));
                }
                boolean hasDescription = (boolean) r4actionClass.getMethod("hasDescription").invoke(action);
                if (hasDescription) {
                	isValidCard = true;
                	card.setDetail((String) r4actionClass.getMethod("getDescription").invoke(action));
                }
                boolean hasExtension2 = (boolean) r4actionClass.getMethod("hasExtension").invoke(action);
                if (hasExtension2) {
                	isValidCard = true;
                	List<?> exts = (List<?>) r4actionClass.getMethod("getExtension").invoke(action);
                	for (Object ext : exts) {
	                	Object extUrl = ext.getClass().getMethod("getUrl").invoke(ext);
	                	Object extValue = ext.getClass().getMethod("getValue").invoke(ext);
	                	if (extUrl.toString().endsWith(".override")) {
	                		Class<?> ccClass = cl.loadClass("org.hl7.fhir.r4.model.CodeableConcept");
	                		Class<?> codingClass = cl.loadClass("org.hl7.fhir.r4.model.Coding");
	                		if (extValue != null) {
	                			List<JSONObject> overrideReasons = new ArrayList<>();
	                			if (ccClass.isInstance(extValue)) {
	                				List<?> codings = (List<?>) ccClass.getMethod("getCoding").invoke(extValue);
	                				for (Object coding : codings) {
	                					Map<String, Object> json = new HashMap<>();
	                					json.put("code", codingClass.getMethod("getCode").invoke(coding));
	                					json.put("system", codingClass.getMethod("getSystem").invoke(coding));
	                					json.put("display", codingClass.getMethod("getDisplay").invoke(coding));
	                					overrideReasons.add(new JSONObject(json));
	                				}
	                			} else if (codingClass.isInstance(extValue)) {
	            					Map<String, Object> json = new HashMap<>();
	                				json.put("code", codingClass.getMethod("getCode").invoke(extValue));
	                				json.put("system", codingClass.getMethod("getSystem").invoke(extValue));
	                				json.put("display", codingClass.getMethod("getDisplay").invoke(extValue));
	            					overrideReasons.add(new JSONObject(json));
	            				//TODO we need to add here something fro the case it comes as a String, or as a list of strings?
	                			} else {
	                				LOG.warn("Cannot parse extension of type " + extValue.getClass().getName() + " as overrideReasons element");
	                			}
	                			card.setOverrideReasons(overrideReasons);
	                		}
	                	} else {
		                	card.setIndicator(String.valueOf(extValue));
	                	}
                	}
                }
                // source
                boolean hasDoc = (boolean) r4actionClass.getMethod("hasDocumentation").invoke(action);
                Map<String, Object> source = new HashMap<>();
                if (hasDoc) {
                	//isValidCard = true;
                    // Assuming first related artifact has everything
                	Object documentation = r4actionClass.getMethod("getDocumentationFirstRep").invoke(action);
                    boolean hasDisplay = (boolean) documentation.getClass().getMethod("hasDisplay").invoke(documentation);
                    boolean hasLabel = (boolean) documentation.getClass().getMethod("hasLabel").invoke(documentation);
                    if (hasDisplay) {
                    	Object display = documentation.getClass().getMethod("getDisplay").invoke(documentation);
                    	source.put("label", display);
                    } else if (hasLabel) {
                    	Object label = documentation.getClass().getMethod("getLabel").invoke(documentation);
                    	source.put("label", label);
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
                card.setSource(new JSONObject(source));

                boolean hasSelectionBehavior = (boolean) r4actionClass.getMethod("hasSelectionBehavior").invoke(action);
                if (hasSelectionBehavior) {
                	Object selBehavior = r4actionClass.getMethod("getSelectionBehavior").invoke(action);
                	Object code = selBehavior.getClass().getMethod("toCode").invoke(selBehavior);
                	card.setSelectionBehavior((String) code);
                }
                boolean hasAction2 = (boolean) r4actionClass.getMethod("hasAction").invoke(action);
                if (hasAction2) {
                	List<?> action2List = (List<?>) r4actionClass.getMethod("getAction").invoke(action);
                	for (Object act2 : action2List) {
                		Suggestion sug = new Suggestion();
                		sug.setLabel((String) r4actionClass.getMethod("getPrefix").invoke(act2));
                		Map<String, Object> actionsRet = new HashMap<>();
                		sug.setUuid(UUID.randomUUID().toString());
                		boolean hasId = (boolean) r4actionClass.getMethod("hasId").invoke(act2);
                        if (hasId) {
                        	Object id = r4actionClass.getMethod("getId").invoke(act2);
                        	actionsRet.put("resourceId", id);
                        }
                        boolean hasTextEquivalent = (boolean) r4actionClass.getMethod("hasTextEquivalent").invoke(act2);
                        if (hasTextEquivalent) {
                        	Object description = r4actionClass.getMethod("getTextEquivalent").invoke(act2);
                        	actionsRet.put("description", description);
                        } else {
                        	Object description = r4actionClass.getMethod("getDescription").invoke(act2);
                        	actionsRet.put("description", description);
                        }
                        boolean actionHasType = (boolean) r4actionClass.getMethod("hasType").invoke(act2);
                        if (actionHasType) {
                        	Object type = r4actionClass.getMethod("getType").invoke(act2);
                        	Object codingFirstRep = type.getClass().getMethod("getCodingFirstRep").invoke(type);
                        	Object code = codingFirstRep.getClass().getMethod("getCode").invoke(codingFirstRep);
                        	if("fire-event".equals(code)) {
                        		actionsRet.put("type", "create");
                        	} else {
                        		actionsRet.put("type", "remove".equals(code) ? "delete" : code);
                        	}
                        }
                        boolean hasResource = (boolean) r4actionClass.getMethod("hasResource").invoke(act2);
                        if (hasResource) {
                        	Class<?> ibaseResClass = cl.loadClass("org.hl7.fhir.instance.model.api.IBaseResource");
                        	Object resource = r4actionClass.getMethod("getResource").invoke(act2);
                        	Object resourceTarget = resource.getClass().getMethod("getResource").invoke(resource);
                        	if (resourceTarget != null) {
    	                    	String json = (String) parser.getClass().getMethod("encodeResourceToString", ibaseResClass).invoke(parser, resourceTarget);
    	                    	JSONObject obj = (JSONObject) new JSONParser().parse(json);
    	                    	actionsRet.put("resource", obj);
                        	}
                        }
                        sug.setActions(Arrays.asList(new JSONObject(actionsRet)));
                        suggestions.add(sug);
                	}
                }
                card.setSuggestions(suggestions);
                if (!links.isEmpty()) {
                	card.setLinks(links);
                }
                if (isValidCard) {
                	card.setUuid(UUID.randomUUID().toString());
                	cards.add(card);
                }
            }
        }
        return cards;
    }
}
