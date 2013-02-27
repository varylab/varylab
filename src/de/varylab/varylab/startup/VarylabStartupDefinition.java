package de.varylab.varylab.startup;

import java.awt.EventQueue;
import java.util.HashSet;
import java.util.Set;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.GraphiteAquaSkin;

import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.View;
import de.jreality.util.NativePathUtility;
import de.jreality.util.Secure;
import de.jtem.halfedgetools.JRHalfedgeViewer;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jtao.Tao;
import de.varylab.varylab.plugin.ui.image.ImageHook;

public abstract class VarylabStartupDefinition {

	private VarylabSplashScreen
		splash = null;
	
	public abstract void getPlugins(Set<Class<? extends Plugin>> classes, Set<Plugin> instances);

	public abstract String getApplicationName();
	
	public abstract String getPropertyFileName();
	
	public VarylabSplashScreen getSplashScreen() {
		if (splash == null) {
			splash = new VarylabSplashScreen();
		}
		return splash;
	}
	
	static {
		NativePathUtility.set("native");
		String[] taoCommand = new String[] {
			"-tao_nm_lamda", "0.01", 
			"-tao_nm_mu", "1.0",
//			"-tao_fd_gradient", "1E-6"
		};
		Tao.Initialize("Tao Varylab", taoCommand, false);
		Secure.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "VaryLab[Ultimate]");
		JRViewer.setApplicationIcon(ImageHook.getImage("main_03.png"));
	}
	
	private void installLookAndFeel() {
		try {
			SubstanceLookAndFeel.setSkin(new GraphiteAquaSkin());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void startup() {
		if (!EventQueue.isDispatchThread()) {
			Runnable delegate = new Runnable() {
				@Override
				public void run() {
					startup();
				}
			};
			EventQueue.invokeLater(delegate);
			return;
		}
		JRHalfedgeViewer.initHalfedgeFronted();
		StaticSetup.includePluginJars();
		StaticSetup.includeLibraryJars();
		View.setIcon(ImageHook.getIcon("main_03.png"));
		View.setTitle(getApplicationName());
		installLookAndFeel();
		final VarylabSplashScreen splash = getSplashScreen();
		splash.setVisible(true);
		// post-pone startup on the event queue
		Runnable startupRun = new Runnable() {
			@Override
			public void run() {
				JRViewer v = new JRViewer();
				v.setSplashScreen(splash);
				v.getController().setManageLookAndFeel(false);
				v.getController().setSaveOnExit(true);
				v.getController().setAskBeforeSaveOnExit(false);
				v.getController().setLoadFromUserPropertyFile(true);
				v.setPropertiesFile(getPropertyFileName());
				v.setPropertiesResource(this.getClass(), getPropertyFileName());
				v.setShowPanelSlots(true, true, true, true);
				v.addContentSupport(ContentType.Raw);
				v.setShowToolBar(true);
				v.setShowMenuBar(true);
				v.addBasicUI();
				v.addContentUI();
				v.addPythonSupport();
				Set<Class<? extends Plugin>> classes = new HashSet<Class<? extends Plugin>>();
				Set<Plugin> instances = new HashSet<Plugin>();
				getPlugins(classes, instances);
				for (Class<? extends Plugin> pc : classes) {
					v.registerPlugin(pc);
				}
				for (Plugin instance : instances) {
					v.registerPlugin(instance);
				}
				v.startup();
				splash.setVisible(false);
				System.out.println("Welcome to Varylab.");				
			}
		};
		EventQueue.invokeLater(startupRun);
	}
	
}
