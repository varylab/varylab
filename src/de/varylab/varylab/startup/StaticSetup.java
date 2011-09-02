package de.varylab.varylab.startup;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import sun.misc.JarFilter;
import de.varylab.varylab.VaryLab;

public class StaticSetup {

	private static ClassLoader
		varylabLoader = null;

	private static class VarlabClassLoader extends URLClassLoader {
		
		public VarlabClassLoader(ClassLoader parent) {
			super(new URL[] {}, parent);
		}
		
	}
	
	static {
		ClassLoader sysLoader = VaryLab.class.getClassLoader();
		varylabLoader = new VarlabClassLoader(sysLoader);
		Thread.currentThread().setContextClassLoader(varylabLoader);
	}
	
	
	private static void addURL(URL url) throws Exception {
		Method addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		addURLMethod.setAccessible(true);
		addURLMethod.invoke(varylabLoader, url);
		addURLMethod.setAccessible(false);
	}
	
	
	public static void includePluginJars() {
		File pluginFolder = new File("plugin");
		if (!pluginFolder.exists()) {
			pluginFolder.mkdir();
		}
		String[] jars = pluginFolder.list(new JarFilter());
		for (String jarName : jars) {
			String jarPath = pluginFolder.getAbsolutePath() + "/" + jarName;
			File f = new File(jarPath);
			try {
				URL url = f.toURI().toURL();
				addURL(url);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void includeLibraryJars() {
		File libraryFolder = new File("bin");
		if (!libraryFolder.exists()) return;
		String[] jars = libraryFolder.list(new JarFilter());
		for (String jarName : jars) {
			String jarPath = libraryFolder.getAbsolutePath() + "/" + jarName;
			File f = new File(jarPath);
			try {
				URL url = f.toURI().toURL();
				addURL(url);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
