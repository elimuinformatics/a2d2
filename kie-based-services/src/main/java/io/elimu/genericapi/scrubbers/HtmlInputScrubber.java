package io.elimu.genericapi.scrubbers;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

public class HtmlInputScrubber extends AbstractInputScrubber implements InputScrubber {

	@Override
	protected String doScrub(String str) {
		return Jsoup.clean(str, Whitelist.basic());
	}

	@Override
	protected String getContentName() {
		return "html";
	}
}
