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
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.impl.BaseClient;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
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

	public IGenericClient getFhirClient(FhirContext ctx, URL fhirUrl) {

		IGenericClient client = null;

		if (fhirUrl != null) {
			client = ctx.newRestfulGenericClient(fhirUrl.toString());
			client.setEncoding(EncodingEnum.JSON);
			((BaseClient) client).setDontValidateConformance(true);
		}

		return client;
	}

	public boolean authenticateFhirServer(IGenericClient client, String authHeader) {

		boolean isAuthenticated = true;

		try {
		if (authHeader != null && !"".equals(authHeader.trim())) {
			if (authHeader.startsWith("Basic ")) {
				String token = authHeader.replace("Basic ", "");
				String tokenDecrypt = new String(Base64.getDecoder().decode(token));
				String[] parts = tokenDecrypt.split(":");
				client.registerInterceptor((Object) new BasicAuthInterceptor(parts[0], parts[1]));
			} else if (authHeader.startsWith("Bearer ")) {
				client.registerInterceptor((Object) new BearerTokenAuthInterceptor(authHeader.replace("Bearer ", "")));
			}
		}
		}catch(Exception ex) {
			isAuthenticated=false;
		}
		return isAuthenticated;
	}

	public FhirContext getFhirContext(String fhirType) {
		FhirContext ctx = null;
		switch (String.valueOf(fhirType)) {
		case DSTU2:
			ctx = FhirContext.forDstu2();
			break;
		case STU3:
			ctx = FhirContext.forDstu3();
			break;
		case R4:
			ctx = FhirContext.forR4();
			break;
		default:
			ctx = FhirContext.forDstu2();
			break;
		}

		return ctx;
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
