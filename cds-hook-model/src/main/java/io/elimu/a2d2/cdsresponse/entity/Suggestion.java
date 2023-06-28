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
import java.util.Map;
import java.util.Objects;

import org.json.simple.JSONObject;

public class Suggestion  implements Serializable {

	private static final long serialVersionUID = 1L;

	private String label;
	private String uuid;
	private List<JSONObject> actions;
	private boolean isRecommended;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getUuid() {
		return uuid;
	}

	@Override
	public int hashCode() {
		return Objects.hash(actions, label, uuid, isRecommended);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Suggestion other = (Suggestion) obj;
		return Objects.equals(actions, other.actions) && Objects.equals(label, other.label)
				&& Objects.equals(uuid, other.uuid) && Objects.equals(isRecommended, other.isRecommended);
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public List<JSONObject> getActions() {
		return actions;
	}

	public void setActions(List<JSONObject> actions) {
		this.actions = actions;
	}

	public void addAction(Map<String, Object> map) {
		if (this.actions == null) {
			this.actions = new ArrayList<>();
		}
		this.actions.add(new JSONObject(map));
	}
	
	public boolean getIsRecommended() {
		return isRecommended;
	}
	
	public void setIsRecommended(boolean isRecommended) {
		this.isRecommended = isRecommended;
	}
}
