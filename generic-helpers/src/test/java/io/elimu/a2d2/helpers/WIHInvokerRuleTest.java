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

package io.elimu.a2d2.helpers;

import java.util.HashMap;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class WIHInvokerRuleTest {

	@Test
	public void testRuleInvokationOfWIH() throws Exception {
		String drl = "package x\n"
				+ "import io.elimu.a2d2.helpers.WIHInvoker;\n"
				+ "import java.util.HashMap\n"
				+ "import java.util.Map\n"
				+ "global HashMap output\n"
				+ "rule A\n"
				+ "when\n"
				+ " $s: String()\n"
				+ "then\n"
				+ "  HashMap $params = new HashMap();\n"
				+ "  $params.put(\"param1\", $s);\n"
				+ "  Map $result = WIHInvoker.invoke(drools, \"MyWIH\", $params);\n"
				+ "  output.putAll($result);\n"
				+ "end\n";
		KieServices ks = KieServices.get();
		KieFileSystem kfs = ks.newKieFileSystem();
		kfs.write("src/main/resources/x/rule.drl", ks.getResources().newByteArrayResource(drl.getBytes()));
		KieBuilder kbuilder = ks.newKieBuilder(kfs);
		kbuilder.buildAll();
		if (kbuilder.getResults().hasMessages(Message.Level.ERROR)) {
			throw new IllegalArgumentException("Invalid kbase: " + kbuilder.getResults());
		}
		KieContainer kc = ks.newKieContainer(kbuilder.getKieModule().getReleaseId());
		KieSession ksession = kc.newKieSession();
		HelperTestWorkItemHandler handler = new HelperTestWorkItemHandler();
		HashMap<String, Object> output = new HashMap<>();
		ksession.getWorkItemManager().registerWorkItemHandler("MyWIH", handler);
		ksession.setGlobal("output", output);
		ksession.insert("VALUE");
		ksession.fireAllRules();
		Assert.assertNotNull(output.get("out1"));
		Assert.assertEquals("VALUE_OUT", output.get("out1"));
	}

	@Test
	public void testRuleInvokationOfWIHWithDynamicParams() throws Exception {
		String drl = "package x\n"
				+ "import io.elimu.a2d2.helpers.WIHInvoker;\n"
				+ "import java.util.HashMap\n"
				+ "import java.util.Map\n"
				+ "global HashMap output\n"
				+ "rule A\n"
				+ "when\n"
				+ " $s: String()\n"
				+ "then\n"
				+ "  Map $result = WIHInvoker.invokeDyn(drools, \"MyWIH\", \"param1\", $s);\n"
				+ "  output.putAll($result);\n"
				+ "end\n";
		KieServices ks = KieServices.get();
		KieFileSystem kfs = ks.newKieFileSystem();
		kfs.write("src/main/resources/x/rule.drl", ks.getResources().newByteArrayResource(drl.getBytes()));
		KieBuilder kbuilder = ks.newKieBuilder(kfs);
		kbuilder.buildAll();
		if (kbuilder.getResults().hasMessages(Message.Level.ERROR)) {
			throw new IllegalArgumentException("Invalid kbase: " + kbuilder.getResults());
		}
		KieContainer kc = ks.newKieContainer(kbuilder.getKieModule().getReleaseId());
		KieSession ksession = kc.newKieSession();
		HelperTestWorkItemHandler handler = new HelperTestWorkItemHandler();
		HashMap<String, Object> output = new HashMap<>();
		ksession.getWorkItemManager().registerWorkItemHandler("MyWIH", handler);
		ksession.setGlobal("output", output);
		ksession.insert("VALUE");
		ksession.fireAllRules();
		Assert.assertNotNull(output.get("out1"));
		Assert.assertEquals("VALUE_OUT", output.get("out1"));
	}

}
