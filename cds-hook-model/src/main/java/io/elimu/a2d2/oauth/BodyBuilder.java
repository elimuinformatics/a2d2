package io.elimu.a2d2.oauth;

import org.apache.commons.lang3.StringUtils;

public class BodyBuilder {

	StringBuilder sb = new StringBuilder();
	
	public BodyBuilder addToBody(String key, String value) {
		if (StringUtils.isEmpty(value)) {
			return this;
		}
		if (sb.length() > 0) {
			sb.append('&');
		}
		sb.append(key).append("=").append(value);
		return this;
	}

	public String build() {
		return sb.toString();
	}

}
