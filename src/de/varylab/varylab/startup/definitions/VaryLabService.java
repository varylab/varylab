package de.varylab.varylab.startup.definitions;

import java.awt.Image;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import de.jtem.halfedgetools.plugin.widget.ContextMenuWidget;
import de.jtem.halfedgetools.plugin.widget.MarqueeWidget;
import de.jtem.jrworkspace.plugin.Plugin;
import de.varylab.varylab.plugin.VarylabMain;
import de.varylab.varylab.plugin.optimization.IterationProtocolPanel;
import de.varylab.varylab.plugin.optimization.OptimizationPanel;
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
	
	static {
		// create varylab preferences folder
		new File(".varylab").mkdirs();
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
		return ".varylab/project_" + projectId + ".xml";
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
		classes.add(IterationProtocolPanel.class);
		classes.add(OptimizationPanel.class);
		classes.add(MarqueeWidget.class);
		classes.add(ContextMenuWidget.class);
		
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
