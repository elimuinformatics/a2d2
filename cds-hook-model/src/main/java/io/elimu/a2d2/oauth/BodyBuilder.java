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

public class BodyBuilder {

	StringBuilder sb = new StringBuilder();
	
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

	public String build() {
		return sb.toString();
	}

}
