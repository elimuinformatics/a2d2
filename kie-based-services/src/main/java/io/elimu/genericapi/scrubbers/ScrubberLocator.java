package io.elimu.genericapi.scrubbers;

import java.util.ArrayList;
import java.util.List;

import io.elimu.a2d2.genericmodel.ServiceRequest;

public class ScrubberLocator {

	private static final ScrubberLocator INSTANCE = new ScrubberLocator();
	
	public static ScrubberLocator getInstance() {
		return INSTANCE;
	}

	private final List<InputScrubber> available = new ArrayList<>();
	
	private ScrubberLocator() {
		available.add(new JsonInputScrubber());
		available.add(new HtmlInputScrubber());
		available.add(new DoNothingInputScrubber());
	}
	
	public InputScrubber getScrubber(ServiceRequest request) {
		for (InputScrubber scrubber : available) {
			if (scrubber.accept(request)) {
				return scrubber;
			}
		}
		return available.get(available.size() - 1); // last one is always default
	}
}
