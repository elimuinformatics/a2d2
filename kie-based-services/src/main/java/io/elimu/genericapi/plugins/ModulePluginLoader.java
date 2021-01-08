package io.elimu.genericapi.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.elimu.a2d2.cdsmodel.Dependency;
import io.elimu.genericapi.service.GenericServiceConfigException;

public interface ModulePluginLoader {

	static final Logger log = LoggerFactory.getLogger(ModulePluginLoader.class);
	
	public static ModulePluginLoader get(ClassLoader cloader, String type) throws GenericServiceConfigException {
		String className = ModulePluginLoader.class.getPackage().getName() + "."
			+ type + "PluginLoader";
		try {
			Class<?> clz = cloader.loadClass(className);
			Object obj = clz.newInstance();
			getDeclaredMethod(type, className, clz);
			return (ModulePluginLoader) obj;
		} catch (Exception e) {
			throw new GenericServiceConfigException("Problem initializing ModulePluginLoader of type " + type, e);
		}
	}

	public static void getDeclaredMethod(String type, String className, Class<?> clz) {
		try {
			clz.getDeclaredMethod("setClassLoader", ClassLoader.class);
		} catch (NoSuchMethodException e) {
			log.warn("no setClassLoader(ClassLoader) method found on class " + className + ". Skipping ClassLoader setting for module loader " + type);
		}
	}
	
	void process(Dependency dep);
}
