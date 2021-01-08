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

import org.json.simple.JSONObject;

public class Suggestion  implements Serializable {

	private static final long serialVersionUID = 1L;

	private String label;
	private String uuid;
	private List<JSONObject> actions;

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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actions == null) ? 0 : actions.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
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
		if (actions == null) {
			if (other.actions != null)
				return false;
		} else if (!actions.equals(other.actions)) {
			return false;
		}
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label)) {
			return false;
		}
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid)) {
			return false;
		}
		return true;
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
}
