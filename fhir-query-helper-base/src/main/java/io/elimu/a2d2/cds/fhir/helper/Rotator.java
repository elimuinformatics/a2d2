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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

public class Rotator<T> {

	private final List<T> rotation;
	private Iterator<T> iter;

	public Rotator(Callable<T> callable, int count) {
		this.rotation = new ArrayList<T>(count);
		for (int index = 0; index < count; index++) {
			try {
				rotation.add(callable.call());
			} catch (Exception ignore) { 
				//no-op
			}
		}
		this.iter = rotation.iterator();
	}

	public synchronized T next() {
		if (!iter.hasNext()) {
			iter = rotation.iterator();
		}
		return iter.next();

	}
}
