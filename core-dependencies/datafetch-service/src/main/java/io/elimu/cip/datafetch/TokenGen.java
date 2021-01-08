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

package io.elimu.cip.datafetch;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.Map;

public class TokenGen {

	public static String generateHttpHeader(Map<String, Object> auth) {
		if (auth.get("token_type") != null && auth.get("access_token") != null) {
			return String.format("%s %s", auth.get("token_type"), auth.get("access_token"));
		} else if (auth.get("access_token") != null) {
			return String.format("Bearer %s", auth.get("access_token"));
		}
		return "";
	}

	public static String generateAuthToken(EHRConsumer ehr, String serviceId) {
		return Jwts.builder().
			setIssuer("https://elimu.io/").
			setAudience(ehr.getAudienceUrl()).
			setSubject(ehr.getEhrId()).
			setExpiration(new Date(System.currentTimeMillis() + 300_000)).
			setIssuedAt(new Date()).
			signWith(SignatureAlgorithm.HS256, secretFor(ehr.getEhrId(), serviceId)).
			compact();
	}

	private static final String STATIC_SECRET = "1234-56789-12345";
	protected static String secretFor(String ehrId, String serviceId) {
		return STATIC_SECRET; //TODO make configurable
	}
}
