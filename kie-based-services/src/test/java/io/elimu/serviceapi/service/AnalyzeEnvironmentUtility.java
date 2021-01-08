package io.elimu.serviceapi.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.elimu.a2d2.cdsmodel.Dependency;
import io.elimu.a2d2.process.ServiceUtils;
import io.elimu.genericapi.service.RunningServices;

public class AnalyzeEnvironmentUtility {

	@SuppressWarnings("all")
	public static void main(String[] args) throws Exception {
		String baseURL = System.getProperty("baseURL");
		if (baseURL == null) {
			System.out.println("Usage:");
			System.out.println(" -Denv=<ENV>: the environment id (dev, qa, staging, demo)");
			System.out.println(" -Dmaven.repo.location=<URL>: the workbench or maven location");
			System.out.println(" -Dmaven.repo.user=<USR>: the workbench or maven user");
			System.out.println(" -Dmaven.repo.pwd=<PASS>: the workbench or maven password");
			System.exit(-1);
		}
		String url = baseURL + "/kie-services/server/containers";
		byte[] content = readUrl(url);
		JsonNode node = new ObjectMapper().readTree(content);
		JsonNode containers = node.get("result").get("kie-containers").get("kie-container");
		List<Dependency> services = new ArrayList<>();
		for (int index = 0; index < containers.size(); index++) {
			JsonNode c = containers.get(index);
			JsonNode r = c.get("release-id");
			if (r == null || r.isNull()) {
				continue;
			}
			String groupId = r.get("group-id").asText();
			String artifactId = r.get("artifact-id").asText();
			String version = r.get("version").asText();
			Dependency dep = new Dependency(groupId, artifactId, version);
			services.add(dep);
		}
		System.out.println("We have " + services.size() + " services deployed in " + baseURL);
		for (Dependency dep : services) {
			RunningServices.getInstance().downloadDependency(dep);
			if (depContains(dep, "service.dsl")) {
				System.out.println("dep " + dep + " is  a new service");
			} else if (depContains(dep, "cds.dsl")) {
				System.err.println("dep " + dep + " is  an old service");
			}
		}
	}
	
	private static boolean depContains(Dependency dep, String filePath) throws IOException {
		try (JarFile jar = new JarFile(ServiceUtils.toJarPath(dep).getFile())) {
			return jar.getJarEntry(filePath) != null;
		}
	}
	
	private static byte[] readUrl(String url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Accept", "application/json");
		return IOUtils.toByteArray(conn.getInputStream());
	}
}
