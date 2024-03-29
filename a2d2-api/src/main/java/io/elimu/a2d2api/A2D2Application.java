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

package io.elimu.a2d2api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@Configuration
@EnableAutoConfiguration  
@ComponentScan(basePackages = {"io.elimu.genericapi.*","io.elimu.cdshookapi","io.elimu.a2d2api", "io.elimu.serviceapi.service", "io.elimu.utils"})
@EnableJpaRepositories(basePackages="io.elimu.service.dao.jpa") 
@EntityScan("io.elimu.service.models")
public class A2D2Application extends SpringBootServletInitializer {

    private static final Class<A2D2Application> applicationClass = A2D2Application.class;

	public static void main(String[] args) {
		enableBtxUniqueProps();
		SpringApplication.run(applicationClass, args);
	}

	private static void enableBtxUniqueProps() {
		String nodeId = String.valueOf(new java.util.Random(System.currentTimeMillis()).nextInt(10000));
		String tlog1 = System.getProperty("bitronix.tm.journal.disk.logPart1Filename");
		if (tlog1 == null) {
			tlog1 = System.getProperty("user.home") + "/btm1/btm1.tlog";
		}
		tlog1 = tlog1.replace(".tlog", "-" + nodeId + ".tlog");
		System.setProperty("bitronix.tm.journal.disk.logPart1Filename", tlog1);
		String tlog2 = System.getProperty("bitronix.tm.journal.disk.logPart2Filename");
		if (tlog2 == null) {
			tlog2 = System.getProperty("user.home") + "/btm2/btm2.tlog";
		}
		tlog2 = tlog2.replace(".tlog", "-" + nodeId + ".tlog");
		System.setProperty("bitronix.tm.journal.disk.logPart2Filename", tlog2);	
	}

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(applicationClass);
    }
    

}
