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

package io.elimu.a2d2.genericmodel;

import java.io.Serializable;

/**
 * Name value pair object to map primitive values and specific objects of similar type
 * from the process into the rules, and from one group of rules to the next one.
 */
public class NamedDataObject implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Name of the name-value pair. Just an identifier string
	 */
	private String name;

	/**
	 * Value of the name-value pair. Basically any kind of object
	 */
	private Object value;

	/**
	 * A Constructor for both parameters
	 * @param name the name
	 * @param value the value
	 */
	public NamedDataObject(String name, Object value) {
		super();
		this.name = name;
		this.value = value;
	}

	/**
	 * A default constructor
	 */
	public NamedDataObject() {
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		NamedDataObject other = (NamedDataObject) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "NamedDataObject [name=" + name + ", value=" + value + "]";
	}
}
