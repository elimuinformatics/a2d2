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

package io.elimu.serviceapi.service;

import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.elimu.a2d2.process.ServiceUtils;

@Component
public class NoOpCreateSubscription implements CreateSubscription {

	private static final Logger log = LoggerFactory.getLogger(NoOpCreateSubscription.class);

	@Override
	public void create(AbstractKieService service,String type){
		try {
			URL jarPath = ServiceUtils.toJarPath(service.getDependency());
			Properties cds = new Properties();
			InputStream entry = ServiceUtils.readEntry(jarPath, "service.dsl");

			cds.load(entry);

			cds.stringPropertyNames()
			.stream()
			.filter(key -> key.equalsIgnoreCase("packages"))
			.forEach(key -> {
				List<String> packagelist = Arrays.asList(cds.getProperty(key).split(","));
				packagelist
				.forEach(packageName -> {
					String productName = service.getDefaultCustomer() + "::" + packageName;
					createProductAndPlan(productName, type, service.getDefaultCustomer());
				});
			});

		} catch (Exception ex) {
			log.error("Error in saving the products ::"+ex);
		}
	}

	private void createProductAndPlan(String productName, String type, String owner) {
		log.debug("Can create productName = " + productName + ", type = " + type + ", owner = " + owner);
	}

}
