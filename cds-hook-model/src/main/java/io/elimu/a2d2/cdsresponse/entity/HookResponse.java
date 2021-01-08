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

public class HookResponse extends Response {

	private static final long serialVersionUID = 1L;

	private String result;
	private boolean status;
	private Object output;

	public HookResponse() {
	}

	public HookResponse(Object output) {
		this.output = output;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getResult() {
		return result;
	}

	public Object getOutput() {
		return output;
	}

	public void setOutput(Object output) {
		this.output = output;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int res = 1;
		res = prime * res + ((this.result == null) ? 0 : this.result.hashCode());
		return res;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HookResponse other = (HookResponse) obj;
		if (result == null) {
			if (other.result != null)
				return false;
		} else if (!result.equals(other.result)) {
			return false;
		}
		return true;
	}

	public void setResult(String result) {
		this.result = result;
	}

}
