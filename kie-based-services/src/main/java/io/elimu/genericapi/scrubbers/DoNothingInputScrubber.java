package io.elimu.genericapi.scrubbers;

import io.elimu.a2d2.genericmodel.ServiceRequest;

public class DoNothingInputScrubber implements InputScrubber {

	@Override
	public boolean accept(ServiceRequest request) {
		return true;
	}

	@Override
	public void scrub(ServiceRequest request) {
		//do nothing
	}

}
