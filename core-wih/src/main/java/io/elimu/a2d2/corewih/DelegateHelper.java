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

package io.elimu.a2d2.corewih;

import java.util.Map.Entry;
import org.kie.api.runtime.process.WorkItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelegateHelper {

	private static final Logger log = LoggerFactory.getLogger(DelegateHelper.class);

	private DelegateHelper() {
		throw new IllegalStateException("Utility class");
	}
	
	public static void formatWorkItemValues(WorkItem workItem) {

		for(Entry<String, Object>entrySet : workItem.getParameters().entrySet())
		{
			Object value = entrySet.getValue();
			if(value instanceof String &&
					value.toString().contains("__")) {
				value = value.toString().replace("__", "-");
				entrySet.setValue(value);
				log.debug("Formatting value of {} to ::{}", entrySet.getKey(), value);
			}
		}
	}
}
