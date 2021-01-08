package io.elimu.genericapi.scrubbers;

import io.elimu.a2d2.genericmodel.ServiceRequest;

public interface InputScrubber {

	boolean accept(ServiceRequest request);
	
	void scrub(ServiceRequest request);
}
