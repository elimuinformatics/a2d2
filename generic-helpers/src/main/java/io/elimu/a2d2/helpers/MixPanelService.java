package io.elimu.a2d2.helpers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.drools.core.spi.KnowledgeHelper;

import io.elimu.a2d2.exception.WorkItemHandlerException;
import io.elimu.a2d2.genericmodel.ServiceRequest;

public class MixPanelService {
	private static final MixPanelService INSTANCE = new MixPanelService();

	private ThreadLocal<String> distinctId = ThreadLocal.withInitial(() -> "");
	
	private MixPanelService() {
	}

	public static MixPanelService getInstance() {
		return INSTANCE;
	}

	public void identify(String distinctId) {
		this.distinctId.set(distinctId);
	}
	
	public void identify(ServiceRequest request) {
		//TODO this still needs to be decided. There are open discussions on how to extract the discinctId
		//One proposal is to send it on the HTTP body, but it would disrupt service structure and openness of the omnibus-api service model
		//Other alternatives would be as HTTP headers
		//Another option could be to extract it from the HTTP OAuth as well, if possible
	}
	
	public void track(KnowledgeHelper drools, String eventName, Map<String, Object> action) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("eventDescription", eventName);
		String distinctId = this.distinctId.get();
		if ("".equals(distinctId) || distinctId == null) {
			throw new WorkItemHandlerException("Call MixPanel.identify(...) first");
		}
		parameters.put("distinctId", distinctId);
		if (action != null) {
			for (String key : action.keySet()) {
				parameters.put("mixpanel_" + key, action.get(key));
			}
		}
		WIHInvoker.invoke(drools, "MixPanelInvocation", parameters);
	}

	public String hashPatient(String value) throws NoSuchAlgorithmException {
		String salt = System.getenv("SALT"); // this needs to be added to the task definition
		if (salt == null) {
			salt = "";
		}
		byte[] hashBytes = MessageDigest.getInstance("SHA-256").digest((value + salt).getBytes(StandardCharsets.UTF_8));
		return bytesToHex(hashBytes);
	}
	
	 private String bytesToHex(byte[] hash) {
		 StringBuilder hexString = new StringBuilder(2 * hash.length);
		 for (byte h : hash) {
			 String hex = Integer.toHexString(0xff & h);
			 if (hex.length() == 1)
				 hexString.append('0');
			 hexString.append(hex);
		 }
		 return hexString.toString();
	 }
}
