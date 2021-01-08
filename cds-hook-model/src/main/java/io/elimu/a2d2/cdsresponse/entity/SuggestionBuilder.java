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

import java.util.HashMap;
import java.util.Map;

public class SuggestionBuilder {

	public static SuggestionBuilder create() {
		return new SuggestionBuilder();
	}

	private Suggestion suggestion;

	private SuggestionBuilder() {
		this.suggestion = new Suggestion();
	}

	public SuggestionBuilder withLabel(String label) {
		suggestion.setLabel(label);
		return this;
	}

	public SuggestionBuilder withUuid(String uuid) {
		suggestion.setUuid(uuid);
		return this;
	}

	public SuggestionBuilder withAction(String type, String description, Object resource) {
		Map<String, Object> map = new HashMap<>();
		map.put("type", type);
		map.put("description", description);
		map.put("resource", resource);
		suggestion.addAction(map);
		return this;
	}

	public Suggestion build() {
		return suggestion;
	}
}
