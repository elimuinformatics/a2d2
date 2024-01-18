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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

public class Card implements Serializable {

	private static final long serialVersionUID = 1L;

	private String uuid;
	private String summary;
	private String detail;
	private String indicator;
	private List<LinkCard> links;
	private List<Suggestion> suggestions;
	private Map<String, JSONObject> extension;
	private Object source;
	private Object decisions;
	private String selectionBehavior;
	private List<JSONObject> overrideReasons;

	public String getUuid() {
		return uuid;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
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
	
	public void setSelectionBehavior(String selectionBehavior) {
		this.selectionBehavior = selectionBehavior;
	}
	
	public String getSelectionBehavior() {
		return selectionBehavior;
	}
	
	public List<JSONObject> getOverrideReasons() {
		return overrideReasons;
	}
	
	public void setOverrideReasons(List<JSONObject> overrideReasons) {
		this.overrideReasons = overrideReasons;
	}
	
	public void setExtension(Map<String, JSONObject> extension) {
		this.extension = extension;
	}
	
	public Map<String, JSONObject> getExtension() {
		return extension;
	}
	
	public void addExtension(String key, JSONObject extension) {
		if (this.extension == null) {
			this.extension = new HashMap<>();
		}
		this.extension.put(key, extension);
	}
}
