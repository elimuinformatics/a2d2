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

package io.elimu.a2d2.process;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.elimu.a2d2.cdsmodel.Dependency;
import io.elimu.a2d2.cdsresponse.entity.TemplateRepository;
import io.elimu.a2d2.exception.BaseException;

public class ServiceUtils {

	private static final String DEFAULT_CONFIG_FOLDER = System.getProperty("catalina.base", "/mnt1/setup") + "/conf/";
	private static final Logger LOG = LoggerFactory.getLogger(ServiceUtils.class);

	private ServiceUtils() {
		throw new IllegalStateException("Utility class");
	}
	
	public static String getDefaultConfigFolder() {
		return DEFAULT_CONFIG_FOLDER;
	}

	public static URL toJarPath(Dependency dep) {
		return toJarPath(dep, true);
	}

	public static URL toJarPath(Dependency dep, boolean create) {
		char s = File.separatorChar;
		String groupIdPath = null;
		try {
			groupIdPath = dep.getGroupId().replaceAll("\\.", Matcher.quoteReplacement(File.separator));
		} catch (Exception e) {
			groupIdPath = dep.getGroupId().replaceAll("\\\\.", Matcher.quoteReplacement(File.separator));
		}
		String jarPath = new StringBuilder(System.getProperty("user.home")).
				append(s).append(".m2").append(s).append("repository").
				append(s).append(groupIdPath).append(s).append(dep.getArtifactId()).
				append(s).append(releaseOrSnapshot(dep.getVersion())).append(s).append(dep.getArtifactId()).
				append('-').append(dep.getVersion()).append(".jar").toString();
		File f = new File(jarPath);
		try {
			if (f.exists()) {
				return f.toURI().toURL();
			} else if (create) {
				//TODO refactor so that the logic does not depend on empty jars
				byte[] data = null;
				Files.write(Paths.get(f.toURI()),  data, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			}
			return f.toURI().toURL();
		} catch (Exception e) {
			throw new BaseException("Couldn't write non-existing file " + f, e);
		}
	}

	private static String releaseOrSnapshot(String version) {
                if (Pattern.compile(".*-\\d{8}.\\d{6}-\\d{1,10}$").matcher(version).matches()) {
                        return version.replace(version.substring(version.lastIndexOf('-') - 15), "SNAPSHOT");
                }
                return version;
        }

	public static Properties getConfig(Dependency dep, String envName) throws IOException {
		Properties config = new Properties();
		//priority 3: service.dsl
		Properties configLevel3 = new Properties();
		URL jarPath = ServiceUtils.toJarPath(dep);
		try {
			configLevel3.load(ServiceUtils.readEntry(jarPath, "service.dsl"));
		} catch (Exception e) {
			LOG.error("Exception while reading jar", e);
		}
		//priority 2: service-envName.dsl
		Properties configLevel2 = new Properties();
		try {
			InputStream is = ServiceUtils.readEntry(jarPath, "service-" + envName + ".dsl");
			if (is != null) {
				configLevel2.load(is);
			}
		} catch (Exception e) {
			LOG.error("Exception while reading jar", e);
		}
		//priority 1: localized service properties file
		//configure a system property pointing to the server's config folder
		Properties configLevel1 = new Properties();
		String appConfigFile = System.getProperty("kie.apps.config.folder", DEFAULT_CONFIG_FOLDER) + "/" + dep.getArtifactId() + ".properties";
		Path path = Paths.get(URI.create("file://" + appConfigFile));
		if (Files.exists(path)) {
			configLevel1.load(Files.newInputStream(path));
		}
		//make sure the priorities for properties are respected on config
		config.putAll(configLevel3);
		config.putAll(configLevel2);
		config.putAll(configLevel1);
		return config;
	}

	public static URL toJarURL(Dependency dep, String workbenchBaseUrl) throws MalformedURLException {
		return toMavenUrl(dep, workbenchBaseUrl, "jar");
	}

	public static URL toPomURL(Dependency dep, String workbenchBaseUrl) throws MalformedURLException {
		return toMavenUrl(dep, workbenchBaseUrl, "pom");
	}

	private static URL toMavenUrl(Dependency dep, String workbenchBaseUrl, String type) throws MalformedURLException {
		String groupIdPath = null;
		try {
			groupIdPath = dep.getGroupId().replaceAll("\\.", Matcher.quoteReplacement("/"));
		} catch (Exception e) {
			groupIdPath = dep.getGroupId().replaceAll("\\\\.", Matcher.quoteReplacement("/"));
		}
		String jarUrl = new StringBuilder(workbenchBaseUrl).
				append('/').append(groupIdPath).append('/').append(dep.getArtifactId()).
				append('/').append(dep.getVersion()).append('/').append(dep.getArtifactId()).
				append('-').append(dep.getVersion()).append('.').append(type).toString();
		return new URL(jarUrl);
	}

	public static boolean isKieModule(URL jarPath) {
		try (JarFile jar = new JarFile(jarPath.getFile())) {
			JarEntry kmoduleEntry = jar.getJarEntry("META-INF/kmodule.xml");
			return (kmoduleEntry != null);
		} catch (IOException ignored) {
			return false;
		}
	}

	public static InputStream readEntry(URL jarPath, String entry) {
		try (JarFile jar = new JarFile(jarPath.getFile())) {
			JarEntry openEntry = jar.getJarEntry(entry);
			DataInputStream input = new DataInputStream(jar.getInputStream(openEntry));
			byte[] data = new byte[(int) openEntry.getSize()];
			input.readFully(data);
			return new ByteArrayInputStream(data);
		} catch (Exception ignored) {
			return null;
		}
	}

	public static Set<String> findEntriesOfType(URL jarPath, String... fileTypes) {
		Set<String> retval = new HashSet<>();
		try (JarFile jar = new JarFile(jarPath.getFile())) {
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				for (String fileType : fileTypes) {
					if (entry.getName().endsWith("." + fileType)) {
						retval.add(entry.getName());
					}
				}
			}
		} catch (IOException ignored) {
			LOG.error("Exception occured due to {}", ignored.getMessage(), ignored);
		}
		return retval;
	}

	public static void loadTemplateEntries(URL jarPath, String fileType, String serviceName) {


		try (JarFile jar = new JarFile(jarPath.getFile())) {
			if (jar.getJarEntry("META-INF/kmodule.xml") == null) {
				//if not a kJAR, it will not have this file, and the FTL search can be ignored
				return;
			}
			LOG.debug("findFtlEntry for the ::" + jarPath + "for file type :: " + "for service name " + serviceName);
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();

				if (entry.getName().endsWith("." + fileType)) {
					String[] names = entry.getName().split("/");
					DataInputStream input = new DataInputStream(jar.getInputStream(entry));
					byte[] data = new byte[(int) entry.getSize()];
					input.readFully(data);

					String ftlContent = new String(data);
					LOG.debug("found ftl file:::  " + serviceName + "_" + names[names.length-1] + " " + ftlContent);
					TemplateRepository.getInstance().getFltMap().put(serviceName + "_" + names[names.length-1] , ftlContent);
					input.close();
				}
			}
		} catch (Exception ex) {
			LOG.error("findFtlEntry failed for ::" + jarPath + "for file type :: " + "for service name " + serviceName);
		}
	}

	public static String getKieWbLocation() {
		return System.getProperty("kie.wb.location");
	}

	public static String getMavenLocation() {
		String mavenProp = System.getProperty("maven.repo.location");
		if (mavenProp != null) {
			return mavenProp;
		}
		return getKieWbLocation() + "/maven2";
	}

	public static String getKieWbUser() {
		return System.getProperty("kie.wb.user");
	}

	public static String getKieWbPassword() {
		return System.getProperty("kie.wb.pwd");
	}

	public static String getMavenUser() {
		String mavenProp = System.getProperty("maven.repo.user");
		if (mavenProp == null) {
			return System.getProperty("kie.wb.user");
		}
		return mavenProp;
	}

	public static String getMavenPassword() {
		String mavenProp = System.getProperty("maven.repo.pwd");
		if (mavenProp == null) {
			return System.getProperty("kie.wb.pwd");
		}
		return mavenProp;
	}

}
