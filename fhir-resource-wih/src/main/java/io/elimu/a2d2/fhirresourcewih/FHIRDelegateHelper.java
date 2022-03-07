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

package io.elimu.a2d2.fhirresourcewih;

import java.net.URL;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.elimu.a2d2.exception.FhirServerException;
import io.elimu.a2d2.parsing.FhirParseUtil.FormatType;

public class FHIRDelegateHelper {

	private static final Logger log = LoggerFactory.getLogger(FHIRDelegateHelper.class);

	private static final String R4 = "R4";
	private static final String STU3 = "STU3";
	private static final String DSTU2 = "DSTU2";
	private static final String FHIR4 = "FHIR4";
	private static final String FHIR3 = "FHIR3";
	private static final String FHIR2 = "FHIR2";

	public Object getFhirClient(Object ctx, URL fhirUrl) {

		Object client = null;
		try {
			if (fhirUrl != null) {
				client = ctx.getClass().getDeclaredMethod("newRestfulGenericClient", String.class).invoke(ctx, fhirUrl.toString());
				client.getClass().getDeclaredMethod("setEncoding", Class.forName("ca.uhn.fhir.rest.api.EncodingEnum")).invoke(client, Class.forName("ca.uhn.fhir.rest.api.EncodingEnum").getDeclaredField("JSON"));
				client.getClass().getDeclaredMethod("setDontValidateConformance", Boolean.class).invoke(client, true);
			}
	
			return client;
		} catch (Exception e) {
			throw new RuntimeException("Couldn't invoke client", e);
		}
	}

	public boolean authenticateFhirServer(Object client, String authHeader) {

		boolean isAuthenticated = true;

		try {
		if (authHeader != null && !"".equals(authHeader.trim())) {
			if (authHeader.startsWith("Basic ")) {
				String token = authHeader.replace("Basic ", "");
				String tokenDecrypt = new String(Base64.getDecoder().decode(token));
				String[] parts = tokenDecrypt.split(":");
				Object interceptor = Class.forName("ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor").getConstructor(String.class, String.class).newInstance(parts[0], parts[1]);
				client.getClass().getDeclaredMethod("registerInterceptor", Object.class).invoke(client, interceptor);
			} else if (authHeader.startsWith("Bearer ")) {
				Object interceptor = Class.forName("ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor").getConstructor(String.class).newInstance(authHeader.replace("Bearer ", ""));
				client.getClass().getDeclaredMethod("registerInterceptor", Object.class).invoke(client, interceptor);
			}
		}
		}catch(Exception ex) {
			isAuthenticated=false;
		}
		return isAuthenticated;
	}

	public Object getFhirContext(String fhirType) {
		Object ctx = null;
		try {
			switch (String.valueOf(fhirType)) {
			case DSTU2:
				ctx = Class.forName("ca.uhn.fhir.context.FhirContext").getDeclaredMethod("forDstu2").invoke(null);
				break;
			case STU3:
				ctx = Class.forName("ca.uhn.fhir.context.FhirContext").getDeclaredMethod("forDstu3").invoke(null);
				break;
			case R4:
				ctx = Class.forName("ca.uhn.fhir.context.FhirContext").getDeclaredMethod("forR4").invoke(null);
				break;
			default:
				ctx = Class.forName("ca.uhn.fhir.context.FhirContext").getDeclaredMethod("forDstu2").invoke(null);
				break;
			}
	
			return ctx;
		} catch (Exception e) {
			throw new RuntimeException("Coudln't start FhirContext", e);
		}
	}

	public FormatType getFormatType(String fhirType) {
		FormatType type = null;
		switch (String.valueOf(fhirType)) {
		case DSTU2:
			type = FormatType.FHIR2;
			break;
		case STU3:
			type = FormatType.FHIR3;
			break;
		case R4:
			type = FormatType.FHIR4;
			break;
		default:
			type = FormatType.FHIR2;
			break;
		}
		return type;
	}

	public static FormatType getFormatTypeFromVersion(String fhirVersion) {
		if (fhirVersion == null) {
			fhirVersion = FHIR2; // default
		}
		FormatType type = null;
		switch (fhirVersion) {
		case FHIR2:
			type = FormatType.FHIR2;
			break;
		case FHIR3:
			type = FormatType.FHIR3;
			break;
		case FHIR4:
			type = FormatType.FHIR4;
			break;
		default:
			log.error("Invalid fhirVersion: {}", fhirVersion);
			throw new FhirServerException("Invalid fhirVersion: " + fhirVersion);
		}
		return type;
	}

}
