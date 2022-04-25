package io.elimu.a2d2.corewih;

import java.io.IOException;

import org.json.JSONObject;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;

import io.elimu.a2d2.genericmodel.ServiceRequest;

public class MixPanelWorkItemHandler implements WorkItemHandler {

	private static final Logger LOG = LoggerFactory.getLogger(MixPanelWorkItemHandler.class);
	public static final String MIXPANEL_PREFIX = "mixpanel_";
	public static final String MIXPANEL_REQUEST_PREFIX = "mixpanel-";
	
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		String appToken = System.getProperty("mixpanel.app.token");
		String mixpanelUrl = System.getProperty("mixpanel.app.url", "https://api.mixpanel.com");
		String distinctId = (String) workItem.getParameter("distinctId");
		ServiceRequest request = (ServiceRequest) workItem.getParameter("serviceRequest");
		if (appToken == null) {
			LOG.info("No mixpannel app token provided. Returning without sending event");
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
		if (request != null) {
			for (String headerName : request.getHeaders().keySet()) {
				if (headerName.startsWith(MIXPANEL_REQUEST_PREFIX)) {
					evt.put(prettify(headerName), request.getHeader(headerName));
				}
			}
			if (request.getHeader(MIXPANEL_REQUEST_PREFIX + "distinct-id") != null) {
				distinctId = request.getHeader(MIXPANEL_REQUEST_PREFIX + "distinct-id");
			}
		}
		evt.put("distinct_id", distinctId);
		MessageBuilder messageBuilder = new MessageBuilder(appToken);
		JSONObject omnibusEvent = messageBuilder.event(distinctId, evtName, evt);
		MixpanelAPI mixpanel = new MixpanelAPI(mixpanelUrl + "/track", mixpanelUrl + "/engage");
		try {
			mixpanel.sendMessage(omnibusEvent);
			LOG.debug("Sending single mixpanel event");
			manager.completeWorkItem(workItem.getId(), workItem.getResults());
		} catch (IOException e) {
			LOG.error("Could not send event to mixpanel", e);
			manager.abortWorkItem(workItem.getId());
		}
	}

	private String prettify(String headerName) {
		String startPoint = headerName.replace(MIXPANEL_REQUEST_PREFIX, "");
		startPoint = startPoint.replaceAll("-", " ");
		String[] split = startPoint.split(" ");
		StringBuilder retval = new StringBuilder();
		for (int index = 0; index < split.length; index++) {
			String part = split[index];
			if (part.length() < 1) {
				continue;
			}
			if (index > 0) {
				retval.append(' ');
			}
			retval.append(part.toUpperCase().charAt(0)).append(part.substring(1));
		}
		return retval.toString();
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// do nothing
	}
}
