package io.elimu.a2d2.corewih;

import java.io.IOException;
import java.util.UUID;

import org.json.JSONObject;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;

public class MixPanelWorkItemHandler implements WorkItemHandler {

	private static final Logger LOG = LoggerFactory.getLogger(MixPanelWorkItemHandler.class);
	public static final String MIXPANEL_PREFIX = "mixpanel_";
	
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String appToken = System.getProperty("mixpanel.app.token");
		if (appToken == null) {
			LOG.info("No mixpannel app token provided in the system config. Returning without sending event");
			manager.completeWorkItem(workItem.getId(), workItem.getResults());
			return;
		}
		String evtName = (String) workItem.getParameter("eventDescription");
		if (evtName == null || "".equals(evtName.trim())) {
			evtName = "Omnibus Generic Event";
		}
		JSONObject evt = new JSONObject();
		for (String key : workItem.getParameters().keySet()) {
			if (key.startsWith(MIXPANEL_PREFIX)) {
				evt.put(key.replace(MIXPANEL_PREFIX, ""), workItem.getParameter(key));
			}
		}
		MessageBuilder messageBuilder = new MessageBuilder(appToken);
		JSONObject omnibusEvent = messageBuilder.event(UUID.randomUUID().toString(), evtName, evt);
		MixpanelAPI mixpanel = new MixpanelAPI();
		try {
			mixpanel.sendMessage(omnibusEvent);
			LOG.debug("Sending single mixpanel event");
			manager.completeWorkItem(workItem.getId(), workItem.getResults());
		} catch (IOException e) {
			LOG.error("Could not send event to mixpanel", e);
			manager.abortWorkItem(workItem.getId());
		}
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// do nothing
	}
}
