package io.elimu.genericapi.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.appformer.maven.integration.MavenRepository;
import org.appformer.maven.support.AFReleaseId;
import org.appformer.maven.support.AFReleaseIdImpl;
import org.appformer.maven.support.PomModel;
import org.eclipse.aether.transfer.ArtifactNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.elimu.a2d2.cdsmodel.Dependency;
import io.elimu.a2d2.exception.BaseException;
import io.elimu.a2d2.process.ServiceUtils;
import io.elimu.serviceapi.service.AppContextUtils;
import io.elimu.serviceapi.service.ServiceStore;

public class RunningServices {

	private static final Logger LOG = LoggerFactory.getLogger(RunningServices.class);

	private static final RunningServices INSTANCE = new RunningServices();
	
	private Map<String, GenericService> registry = new HashMap<>();
	
	private boolean started = false;

	public static RunningServices getInstance() {
		return INSTANCE;
	}
	
	private RunningServices() {
	}

	public boolean isStarted() {
		return started;
	}

	public void markStarted() {
		this.started = true;
	}
	
	public void register(GenericService service) {
		register(service, false);
	}
	
	public void register(GenericService service, boolean store) {
		if (store && service instanceof GenericKieBasedService) {
			ServiceStore.get().markUpdate((GenericKieBasedService) service);
		}
		registry.put(service.getId(), service);
		if (store && service instanceof GenericKieBasedService) {
			ServiceStore.get().updateDone((GenericKieBasedService) service);
		}
	}

	public GenericService get(String id) {
		return registry.get(id);
	}

	public GenericService unregister(String id) {
		return unregister(id, false);
	}
	
	public GenericService unregister(String id, boolean store) {
		int i=0;
		if(!store) {
			i=ServiceStore.get().delete(id);
			if(i==0) {
				return null;
			}
		}
		return registry.remove(id);
	}
	
	public Set<String> serviceNames() {
		return Collections.unmodifiableSet(registry.keySet());
	}
	
	public void recreateDependency(Dependency dep) {
		downloadDependency(dep);
		restartService(dep);
	}
	
//	private RemoteRepository remoteRepo = null;

	public PomModel parseModel(Dependency dep, InputStream pomXml) {
		PomModel pomModel = null;
		byte[] pomXmlBytes = null;
		while (pomModel == null) {
			try {
				if (pomXmlBytes == null) {
					pomXmlBytes = IOUtils.toByteArray(pomXml);
				}
				pomModel = PomModel.Parser.parse("pom.xml", new ByteArrayInputStream(pomXmlBytes));
			} catch (IOException e) {
				throw new BaseException("Problem reading initial pom.xml", e);
			} catch (RuntimeException e) {
				ArtifactNotFoundException anfe = extractArtifactNotFound(e);
				if (anfe == null) {
					throw e;
				}
				Dependency dep2 = new Dependency(anfe.getArtifact().getGroupId(), anfe.getArtifact().getArtifactId(), anfe.getArtifact().getVersion());
				downloadDependency(dep2);
			}
		}
		return pomModel;
	}

	private ArtifactNotFoundException extractArtifactNotFound(Throwable e) {
		Throwable cause = e.getCause();
		if (cause == null || cause == e) {
			return null;
		}
		if (cause instanceof ArtifactNotFoundException) {
			return (ArtifactNotFoundException) cause;
		}
		return extractArtifactNotFound(cause);
	}

	public void downloadDependency(Dependency dep) {
		//if ("true".equalsIgnoreCase(String.valueOf(System.getProperty("kie.maven.offline.force")))) {
		if (true) {
			LOG.debug("Running in offline read mode");
			return;
		}
		LOG.debug("Running in online mode");
		String kieWbMavenLocation = ServiceUtils.getMavenLocation();
		String kieWbUser = ServiceUtils.getMavenUser();
		String kieWbPassword = ServiceUtils.getMavenPassword();
		try {
			byte[] jarContent = readUrl(dep, "jar", ServiceUtils.toJarURL(dep, kieWbMavenLocation).toExternalForm(), kieWbUser, kieWbPassword);
			byte[] pomContent = readUrl(dep, "pom", ServiceUtils.toPomURL(dep, kieWbMavenLocation).toExternalForm(), kieWbUser, kieWbPassword);
			if (!dep.getVersion().endsWith("-SNAPSHOT")) {
				deleteFile(dep);
			}
			AFReleaseId rid = new AFReleaseIdImpl(dep.getExternalForm());
			if (jarContent.length == 0 && pomContent.length > 0) {
				//just install the pom file manually
				Dependency dep2 = new Dependency( rid.getGroupId(), rid.getArtifactId(), rid.getVersion() );
				String jarLocalFile = ServiceUtils.toJarPath(dep2, false).getFile();
				String pomLocalFile = jarLocalFile.replace(".jar", ".pom");
				File file = new File(pomLocalFile);
				FileUtils.writeByteArrayToFile(file, pomContent);
				MavenRepository.getMavenRepository().resolveArtifact(rid);
			} else {
				MavenJarResolver.resolveDependencies(dep.getGroupId(), dep.getArtifactId(), dep.getVersion());
				MavenRepository.getMavenRepository().installArtifact(
						rid, jarContent, pomContent);
			}
		} catch (IOException | URISyntaxException e) {
			throw new BaseException("Couldn't read JAR or POM from URL: " + kieWbMavenLocation, e);
		}
	}

	private void deleteFile(Dependency dep) throws IOException, URISyntaxException {
		try {
			Files.delete(Paths.get(ServiceUtils.toJarPath(dep).toURI()));
		} catch (RuntimeException e) {
			//this happens when the file is not present, and we need to
			//ignore it and carry on.
		}
	}
	
	protected File bytesToFile(AFReleaseId releaseId, byte[] bytes, String extension ) {
		File file = null;
		try {
			file = File.createTempFile(releaseId.getArtifactId() + "-" + releaseId.getVersion(), extension );
		} catch (IOException e) {
			throw new BaseException( e );
		}
		try(FileOutputStream fos = new FileOutputStream( file )) {
			fos.write( bytes );
		} catch ( IOException e ) {
			throw new BaseException( e );
		}
		return file;
	}

	public void restartService(Dependency dep) {
		restartService(dep, true);
	}
	
	public void restartService(Dependency dep, boolean store) {
		register(new GenericKieBasedService(dep), store);
		Set<String> services = servicesWithDependency(dep);
		for (String serviceName : services) {
			restartService(serviceName);
		}
	}
	
	public void restartService(String serviceName) {
		GenericService service = registry.get(serviceName);
		if (service instanceof GenericTempService) {
			String releaseId = service.getDependency().getExternalForm();
			String customer = service.getDefaultCustomer();
			service = new GenericKieBasedService(releaseId, customer);
		}
		unregister(serviceName);
		((GenericKieBasedService) service).restart();
		register(service);
	}
	
	public Set<String> servicesWithDependency(Dependency dep) {
		return registry.entrySet().stream().
			filter(e -> e.getValue() instanceof GenericKieBasedService).
			filter(e -> ((GenericKieBasedService) e.getValue()).getKieDependencies().contains(dep)).
			map(e -> e.getKey()).collect(Collectors.toSet());
	}

	private byte[] readUrl(Dependency dep, String type, String url, String user, String password) throws IOException {
		if (dep.getVersion().endsWith("SNAPSHOT")) {
			url = url.replace("/" + dep.getArtifactId() + "-" + dep.getVersion() + "." + type, "/maven-metadata.xml");
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("GET");
			applyAuth(user, password, conn, "application/xml");
			String timestampBuildNumber = getLastTimestamp(IOUtils.toByteArray(conn.getInputStream()));
			url = url.replaceAll("/maven-metadata.xml", "/" + dep.getArtifactId() + "-" +
					dep.getVersion().replace("-SNAPSHOT", "-") + timestampBuildNumber + "." + type);
		}
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setRequestMethod("GET");
		applyAuth(user, password, conn, "application/xml");
		return conn.getResponseCode() == 200 ? IOUtils.toByteArray(conn.getInputStream()) : new byte[0];
	}

	private String getLastTimestamp(byte[] xmlByteArray) {
		String xml = new String(xmlByteArray);
		int start1 = xml.indexOf("<timestamp>") + "<timestamp>".length();
		int end1 = xml.indexOf("</timestamp>", start1);
		int start2 = xml.indexOf("<buildNumber>") + "<buildNumber>".length();
		int end2 = xml.indexOf("</buildNumber>", start2);
		return xml.substring(start1, end1) + "-" + xml.substring(start2, end2);
	}

	private void applyAuth(String user, String password, HttpURLConnection conn, String contentType) {
		String userCredentials = user + ":" + password;
		String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userCredentials.getBytes());
		conn.setRequestProperty ("Authorization", basicAuth);
		conn.setRequestProperty("Content-Type", contentType);
		conn.setRequestProperty("Accept", contentType);
	}
	
	public String getSpace(Dependency dep) {
		String space = null;
		try {
			space = getSpaceFromJar(dep);
		} catch (IOException e) {
			LOG.error("Problem reading space from JAR: " + e.getMessage());
		}
		try {
			if (space == null) {
				space = System.getProperty("elimu.default.customer");
			} 
			if (space == null) {
				space = getSpaceFromWb(dep);
			}
		} catch (IOException e) {
			LOG.error("Problem reading space from Workbench", e);
		}
		return space;
	}

	public String getSpaceFromWb(Dependency dep) throws IOException {
		String url = UriComponentsBuilder.fromUriString(ServiceUtils.getKieWbLocation()).
				path("/rest/spaces/").build().toUriString();
		LOG.info("SPACE: Getting spaces from " + url);
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setRequestMethod("GET");
		applyAuth(ServiceUtils.getKieWbUser(), ServiceUtils.getKieWbPassword(), conn, "application/json");
		String spaceName=extractSpace(dep, conn.getInputStream());
		if (spaceName == null) {
			return null; //for tests only
		}
		return spaceName.contains("_") ? spaceName.split("_")[0] : spaceName;
	}

	public String getSpaceFromJar(Dependency dep) throws IOException {
		Properties props = ServiceUtils.getConfig(dep, AppContextUtils.getInstance().getProfileName());
		return props.getProperty("kie.project.space");
	}

	private String extractSpace(Dependency dep, InputStream inputStream) throws IOException {
		JsonNode json = new ObjectMapper().readTree(inputStream);
		for (int spaceIndex = 0; spaceIndex < json.size(); spaceIndex++) {
			JsonNode space = json.get(spaceIndex);
			String spaceName = space.get("name").asText();
			JsonNode projects = space.get("projects");
			for (int projIndex = 0; projIndex < projects.size(); projIndex++) {
				JsonNode project = projects.get(projIndex);
				if (project.get("groupId").asText().equals(dep.getGroupId()) &&
					project.get("name").asText().equals(dep.getArtifactId())) {
					return spaceName;
				}
			}
		}
		LOG.warn("Couldn't find space for dependency " + dep.getExternalForm());
		return null;
	}
}
