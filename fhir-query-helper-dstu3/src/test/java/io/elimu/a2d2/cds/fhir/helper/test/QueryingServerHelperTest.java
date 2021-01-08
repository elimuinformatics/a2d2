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

package io.elimu.a2d2.cds.fhir.helper.test;

import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import io.elimu.a2d2.cds.fhir.cache.CacheUtil;
import io.elimu.a2d2.cds.fhir.helper.dstu3.QueryingServerHelper;
import io.elimu.a2d2.cds.fhir.helper.dstu3.QueryingServerHelper.FhirVersion;

@SuppressWarnings("deprecation")
public class QueryingServerHelperTest {
	
	private static final String FHIR3_URL = "http://hapi.fhir.org/baseDstu3";

	public static QueryingServerHelper queryingServerHelper = new QueryingServerHelper(
			FHIR3_URL).
			addAuthentication(CacheUtil.BASIC, "user", "pass");

	@Ignore("Broken, fix later")
	@Test
	public void fhirQueryAndQueryPartTestWithFhirVersion3() {
		QueryingServerHelper queryingServerHelperTest = new QueryingServerHelper(
				FHIR3_URL, FhirVersion.FHIR3);
		List<IBaseResource> iResourceList = queryingServerHelperTest.queryResources("Observation", "2", "subject:Patient",
				"code=http://snomed.info|363779003");
		Assert.assertNotNull(iResourceList);
		Assert.assertFalse(iResourceList.isEmpty());
		IBaseResource iResource = queryingServerHelperTest.getResourceById("Encounter", "1207");
		Assert.assertNotNull(iResource);
		Assert.assertTrue(iResource instanceof org.hl7.fhir.dstu3.model.Encounter);
		org.hl7.fhir.dstu3.model.Encounter enc = (org.hl7.fhir.dstu3.model.Encounter)iResource;
	}

}
