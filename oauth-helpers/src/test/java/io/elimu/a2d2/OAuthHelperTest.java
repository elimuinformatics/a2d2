// Copyright 2018-2021 Elimu Informatics
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

package io.elimu.a2d2;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Resource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import io.elimu.a2d2.cds.fhir.helper.FhirResponse;
import io.elimu.a2d2.cds.fhir.helper.QueryingServerHelperBase;
import io.elimu.a2d2.cds.fhir.helper.QueryingServerHelperBase.FhirVersionAbs;
import io.elimu.a2d2.genericmodel.NamedDataObject;

public class OAuthHelperTest {
	
	private KieSession ksession;

	@Before
	public void setUp() throws Exception {
		Assume.assumeTrue(System.getProperty("tokenUrl") != null && System.getProperty("oauthUsername") != null && System.getProperty("oauthPassword") != null);
		
		KieServices ks = KieServices.get();
		KieFileSystem kfs = ks.newKieFileSystem();
		String drl = "import io.elimu.a2d2.cds.fhir.helper.QueryingServerHelperBase;\n"
				+ "import io.elimu.a2d2.genericmodel.NamedDataObject;\n"
				+ "import io.elimu.a2d2.OAuthHelper;\n"
				+ "\n"
				+ "rule \"test\" \n"
				+ "salience 10\n"
				+ "when\n"
				+ "	qsh: QueryingServerHelperBase()\n"
				+ "	NamedDataObject(name == \"fhirScope\")\n"
				+ "then\n"
				+ "	OAuthHelper.addOAuthToken(drools, qsh);\n"
				+ "end\n"
				+ "";
		
		kfs.write("src/main/resources/rules.drl", ks.getResources().newByteArrayResource(drl.getBytes()));
		KieBuilder kbuilder = ks.newKieBuilder(kfs);
		kbuilder.buildAll();
		if (kbuilder.getResults().hasMessages(Message.Level.ERROR)) {
			throw new IllegalArgumentException("Cannot compile kbase: " + kbuilder.getResults());
		}
		
		KieContainer kc = ks.newKieContainer(kbuilder.getKieModule().getReleaseId());
		this.ksession = kc.newKieSession();
	}
	
	@After
	public void tearDown() throws Exception {
		if (ksession != null) {
			ksession.destroy();
		}
	}
	
	@Test
	public void testGetToken() throws Exception {
		TestQueryingServerHelper qsh = new TestQueryingServerHelper();
		this.ksession.insert(qsh);
		this.ksession.insert(new NamedDataObject("fhirScope", "offline_access"));
		this.ksession.insert(new NamedDataObject("fhirClientId", "omnibus-api"));
		this.ksession.insert(new NamedDataObject("fhirClientSecret", ""));
		this.ksession.insert(new NamedDataObject("fhirGrantType", "password"));
		this.ksession.insert(new NamedDataObject("fhirTokenUrl", System.getProperty("tokenUrl")));
		this.ksession.insert(new NamedDataObject("fhirUsername", System.getProperty("oauthUsername")));
		this.ksession.insert(new NamedDataObject("fhirPasswordDecrypt", System.getProperty("oauthPassword")));
		
		this.ksession.fireAllRules();
		String token = qsh.getToken();
		Assert.assertNotNull(token);
		Assert.assertNotEquals("null", token);
	}
	
	public static enum FhirVersion implements FhirVersionAbs {
		
		FHIR4(FhirContext.forR4());
		
		private final FhirContext ctx;

		FhirVersion(FhirContext ctx) {
			this.ctx = ctx;
		}

		public FhirContext getCtx() {
			return ctx;
		}
	}
	
	private static class TestQueryingServerHelper extends QueryingServerHelperBase<Resource, IBaseResource> {
		
		public TestQueryingServerHelper() {
			super("https://fhir4-internal.elimuinformatics.com/fhir", FhirVersion.FHIR4, FhirVersionEnum.R4);
		}

		@Override
		public FhirResponse<IBaseResource> fetchServer(String resourceType, String resourceQuery) {
			return new FhirResponse<IBaseResource>(null, 404, "Not Found");
		}
		
		@Override
		public FhirResponse<List<IBaseResource>> queryServer(String resourceQuery) {
			return new FhirResponse<List<IBaseResource>>(new ArrayList<IBaseResource>(), 200, "OK");
		}

		public String getToken() throws Exception {
			for (IClientInterceptor interceptor : super.interceptors) {
				if (interceptor instanceof BearerTokenAuthInterceptor) {
					BearerTokenAuthInterceptor btai = (BearerTokenAuthInterceptor) interceptor;
					return btai.getToken();
				}
			}
			return null;
		}
	}
}
