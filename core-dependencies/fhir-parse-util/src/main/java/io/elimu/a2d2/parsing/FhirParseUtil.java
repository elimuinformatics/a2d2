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

public class FhirParseUtil {

	//Change this to change amount of instances available
	private static final int AMOUNT_OF_PARSERS = Integer.parseInt(System.getProperty("fhir.parsers.count", "30"));

	public static enum FormatType {
		FHIR2("forDstu2"),
		FHIR3("forDstu3"),
		FHIR4("forR4");


		private final Object ctx;

		FormatType(String method) { 
			try {
				Class<?> ctxClass = Class.forName("ca.uhn.fhir.context.FhirContext");
				this.ctx = ctxClass.getMethod(method).invoke(null);
			} catch (Exception e) {
				throw new RuntimeException("Cannot instantiate FhirContext", e);
			}
		}

		public Object getCtx() {
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

	public synchronized Object parseJsonResource(FormatType type, String input) {
		try {
			Object parser = type.getCtx().getClass().getMethod("newJsonParser").invoke(type.getCtx());
			return parser.getClass().getMethod("parseResource", String.class).invoke(parser, input);
		} catch (Exception e) {
			throw new RuntimeException("Coudln't invoke newJsonParser.parserResource", e);
		}
	}

	public synchronized Object parseXmlResource(FormatType type, String input) {
		try {
			Object parser = type.getCtx().getClass().getMethod("newXmlParser").invoke(type.getCtx());
			return parser.getClass().getMethod("parseResource", String.class).invoke(parser, input);
		} catch (Exception e) {
			throw new RuntimeException("Coudln't invoke newXmlParser.parserResource", e);
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> T parseResource(FormatType type, String input, Class<T> outputType) {
		try {
			Object parser = type.getCtx().getClass().getMethod("newJsonParser").invoke(type.getCtx());
			return (T) parser.getClass().getMethod("parseResource", Class.class, String.class).invoke(parser, outputType, input);
		} catch (Exception e) {
			throw new RuntimeException("Coudln't invoke newJsonParser.parserResource", e);
		}
	}

	public synchronized String encodeJsonResource(FormatType type, Object hapiresource) {
		try {
			Object parser = type.getCtx().getClass().getMethod("newJsonParser").invoke(type.getCtx());
			return (String) parser.getClass().getMethod("encodeResourceToString", Class.forName("org.hl7.fhir.instance.model.api.IBaseResource")).invoke(parser, hapiresource);
		} catch (Exception e) {
			throw new RuntimeException("Coudln't invoke newJsonParser.encodeResourceToString", e);
		}
	}

	public synchronized String encodeXmlResource(FormatType type, Object hapiresource) {
		try {
			Object parser = type.getCtx().getClass().getMethod("newXmlParser").invoke(type.getCtx());
			return (String) parser.getClass().getMethod("encodeResourceToString", Class.forName("org.hl7.fhir.instance.model.api.IBaseResource")).invoke(parser, hapiresource);
		} catch (Exception e) {
			throw new RuntimeException("Coudln't invoke newXmlParser.encodeResourceToString", e);
		}
	}
}
