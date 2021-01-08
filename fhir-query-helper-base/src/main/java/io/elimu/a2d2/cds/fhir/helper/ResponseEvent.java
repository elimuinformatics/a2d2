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

package io.elimu.a2d2.cds.fhir.helper;

import java.io.Serializable;

public class ResponseEvent<T> implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 4585202206069885183L;

	private final T value;
	private final long timeout;

	public ResponseEvent() {
		this.timeout = System.currentTimeMillis() + QueryingServerHelperBase.RESPONSE_EVENT_TIMEOUT;
		this.value = null;
	}

	public ResponseEvent(T value, long timeout) {
		this.value = value;
		this.timeout = System.currentTimeMillis() + timeout;
	}

	public T getValue() {
		return value;
	}

	public long getTimeout() {
		return timeout;
	}

	public boolean isNew() {
		return System.currentTimeMillis() < timeout;
	}

}
