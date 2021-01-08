package io.elimu.genericapi.plugins;

import java.io.InputStream; 
import java.net.URL;

import org.appformer.maven.support.AFReleaseId;
import org.appformer.maven.support.PomModel;

import io.elimu.a2d2.cdsmodel.Dependency;
import io.elimu.a2d2.process.ServiceUtils;
import io.elimu.genericapi.service.RunningServices;

public abstract class TemplatePluginLoader implements ModulePluginLoader {

	private final String[] fileTypes;

	public TemplatePluginLoader(String... fileTypes) {
		this.fileTypes = fileTypes;
	}
	
	@Override
	public void process(Dependency dep) {
		URL jarPath = ServiceUtils.toJarPath(dep);
		for (String fileType : this.fileTypes) {
			ServiceUtils.loadTemplateEntries(jarPath, fileType, dep.getArtifactId());
		}
		
		InputStream pomXml = ServiceUtils.readEntry(jarPath, "META-INF/maven/" + dep.getGroupId() + "/" + dep.getArtifactId() + "/pom.xml");
		PomModel pomModel = RunningServices.getInstance().parseModel(dep, pomXml);
		for (AFReleaseId relId : pomModel.getDependencies()) {
			Dependency subdep = new Dependency(relId.getGroupId(), relId.getArtifactId(), relId.getVersion());
			URL subJar = ServiceUtils.toJarPath(subdep);
			for (String fileType : this.fileTypes) {
				ServiceUtils.loadTemplateEntries(subJar, fileType, subdep.getArtifactId());
			}
		}
	}

}
