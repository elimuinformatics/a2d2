package io.elimu.genericapi.scrubbers;

import com.google.json.JsonSanitizer;

public class JsonInputScrubber extends AbstractInputScrubber implements InputScrubber {

	@Override
	protected String doScrub(String str) {
		return JsonSanitizer.sanitize(str);
	}

	@Override
	protected String getContentName() {
		return "json";
	}
}
