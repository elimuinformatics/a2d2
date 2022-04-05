package io.elimu.genericapi.service;

import java.io.IOException;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mixpanel.mixpanelapi.ClientDelivery;
import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;

public class MixPanelStack implements Runnable {

	private static final MixPanelStack INSTANCE = new MixPanelStack();
	private static final Logger LOG = LoggerFactory.getLogger(MixPanelStack.class);
	
	private final Stack<JSONObject> events = new Stack<>();

	private String token;

	private MixPanelStack() {
		this.token = System.getProperty("mixpanel.app.token");
		if (this.token == null || "".equals(this.token.trim())) {
			LOG.warn("Implementation of mixpanel requires a mixpanel.app.token configured in the JVM properties. It will be disabled for now");
			Thread thread = new Thread() {
				@Override
				public void run() {
					while(true) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							return;
						}
						events.clear();
					}
				}
			};
			thread.start();
		} else {
			Thread thread = Executors.defaultThreadFactory().newThread(this);
			thread.start();
		}
	}
	
	public static MixPanelStack getInstance() {
		return INSTANCE;
	}

	public void add(JSONObject event) {
		events.add(event);
	}

	@Override
	public void run() {
		while (true) {
			if (events.empty()) {
				try {
					Thread.sleep(5000L);
				} catch (InterruptedException e) {
					return;
				}
				continue;
			}
			ClientDelivery delivery = new ClientDelivery();
			MessageBuilder messageBuilder = new MessageBuilder(token);
			int count = 0;
			while (!events.empty()) {
				JSONObject evt = events.pop();
				String evtName = evt.getString("eventDescription");
				if (evtName == null || "".equals(evtName.trim())) {
					evtName = "Omnibus General Event";
				}
				String distinctId = evt.getString("distinctId");
				if (distinctId == null || "".equals(distinctId.trim())) {
					distinctId = UUID.randomUUID().toString();
				}
				JSONObject omnibusEvent = messageBuilder.event(distinctId, evtName, evt);
				delivery.addMessage(omnibusEvent);
				count++;
			}
			if (count > 0) {
				MixpanelAPI mixpanel = new MixpanelAPI();
				try {
					mixpanel.deliver(delivery);
					LOG.debug("Sending " + count + " events");
				} catch (IOException e) {
					LOG.error("Could not send events to mixpanel", e);
				}
			}
		}
	}

	public void registerTime(long timeInMillis, String serviceId) {
		try {
			JSONObject evt = new JSONObject();
			evt.put("eventDescription", "Omnibus Service " + serviceId + " invoked");
			evt.put("eventType", ("Service Executed"));
			evt.put("serviceName", serviceId);
			evt.put("executionTime", timeInMillis);
			add(evt);
		} catch (JSONException e) {
			LOG.warn("problem creating MixPanel event data", e);
		}
	}

	public int size() {
		return events.size();
	}
}
