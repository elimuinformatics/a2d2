package io.elimu.a2d2.helpers;

import java.util.HashMap;
import java.util.Map;

import org.drools.core.spi.KnowledgeHelper;

public class MixPanelService {
	private static final MixPanelService INSTANCE = new MixPanelService();

	private MixPanelService() {
	}

	public static MixPanelService getInstance() {
		return INSTANCE;
	}

	public void track(KnowledgeHelper drools, String eventName, Map<String, Object> action) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("eventDescription", eventName);
		if (action != null) {
			for (String key : action.keySet()) {
				parameters.put("mixpanel_" + key, action.get(key));
			}
		}
		WIHInvoker.invoke(drools, "MixPanelInvocation", parameters);
	}

	public String hashPatient(String value) {
		return value;//TODO once java-commons has the hash value, it has to be used from here
	}
}
