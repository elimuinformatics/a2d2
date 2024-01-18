// Copyright 2018-2020 Elimu Informatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.elimu.a2d2.cdsresponse.entity;

import java.util.Map;

import org.json.simple.JSONObject;

public class CardBuilder {

	public static CardBuilder create() {
		return new CardBuilder();
	}

	private Card card;

	private CardBuilder() {
		this.card = new Card();
	}

	public CardBuilder withSummary(String summary) {
		card.setSummary(summary);
		return this;
	}

	public CardBuilder withDetail(String detail) {
		card.setDetail(detail);
		return this;
	}

	public CardBuilder withIndicator(String indicator) {
		card.setIndicator(indicator);
		return this;
	}

	public CardBuilder withSource(String url, String label) {
		SourceCard sc = new SourceCard();
		sc.setUrl(url);;
		sc.setLabel(label);
		card.setSource(sc);
		return this;
	}

	public CardBuilder withLink(String label, String url, String type) {
		LinkCard lc = new LinkCard();
		lc.setLabel(label);
		lc.setUrl(url);
		lc.setType(type);
		card.addLink(lc);
		return this;
	}

	public CardBuilder withSuggestion(Suggestion s) {
		card.addSuggestion(s);
		return this;
	}

	public CardBuilder withExtension(String name, Map<String,Object> extension) {
		card.addExtension(name, new JSONObject(extension));
		return this;
	}
	
	public Card build() {
		return card;
	}
}
