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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ResourceBuilder {

	public static ResourceBuilder create() {
		return new ResourceBuilder(null);
	}

	private HashMap<String, Object> map;
	private ResourceBuilder parent;

	private ResourceBuilder(ResourceBuilder parent) {
		this.map = new HashMap<>();
		this.parent = parent;
	}

	public ResourceBuilder child(String key) {
		ResourceBuilder child = new ResourceBuilder(this);
		this.map.put(key, child.build());
		return child;
	}

	public ResourceBuilder childArray(String key) {
		ResourceBuilder child = new ResourceBuilder(this);
		this.map.put(key, Arrays.asList(child.build()));
		return child;
	}

	public ResourceBuilder end() {
		return parent;
	}

	public ResourceBuilder with(String key, Object value) {
		this.map.put(key, value);
		return this;
	}

	public ResourceBuilder with(String key, Map<String, Object> complexValue) {
		this.map.put(key, complexValue);
		return this;
	}

	public Map<String, Object> build() {
		return map;
	}
}
