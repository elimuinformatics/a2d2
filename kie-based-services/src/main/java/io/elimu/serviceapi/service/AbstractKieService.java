package io.elimu.serviceapi.service;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.jbpm.process.core.timer.impl.QuartzSchedulerService;
import org.jbpm.runtime.manager.impl.SimpleRuntimeEnvironment;
import org.jbpm.runtime.manager.impl.mapper.JPAMapper;
import org.jbpm.runtime.manager.impl.tx.NoOpTransactionManager;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.io.Resource;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.conf.PersistenceMode;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.deploy.DeploymentDescriptorIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import io.elimu.a2d2.cdsmodel.Dependency;
import io.elimu.a2d2.exception.BaseException;
import io.elimu.a2d2.process.ServiceUtils;
import io.elimu.genericapi.service.GenericServiceConfigException;
import io.elimu.genericapi.service.MavenJarResolver;
import io.elimu.genericapi.service.RunningServices;

public abstract class AbstractKieService {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractKieService.class);
	private static PoolingDataSource pds = null;
	private static final Object lock = new Object();
	private static final int TRANSACTION_TIMEOUT = Integer.parseInt(System.getProperty("bitronix.tm.timer.defaultTransactionTimeout", "600"));
	
	private RuntimeManager manager;
	private KieBase kbase;
	private String kbaseName;
	private KieContainer kContainer;
	private Dependency dep;
	private List<Dependency> kieDependencies = new ArrayList<>();
	private URLClassLoader classLoader;
	private String status = "STOPPED";
	private String defaultCustomer = null;
	private String serviceCategory = null;
	private List<URL> deps = new ArrayList<>();

	public AbstractKieService() {
	}
	
	public AbstractKieService(String releaseId) {
		this(releaseId, null);
	}
	
	public AbstractKieService(Dependency dep) {
		this(dep, null);
	}
	
	public AbstractKieService(String releaseId, String defaultCustomer) {
		this(extractDependency(releaseId), defaultCustomer);
	}
	
	public AbstractKieService(Dependency dep, String defaultCustomer) {
		this.dep = dep;
		this.defaultCustomer = defaultCustomer;
		this.init();
	}

	protected static Dependency extractDependency(String releaseId) {
		if (releaseId == null || releaseId.split(":").length < 3) {
			throw new IllegalArgumentException("releaseId '" + releaseId + "' is not a valid release ID");
		}
		String[] parts = releaseId.split(":");
		return new Dependency(parts[0], parts[1], parts[2]);
	}
	
	protected URL[] getJarDependencies(Dependency dep) throws IOException {
		List<URL> retval = new ArrayList<>();
		for (Dependency subDep : MavenJarResolver.resolveDependencies(dep.getGroupId(), dep.getArtifactId(), dep.getVersion())) {
			URL subJar = ServiceUtils.toJarPath(subDep);
			retval.add(subJar);
			LOG.info("- " + dep.getArtifactId() + " dependency: " + subJar);
			this.deps.add(subJar);
		}
		return retval.toArray(new URL[retval.size()]);
	}

	protected List<Resource> getKieDependencies(Dependency dep) throws IOException {
		List<Resource> retval = new ArrayList<>();
		for (Dependency subDep : MavenJarResolver.resolveDependencies(dep.getGroupId(), dep.getArtifactId(), dep.getVersion())) {
			URL subJar = ServiceUtils.toJarPath(subDep);
			if (ServiceUtils.isKieModule(subJar)) {
				retval.add(KieServices.get().getResources().newUrlResource(subJar));
			}
		}
		return retval;
	}

	protected void init() {
		System.setProperty("org.jbpm.runtime.manager.ppi.lock", "false");
		ReleaseId kReleaseId = KieServices.get().newReleaseId(
				dep.getGroupId(), dep.getArtifactId(), dep.getVersion());
		//MavenRepository mrepo = MavenRepository.getMavenRepository();
		//mrepo.resolveArtifact(kReleaseId);//just in case it is not in the local M2 repo yet
		KieRepository krepo = KieServices.get().getRepository();
		URL jarPath = null;
		try {
			jarPath = ServiceUtils.toJarPath(dep);
		} catch (NullPointerException e) {
			RunningServices.getInstance().recreateDependency(dep);
			jarPath = ServiceUtils.toJarPath(dep);
		}
		try {
			List<Resource> res = getKieDependencies(dep);
			krepo.removeKieModule(kReleaseId);
			KieModule kModule = krepo.addKieModule(KieServices.get().getResources().newUrlResource(jarPath), res.toArray(new Resource[res.size()]));
			this.classLoader = new URLClassLoader(getJarDependencies(dep), getClass().getClassLoader());
			this.kContainer = KieServices.get().newKieContainer(kReleaseId, classLoader);
			KieModuleModel kmodule = ((InternalKieModule) kModule).getKieModuleModel();
			String kieBaseName = null;
			String defaultKbaseName = null;
			for (Map.Entry<String, KieBaseModel> kbaseMod : kmodule.getKieBaseModels().entrySet()) {
				if (defaultKbaseName == null) {
					defaultKbaseName = kbaseMod.getValue().getName();
				}
				if (kbaseMod.getValue().isDefault()) {
					kieBaseName = kbaseMod.getValue().getName();
				}
			}
			this.kbase = kContainer.getKieBase(kieBaseName == null ? defaultKbaseName : kieBaseName);
			this.kbaseName = kieBaseName;
			DeploymentDescriptor deployDescriptor = DeploymentDescriptorIO.fromXml(ServiceUtils.readEntry(jarPath, "META-INF/kie-deployment-descriptor.xml"));
			this.manager = RuntimeManagerRegistry.get().getManager(dep.getArtifactId());
			if (manager != null) {
				manager.close();
				manager = null;
			}
			boolean persistent = deployDescriptor.getPersistenceMode() == PersistenceMode.JPA;
			EntityManagerFactory emf = null;
			Object tm = new NoOpTransactionManager();
			Object tsr = null;
			if (persistent) {
				emf = createEmf(deployDescriptor.getPersistenceUnit());
				tm = TransactionManagerServices.getTransactionManager();
				tsr = TransactionManagerServices.getTransactionSynchronizationRegistry();
			}
			RuntimeEnvironment renv = RuntimeEnvironmentBuilder.Factory.get().newEmptyBuilder().
				persistence(persistent).
				addEnvironmentEntry(EnvironmentName.ENTITY_MANAGER_FACTORY, emf).
				addEnvironmentEntry(EnvironmentName.TRANSACTION_MANAGER, tm).
				addEnvironmentEntry(EnvironmentName.TRANSACTION, tm).
				addEnvironmentEntry(EnvironmentName.TRANSACTION_SYNCHRONIZATION_REGISTRY, tsr).
				addEnvironmentEntry("ExecutorService", new InMemoryExecutorService(dep.getArtifactId())).
				schedulerService(new QuartzSchedulerService()).
				registerableItemsFactory(new ConfigRegisterableItemsFactory(kContainer, dep.getArtifactId(), deployDescriptor, shouldLogExecution(), getConfig())).
				knowledgeBase(this.kbase).
				userGroupCallback(new FreeUserGroupCallback()).
				classLoader(kContainer.getClassLoader()).
				entityManagerFactory(emf).
				get();
			if (persistent) {
				((SimpleRuntimeEnvironment) renv).setMapper(new JPAMapper(emf));
			}
			this.manager = RuntimeManagerFactory.Factory.get(kContainer.getClassLoader()).newPerProcessInstanceRuntimeManager(renv, dep.getArtifactId());
			status = "STARTED";
		} catch (IOException e) {
			throw new GenericServiceConfigException("Problem determining dependencies of service " + getId(), e);
		}
	}

	protected String extractProcessId(URL jarPath, Properties properties, String propertyKey) {
		if (properties.containsKey(propertyKey)) {
			return properties.getProperty(propertyKey);
		} else if (properties.containsKey(propertyKey.toLowerCase())) {
			return properties.getProperty(propertyKey.toLowerCase());
		} else {
			Set<String> names = ServiceUtils.findEntriesOfType(jarPath, "bpmn", "bpmn2");
			if (names.size() == 1) {
				try {
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
					dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(ServiceUtils.readEntry(jarPath, names.iterator().next()));
					doc.getDocumentElement().normalize();
					NodeList list = doc.getElementsByTagName("bpmn2:process");
					Element node = (Element) list.item(0);
					return node.getAttribute("id");
				} catch (Exception e) {
					throw new BaseException("Cannot obtain process ID", e);
				}
			} else {
				//No processId
				return null;
			}
		}
	}

	protected abstract Properties getConfig();
	
	protected abstract boolean shouldLogExecution();

	public Dependency getDependency() {
		return dep;
	}

	public List<Dependency> getKieDependencies() {
		return Collections.unmodifiableList(kieDependencies);
	}
	
	public String getStatus() {
		return status;
	}

	public KieBase getKieBase() {
		return kbase;
	}
	
	public String getKieBaseName() {
		return kbaseName;
	}
	
	public String getDefaultCustomer() {
		return defaultCustomer;
	}
	
	public void setDefaultCustomer(String defaultCustomer) {
		this.defaultCustomer = defaultCustomer;
	}
	
	public String getServiceCategory() {
		return serviceCategory;
	}

	public void setServiceCategory(String serviceCategory) {
		this.serviceCategory = serviceCategory;
	}

	public void stop() {
		this.manager.close();
		this.manager = null;
		this.kContainer.dispose();
		this.kContainer = null;
		this.kbase = null;
		this.classLoader = null;
		this.status  = "STOPPED";
	}
	
	protected KieContainer getKieContainer() {
		return kContainer;
	}

	public String getId() {
		return dep.getArtifactId();
	}
	
	public URLClassLoader getClassLoader() {
		return classLoader;
	}

	public EntityManagerFactory createEmf(String persistenceUnitName) {
		if (pds == null) {
			synchronized (lock) {
				System.setProperty("java.naming.factory.initial", "bitronix.tm.jndi.BitronixInitialContextFactory");
				pds = new PoolingDataSource();
				// The name must match what's in the persistence.xml!
				pds.setUniqueName("jdbc/jbpm-ds");
				pds.setClassName(System.getProperty("cds.kie.db.className"));
				//pds.getDriverProperties().put("url", "jdbc:postgresql://localhost:5432/a2d2");
				for (String key : System.getProperties().stringPropertyNames()) {
					if (key.startsWith("cds.kie.db.") && !"cds.kie.db.className".equals(key)) {
						pds.getDriverProperties().setProperty(key.replace("cds.kie.db.", ""), System.getProperty(key));
					}
				}
				pds.setMaxPoolSize(10);
				pds.setAllowLocalTransactions(true);
				pds.setMinPoolSize(3);
				pds.setApplyTransactionTimeout(false);
				pds.setEnableJdbc4ConnectionTest(true);
				pds.setShareTransactionConnections(true);
				pds.setAcquisitionTimeout(TRANSACTION_TIMEOUT);
				pds.init();
			}
		}
		Map<String, Object> overrideProperties = new HashMap<>();
		overrideProperties.put("hibernate.connection.datasource", pds);
		return Persistence.createEntityManagerFactory(persistenceUnitName, overrideProperties);
	}
	
	public RuntimeManager getManager() {
		return manager;
	}
	
	public void restart() {
		stop();
		status = "RESTARING";
		init();
	}

	public String getJarTimestamp() {
		return createTimestamp(ServiceUtils.toJarPath(this.dep));
	}

	private String createTimestamp(URL fileUrl) {
		String timestamp = "NOT_FOUND";
		try {
			timestamp = String.valueOf(fileUrl.openConnection().getLastModified());
		} catch (IOException ignore) {
			// no-op
		}
		return fileUrl.toExternalForm() + ";" + timestamp;
	}

	public String getDependenciesTimestamp() {
		StringBuilder sb = new StringBuilder();
		for (URL subDep : this.deps) {
			sb.append(createTimestamp(subDep)).append("; ");
		}
		return sb.toString();
	}
}
