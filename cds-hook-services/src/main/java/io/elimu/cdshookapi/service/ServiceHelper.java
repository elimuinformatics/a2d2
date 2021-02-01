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

package io.elimu.cdshookapi.service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import io.elimu.a2d2.cdsmodel.Dependency;
import io.elimu.a2d2.process.ServiceUtils;
import io.elimu.cdshookapi.entity.CDSService;
import io.elimu.genericapi.service.GenericKieBasedService;
import io.elimu.genericapi.service.GenericTempService;
import io.elimu.genericapi.service.RunningServices;
import io.elimu.service.dao.jpa.CDSServiceRepository;
import io.elimu.service.models.ServiceInfo;

/*
 * Sample service to demonstrate what the API would use to get things done
 */
@Service
@DependsOn("credentials")
public class ServiceHelper {

	private static final Logger log = LoggerFactory.getLogger(ServiceHelper.class);

	private ExecutorService executor = Executors.newSingleThreadExecutor();
	
	private static final String CLIENT = "client";
	private static final String DEPENDENCIES = "dependencies";
	private static final MavenXpp3Reader reader = new MavenXpp3Reader();
	
	@Autowired
	private CDSServiceRepository cdsRepo;

	@SuppressWarnings("unchecked")
	@PostConstruct
	public void init() {
		if (StringUtils.equalsIgnoreCase(System.getProperty("kie.maven.offline.force"), "true")) {
			log.debug("Reading xmls for kjar services in offline mode");
			Map<String, Object> pomData = readPomDependencies();
			Collection<String> dependencies = (Collection<String>) pomData.get(DEPENDENCIES);
			dependencies.forEach(dep -> RunningServices.getInstance()
					.register(new GenericTempService(dep, pomData.get(CLIENT).toString())));
			
			executor.execute(() -> {
				dependencies.forEach(dep -> {
					initializeKieService(dep, pomData.get(CLIENT).toString());
				});
				RunningServices.getInstance().markStarted();
			});
		} else {
			//we first put a placeholder for startup
			cdsRepo.findAllGeneric().forEach(info -> RunningServices.getInstance().register(new GenericTempService(info.getServiceData(), info.getDefaultCustomer())));
			executor.execute(() -> {
				// we now have two types of services: generic services and kie services
				// then we start every one
				for (ServiceInfo info : cdsRepo.findAllGeneric()) {
					initializeKieService(info.getServiceData(), info.getDefaultCustomer());
				}
				RunningServices.getInstance().markStarted();
			});
		}
	}

	private void initializeKieService(String dep, String client) {
		try {
			RunningServices.getInstance().downloadDependency(new Dependency(dep));
			RunningServices.getInstance()
			.register(new GenericKieBasedService(dep, client));
		} catch (Exception e) {
			log.error("Couldn't initialize service '" + dep + "'", e);
		}
	}
	
	private Map<String, Object> readPomDependencies() {
		Map<String, Object> pomData = new ConcurrentSkipListMap<>();
		Collection<String> dependencies = new TreeSet<>();
		String servicePomPath = System.getProperty("services.pom.path");
		try {
			for (File file : new File(servicePomPath).listFiles()) {
				log.debug("Loading xml : " + file.getAbsolutePath());
				if (!file.isDirectory()) {
					Optional<Model> model = getModel(file);
					if(model.isPresent()) {
						pomData.put(CLIENT, model.get().getProperties().getProperty(CLIENT));
						model.get().getDependencies().forEach(dep -> {
							dependencies.add(dep.getGroupId() + ':' + dep.getArtifactId() + ':' + dep.getVersion());
						});
					}
				}
			}
			pomData.put(DEPENDENCIES, dependencies);
		} catch(NullPointerException e) {
			pomData.put(DEPENDENCIES, Collections.emptyList());
			log.error("Couldn't initialize services due to invalid path :"+servicePomPath, e);
		}
		return pomData;
	}
	
	private Optional<Model> getModel(File file) {
		try {
		    return Optional.ofNullable(reader.read(new FileReader(file.getAbsolutePath())));
		} catch (IOException | XmlPullParserException e) {
			log.error("Xml pull parser related faults", e);
		}
		return Optional.empty();
	}

	public CDSService getDefinition(ServiceInfo info,String type,String client,String packageName) throws Exception {
		CDSService cdsService = new CDSService();

		try {

			if(info == null || type == null ||
					packageName == null || client == null) {
				log.error("GetDefinition Parameter missing : serviceInfo or type"
						+ " or packagename or client");
				return null;
			}

			String serviceData=info.getServiceData();

			Dependency dep = new Dependency(serviceData);
			URL jarPath = ServiceUtils.toJarPath(dep);
			Properties config = new Properties();

			InputStream entry = ServiceUtils.readEntry(jarPath, "service.dsl");
			config.load(entry);
			String[] packagenames = config.getProperty("cds.based.project.packages").split(",");

			if(!Arrays.asList(packagenames).contains(packageName) && !packageName.equalsIgnoreCase("all")) {
				return null;
			}

			String hook = config.getProperty("cds.based.project." + type);
			cdsService.setHook(hook);
			cdsService.setTitle(config.getProperty("cds.based.project.title"));
			cdsService.setDescription(config.getProperty("cds.based.project.description"));
			cdsService.setId(dep.getArtifactId());
			Map<String, String> prefetch = new HashMap<>();

			config.stringPropertyNames().forEach(key -> {
				if (key.startsWith("cds.based.project.prefetch.")) {
					prefetch.put(key.replace("cds.based.project.prefetch.", ""), config.getProperty(key));
				}
			});

			cdsService.setPrefetch(prefetch);
		} catch(UnsupportedOperationException | ClassCastException | NullPointerException | IllegalArgumentException ex) {
			log.error("Exception occured in getDefinition of ServiceInfo",ex);
			throw ex;
		}

		log.trace("ServiceHelper.getDefinition for Serviceinfo is completed");
		return cdsService;
	}
}
