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

package io.elimu.a2d2.parsing;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;
import ca.uhn.fhir.context.FhirContext;

public class FhirParseUtil {

	//TODO Change this to change amount of instances available
	private static final int AMOUNT_OF_PARSERS = Integer.parseInt(System.getProperty("fhir.parsers.count", "30"));

	public static enum FormatType {
		FHIR2(FhirContext.forDstu2()),
		FHIR3(FhirContext.forDstu3()),
		FHIR4(FhirContext.forR4());


		private final FhirContext ctx;

		FormatType(FhirContext ctx) {
			this.ctx = ctx;
		}

		public FhirContext getCtx() {
			return ctx;
		}
	}

	private static final Iterator<FhirParseUtil> ITERATOR;

	static {
		List<FhirParseUtil> aux = new LinkedList<>();
		for (int index = 0; index < AMOUNT_OF_PARSERS; index++) {
			aux.add(new FhirParseUtil());
		}
		ITERATOR = new ConcurrentLoopIterator<FhirParseUtil>(aux);
	}

	public static FhirParseUtil getInstance() {
		return ITERATOR.next();
	}

	private FhirParseUtil() {
	}

	public synchronized IBaseResource parseJsonResource(FormatType type, String input) {
		return type.getCtx().newJsonParser().parseResource(input);
	}

	public synchronized IBaseResource parseXmlResource(FormatType type, String input) {
		return type.getCtx().newXmlParser().parseResource(input);
	}

	public synchronized <T extends IBaseResource> T parseResource(FormatType type, String input, Class<T> outputType) {
		return type.getCtx().newJsonParser().parseResource(outputType, input);
	}

	public synchronized String encodeJsonResource(FormatType type, IBaseResource resource) {
		return type.getCtx().newJsonParser().encodeResourceToString(resource);
	}

	public synchronized String encodeXmlResource(FormatType type, IBaseResource resource) {
		return type.getCtx().newXmlParser().encodeResourceToString(resource);
	}
}
