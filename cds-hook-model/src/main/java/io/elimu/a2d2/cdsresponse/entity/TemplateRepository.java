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

public class TemplateRepository {

	private static TemplateRepository templateRepoInstance = null;
	private Map<String, String> fltMap = new HashMap<>();

	private TemplateRepository() {
	}

	public static TemplateRepository getInstance() {
		if (templateRepoInstance == null)
			templateRepoInstance = new TemplateRepository();

		return templateRepoInstance;
	}

	public Map<String, String> getFltMap() {
		return fltMap;
	}

	public void setFltMap(Map<String, String> fltMap) {
		this.fltMap = fltMap;
	}

}
