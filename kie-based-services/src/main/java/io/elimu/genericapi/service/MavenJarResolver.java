package io.elimu.genericapi.service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.appformer.maven.integration.Aether;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.elimu.a2d2.process.ServiceUtils;

public class MavenJarResolver {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MavenJarResolver.class);
	private static final String REPO_PATH = "/root/.m2/repository";
	private static final String DEFAULT = "default";
	
	private static final List<RemoteRepository> REPOS = new ArrayList<>();

	private MavenJarResolver() {
		throw new IllegalStateException("Utility class");
	}
	
	public static List<io.elimu.a2d2.cdsmodel.Dependency> resolveDependencies(String groupId, String artifactId, String version) throws IOException {
		synchronized(REPOS) {
			if (REPOS.isEmpty()) {
				if (!"true".equalsIgnoreCase(String.valueOf(System.getProperty("kie.maven.offline.force")))) {
					REPOS.add(
						new RemoteRepository.Builder("private-repo", DEFAULT, ServiceUtils.getMavenLocation()).
							setAuthentication(
								new AuthenticationBuilder().addUsername(ServiceUtils.getMavenUser()).
								addPassword(ServiceUtils.getMavenPassword()).build()).
							setReleasePolicy(new RepositoryPolicy(true, RepositoryPolicy.UPDATE_POLICY_NEVER, 
									RepositoryPolicy.CHECKSUM_POLICY_IGNORE)).
							setSnapshotPolicy(new RepositoryPolicy(true, RepositoryPolicy.UPDATE_POLICY_DAILY, 
									RepositoryPolicy.CHECKSUM_POLICY_IGNORE)).
							build());
				}
				else {
						LOGGER.debug("Looking into M2 Repository for loading kjars");
						Path path = Paths.get(Optional.ofNullable(System.getenv("M2_REPO")).orElse(REPO_PATH));
						RemoteRepository local = new RemoteRepository.Builder("local", DEFAULT, path.toFile().toURI().toString()).build();
						REPOS.add (local);		
				}
				REPOS.add(
					new RemoteRepository.Builder("central-repo", DEFAULT, "https://repo1.maven.org/maven2/").
						setReleasePolicy(new RepositoryPolicy(true, RepositoryPolicy.UPDATE_POLICY_NEVER, 
								RepositoryPolicy.CHECKSUM_POLICY_IGNORE)).
						setSnapshotPolicy(new RepositoryPolicy(true, RepositoryPolicy.UPDATE_POLICY_DAILY, 
								RepositoryPolicy.CHECKSUM_POLICY_IGNORE)).
						build());
			}
		}
		try {
			final DependencyResult result = Aether.getAether().getSystem().resolveDependencies(Aether.getAether().getSession(), new DependencyRequest(
					new CollectRequest(
							new Dependency(new DefaultArtifact( groupId, artifactId, "jar", version ),"compile"), 
							REPOS
					), 
					new DependencyFilter() {
						@Override
						public boolean accept(DependencyNode node, List<DependencyNode> parents) {
							return true; //download everything needed
						}
					}
			));
			List<io.elimu.a2d2.cdsmodel.Dependency> retval = new ArrayList<>(result.getArtifactResults().size());
			for (ArtifactResult res : result.getArtifactResults()) {
				/*LOGGER.info(String.format("Dependency detected %s:%s:%s...", 
						res.getArtifact().getGroupId(), 
						res.getArtifact().getArtifactId(), 
						res.getArtifact().getVersion()));*/
				retval.add(new io.elimu.a2d2.cdsmodel.Dependency(
						res.getArtifact().getGroupId(), 
						res.getArtifact().getArtifactId(), 
						res.getArtifact().getVersion()));
			}
			return retval;
		} catch (DependencyResolutionException e) {
			throw new IOException(String.format("Cannot resolve %s:%s:%s, %s", groupId, artifactId, version, e.getMessage()), e);
		}
	}
}
