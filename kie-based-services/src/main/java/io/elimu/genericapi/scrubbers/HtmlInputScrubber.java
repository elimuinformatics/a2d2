package io.elimu.genericapi.scrubbers;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class HtmlInputScrubber extends AbstractInputScrubber implements InputScrubber {

	@Override
	protected String doScrub(String str) {
		return Jsoup.clean(str, Safelist.basic());
	}

	@Override
	protected String getContentName() {
		return "html";
	}
}
