package de.varylab.varylab.startup.definitions;

import java.awt.Image;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import de.jreality.plugin.basic.ConsolePlugin;
import de.jreality.plugin.job.JobMonitorPlugin;
import de.jtem.jrworkspace.plugin.Plugin;
import de.varylab.varylab.plugin.VarylabMain;
import de.varylab.varylab.startup.SplashImageHook;
import de.varylab.varylab.startup.VarylabSplashScreen;
import de.varylab.varylab.startup.VarylabStartupDefinition;

public class VaryLabService extends VarylabStartupDefinition {

	private Logger
		log = Logger.getLogger(VaryLabService.class.getName());
	private VarylabSplashScreen
		splash = null;
	private List<String>
		pluginClassNames = null;
	private String
		projectId = "default";
	private static File
		propertiesFolder = null;
	
	static {
		// create varylab preferences folder
		String userHome = System.getProperty("user.home");
		propertiesFolder = new File(userHome + "/.varylab");
		propertiesFolder.mkdirs();
	}
	
	public VaryLabService(List<String> plugins, String projectId) {
		super();
		this.pluginClassNames = plugins;
		this.projectId = projectId;
	}

	@Override
	public String getApplicationName() {
		return "VaryLab";
	}
	
	@Override
	public String getPropertyFileName() {
		String prosFileName = "project_" + projectId + ".xml";
		File propsFile = new File(propertiesFolder, prosFileName);
		checkPropertyFile(propsFile);
		return propsFile.getAbsolutePath();
	}
	
	private void checkPropertyFile(File propsFile) {
		if (propsFile.exists()) {
			return;
		}
		log.info("writing default property file to " + propsFile);
		try {
			InputStream in = getClass().getResourceAsStream("VaryLabDefault.xml");
			Reader propsReader = new InputStreamReader(in);
			FileWriter propsWriter = new FileWriter(propsFile);
			char[] buffer = new char[1024];
			int numRead;
			while ((numRead = propsReader.read(buffer)) >= 0) {
				propsWriter.write(buffer, 0, numRead);
			}
			propsWriter.close();
			propsReader.close();
		} catch (Exception e) {
			log.warning("error writing default property file " + e);
		}
	}
	
	@Override
	public VarylabSplashScreen getSplashScreen() {
		if (splash == null) {
			Image lowRes = SplashImageHook.getImage("varylab_ultimate_low_res.png");
			Image highRes = SplashImageHook.getImage("varylab_ultimate_high_res.png");
			splash = new VarylabSplashScreen(lowRes, highRes);
		}
		return splash;
	}
	
	@Override
	public void getPlugins(Set<Class<? extends Plugin>> classes, Set<Plugin> instances) {
		classes.add(VarylabMain.class);
		classes.add(ConsolePlugin.class);
		classes.add(JobMonitorPlugin.class);
		
		// load custom classes
		for (String className : pluginClassNames) {
			try {
				Class<?> clazz = Class.forName(className);
				Object obj = clazz.newInstance();
				if (obj instanceof Plugin) {
					instances.add((Plugin)obj);
				}
			} catch (Exception e) {
				log.warning("could not load plug-in class \"" + className + "\": " + e);
			}
		}
	}

	
	public static void main(String[] args) throws Exception {
		// read plug-ins
		List<String> plugins = new LinkedList<String>();
		if (args.length >= 1) {
			String pluginNames = args[0];
			for (String name : pluginNames.split(" ")) {
				if (name.trim().isEmpty()) continue;
				plugins.add(name.trim());
			}
		}
		// get project id
		String projectId = "default";
		if (args.length >= 2) {
			projectId = args[1];
		}
		
		new VaryLabService(plugins, projectId).startup();
	}

}
