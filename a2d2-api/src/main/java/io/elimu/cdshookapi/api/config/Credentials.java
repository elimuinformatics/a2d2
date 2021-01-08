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

package io.elimu.cdshookapi.api.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.elimu.serviceapi.service.AppContextUtils;

@Component("credentials")
//@PropertySource("classpath:application.yml")
public class Credentials {

	private String username;
	private String password;
	private String url;
	private String driverClassName;
	
	@Autowired private Environment env;
	
	@Value("${spring.datasource.password}")
	private String dataSourcePassword;
	  
	@PostConstruct
	public void init() {
		username = env.getRequiredProperty("spring.datasource.username");
		password = dataSourcePassword;
		url = env.getRequiredProperty("spring.datasource.url");
		driverClassName = env.getRequiredProperty("spring.datasource.driverClassName");
		System.setProperty("cds.kie.db.className", env.getRequiredProperty("spring.datasource.className"));
		if (env.containsProperty("spring.datasource.serverName") && env.containsProperty("spring.datasource.databaseName")) {
			System.setProperty("cds.kie.db.serverName", env.getProperty("spring.datasource.serverName"));
			System.setProperty("cds.kie.db.databaseName", env.getRequiredProperty("spring.datasource.databaseName"));
		} else {
			System.setProperty("cds.kie.db.url", url);
		}
		System.setProperty("cds.kie.db.user", username);
		System.setProperty("cds.kie.db.password", password);
		System.setProperty("org.quartz.dataSource.nonManagedDS.password", password);
		System.setProperty("org.quartz.properties", env.getRequiredProperty("spring.datasource.quartz.config"));
		System.setProperty("proc.envvar.prefix", env.getProperty("proc.envvar.prefix"));
		System.setProperty("services.pom.path", env.getProperty("services.pom.path"));
		AppContextUtils.getInstance().setEnvironment(env);
	}
	
	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}
	public String getUrl() {
		return url;
	}
	public String getDriverClassName() {
		return driverClassName;
	} 

}
