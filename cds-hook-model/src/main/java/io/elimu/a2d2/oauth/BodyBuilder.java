// Copyright 2018-2021 Elimu Informatics
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

package io.elimu.a2d2.oauth;

import org.apache.commons.lang3.StringUtils;

/**
 * Used to build a body parameter's structure for OAuth. Basically a params encoding helper<br>
 * 
 * Here's an example on how to use it:<br>
 * 
 * <code>
 * String body = new BodyBuilder().addToBody("grant_type", "password_credentials").addToBody("client_id", "omnibus-api").addToBody("client_secret", "1234").build();
 * </code>
 */
public class BodyBuilder {

	StringBuilder sb = new StringBuilder();

	/**
	 * Adds a new key value parameter pair to the body structure
	 * @param key the parameter name
	 * @param value the parameter value
	 * @return itself to keep building
	 */
	public BodyBuilder addToBody(String key, String value) {
		if (StringUtils.isEmpty(value)) {
			return this;
		}
		if (sb.length() > 0) {
			sb.append('&');
		}
		sb.append(key).append("=").append(value);
		return this;
	}

	/**
	 * Returns the string containing the body structure
	 * @return the string containing the body structure
	 */
	public String build() {
		return sb.toString();
	}

}
