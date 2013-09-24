package de.varylab.varylab.startup;

import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import sun.misc.JarFilter;
import de.varylab.varylab.startup.definitions.VaryLabUltimate;

public class StaticSetup {

	private static Logger
		log = Logger.getLogger(StaticSetup.class.getName());
	private static ClassLoader
		varylabLoader = null;

	static {
		varylabLoader = VaryLabUltimate.class.getClassLoader();
	}
	
	
	public static void initLogging() {
		LogManager lm = LogManager.getLogManager();
		File localConf = new File("logging.properties");
		File localCustomConf = new File("logcustom.properties");
		String fileName = null;
		InputStream confIn = null;
		if (localConf.exists() | localCustomConf.exists()) {
			File confFile = null;
			if (localCustomConf.exists()) {
				confFile = localCustomConf;
			} else {
				confFile = localConf;
			}
			fileName = confFile.getAbsolutePath();
			try {
				confIn = new FileInputStream(confFile);
			} catch (FileNotFoundException e) {}
		} else {
			fileName = StaticSetup.class.getResource("logging.properties").getFile();
			confIn = StaticSetup.class.getResourceAsStream("logging.properties");
		}
		assert confIn != null;
		try {
			lm.readConfiguration(confIn);
		} catch (Exception e) {
			System.out.println(e);
		}
		log = Logger.getLogger(StaticSetup.class.getName());
		log.info("initialized logging system from " + fileName);
	}
	
	
	public static List<Image> getMainIconList() {
		List<Image> iconList = new LinkedList<Image>();
		iconList.add(SplashImageHook.getImage("icon_16.png"));
		iconList.add(SplashImageHook.getImage("icon_24.png"));
		iconList.add(SplashImageHook.getImage("icon_32.png"));
		iconList.add(SplashImageHook.getImage("icon_48.png"));
		iconList.add(SplashImageHook.getImage("icon_64.png"));
		iconList.add(SplashImageHook.getImage("icon_128.png"));
		iconList.add(SplashImageHook.getImage("icon_256.png"));
		iconList.add(SplashImageHook.getImage("icon_512.png"));
		iconList.add(SplashImageHook.getImage("icon_1024.png"));
		return iconList;
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
