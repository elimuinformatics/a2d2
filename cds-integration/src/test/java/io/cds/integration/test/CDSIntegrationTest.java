package io.cds.integration.test;

import static io.restassured.RestAssured.get;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;

import org.apache.maven.model.Dependency;
import org.awaitility.Duration;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;

public class CDSIntegrationTest {

	public static ProcessBuilder builder=null;
	
	public static String baseurl = "";
	
	public static Process process=null;
	
	@BeforeClass
	public static void setUp() throws Exception {
		Properties prop = new Properties();
		prop.load(CDSIntegrationTest.class.getResourceAsStream("/cds-test.properties"));
		for (Object key : prop.keySet()) {
			System.setProperty(String.valueOf(key), prop.getProperty(String.valueOf(key)));
		}
		baseurl=System.getProperty("test.base.url");
		Dependency dep = new Dependency();
		dep.setArtifactId("a2d2-api");
		dep.setGroupId("io/elimu/a2d2");
		dep.setVersion("0.0.1-SNAPSHOT");
		URL path = warPath(dep, false);
		builder = new ProcessBuilder("java", "-jar", path.getPath());
		System.out.println("CDSIntegrationTest Process Start");
		builder.redirectErrorStream(true);
		process = builder.start();
		watch(process);
	}

	@Test
	public void testA2d2Api_cdsservice() throws IOException {

	     		final String api = System.getProperty("test.cds.discoveryserviceurl");
  
			final Callable<Integer> statusCode = new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					try {
						URL url = new URL(baseurl+api);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
						connection.setRequestMethod("GET");
						connection.connect();
						return connection.getResponseCode();
					} catch (ConnectException e) {
						return 404;
					}
				}
			};
			await().atMost(Duration.TWO_MINUTES).until(statusCode, equalTo(200));
			
			get(api).then().defaultParser(Parser.JSON).assertThat().body(containsString("services"));
			
			Response response = get(api).then().contentType(ContentType.JSON).extract().response();
			List<Objects> serviceList = response.jsonPath().getList("services");
	        Assert.assertTrue(serviceList.size()>=1);
	}
	public static URL warPath(Dependency dep, boolean create) {
		char s = File.separatorChar;
		String groupIdPath = null;
		try {
			groupIdPath = dep.getGroupId().replaceAll("\\.", Matcher.quoteReplacement(File.separator));
		} catch (Exception e) {
			groupIdPath = dep.getGroupId().replaceAll("\\\\.", Matcher.quoteReplacement(File.separator));
		}
		String jarPath = new StringBuilder(System.getProperty("user.home")).append(s).append(".m2").append(s)
				.append("repository").append(s).append(groupIdPath).append(s).append(dep.getArtifactId()).append(s)
				.append(dep.getVersion()).append(s).append(dep.getArtifactId()).append('-').append(dep.getVersion())
				.append(".war").toString();
		File f = new File(jarPath);
		try {
			if (f.exists()) {
				return f.toURI().toURL();
			} else if (create) {
				byte[] data = null;
				Files.write(Paths.get(f.toURI()), data, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			}
			return f.toURI().toURL();
		} catch (IOException e) {
			throw new RuntimeException("Couldn't write non-existing file " + f, e);
		}
	}

	private static void watch(final Process process) {
		new Thread() {
			public void run() {
				BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line = null;
				try {
					while ((line = input.readLine()) != null) {
						System.out.println(line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	
	@AfterClass
	public static void cleanUp() {
		if (process != null) {
			process.destroy();
			System.out.println("Process Destroyed");
		}
		System.out.println("CDSIntegrationTest Clean Up successfully");
	}
}
