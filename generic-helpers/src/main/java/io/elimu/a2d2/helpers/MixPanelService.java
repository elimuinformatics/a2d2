package io.elimu.a2d2.helpers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
