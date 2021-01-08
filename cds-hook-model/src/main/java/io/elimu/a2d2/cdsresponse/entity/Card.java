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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Card implements Serializable {

	private static final long serialVersionUID = 1L;

	private String summary;
	private String detail;
	private String indicator;
	private List<LinkCard> links;
	private List<Suggestion> suggestions;
	private Object source;
	private Object decisions;

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getIndicator() {
		return indicator;
	}

	public void setIndicator(String indicator) {
		this.indicator = indicator;
	}

	public List<LinkCard> getLinks() {
		return links;
	}

	public void setLinks(List<LinkCard> links) {
		this.links = links;
	}

	public void addLink(LinkCard link) {
		if (this.links == null) {
			this.links = new ArrayList<>();
		}
		this.links.add(link);
	}

	public List<Suggestion> getSuggestions() {
		return suggestions;
	}

	public void setSuggestions(List<Suggestion> suggestions) {
		this.suggestions = suggestions;
	}

	public void addSuggestion(Suggestion suggestion) {
		if (this.suggestions == null) {
			this.suggestions = new ArrayList<>();
		}
		this.suggestions.add(suggestion);
	}

	public Object getSource() {
		return source;
	}

	public void setSource(Object source) {
		this.source = source;
	}

	public Object getDecisions() {
		return decisions;
	}

	public void setDecisions(Object decisions) {
		this.decisions = decisions;
	}
}
