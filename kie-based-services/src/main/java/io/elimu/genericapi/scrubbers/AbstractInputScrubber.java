package io.elimu.genericapi.scrubbers;

import io.elimu.a2d2.genericmodel.ServiceRequest;

public abstract class AbstractInputScrubber implements InputScrubber {

	@Override
	public boolean accept(ServiceRequest request) {
		if (request == null) {
			return false;
		}
		for (String key : request.getHeaders().keySet()) {
			if ("content-type".equalsIgnoreCase(key)) {
				if (request.getHeader(key) == null) {
					return false;
				}
				return request.getHeader(key).toLowerCase().endsWith("/" + getContentName());
			}
		}
		return false;
	}

	@Override
	public void scrub(ServiceRequest request) {
		if (request.getBody() != null) {
			request.setBody(doScrub(request.getBody()));
		}
	}

	protected abstract String doScrub(String str);

	protected abstract String getContentName();

}
