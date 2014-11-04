package de.varylab.varylab.startup;

import java.awt.EventQueue;
import java.awt.Image;
import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;

import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.plugin.basic.ViewToolBar;
import de.jreality.plugin.content.ContentAppearance;
import de.jreality.plugin.content.ContentLoader;
import de.jreality.plugin.content.ContentTools;
import de.jreality.plugin.menu.BackgroundColor;
import de.jreality.plugin.menu.CameraMenu;
import de.jreality.plugin.menu.DisplayOptions;
import de.jreality.plugin.menu.ExportMenu;
import de.jreality.util.NativePathUtility;
import de.jreality.util.Secure;
import de.jtem.halfedgetools.JRHalfedgeViewer;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.simplecontroller.SimpleController.PropertiesMode;
import de.jtem.jrworkspace.plugin.simplecontroller.StartupChain;
import de.jtem.jrworkspace.plugin.simplecontroller.widget.SplashScreen;

public abstract class VarylabStartupDefinition {

	private VarylabSplashScreen
		splash = null;
	private JRViewer 
		v = null;
	private static Logger
		log = Logger.getLogger(VarylabStartupDefinition.class.getName());
	
	public abstract void getPlugins(Set<Class<? extends Plugin>> classes, Set<Plugin> instances);

	public abstract String getApplicationName();
	
	public abstract String getPropertyFileName();
	
	public VarylabSplashScreen getSplashScreen() {
		if (splash == null) {
			splash = new VarylabSplashScreen();
		}
		return splash;
	}
	
	private void staticInit() {
		StaticSetup.initLogging();
		StaticSetup.initJython();
		NativePathUtility.set("native");
		Image appIcon = SplashImageHook.getImage("icon_256.png");
		JRViewer.setApplicationIcon(appIcon);
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		View.setIcon(SplashImageHook.getIcon("icon_16.png"));
		View.setIconList(StaticSetup.getMainIconList());
		View.setTitle(getApplicationName());		
		JRHalfedgeViewer.initHalfedgeFronted();
		try {
			StaticSetup.includePluginJars();
			StaticSetup.includeLibraryJars();
		} catch (Exception e) {
			log.warning("cound not setup drop-in plugin folder: " + e);
		}
	}
	
	
	protected void postStartup(JRViewer v, SplashScreen splash) {
		
	}
	
	protected void startup() {
		Secure.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", getApplicationName());
		final VarylabSplashScreen splash = getSplashScreen();
		splash.setStatus(getApplicationName() + " startup");
		splash.setVisible(true);
		Runnable jobStaticInit = new Runnable() {
			@Override
			public void run() {
				splash.setStatus("static init...");
				staticInit();
			}
		};		
		Runnable jobLookAndFeel = new Runnable() {
			@Override
			public void run() {
				splash.setStatus("install substance look and feel...");
				StaticSetup.installLookAndFeel();
			}
		};
		Runnable jobInitViewer = new Runnable() {
			@Override
			public void run() {
				splash.setStatus("creating viewer...");
				v = new JRViewer();
				v.setSplashScreen(splash);
				v.getController().setManageLookAndFeel(false);
				v.getController().setSaveOnExit(true);
				v.getController().setPropertiesMode(PropertiesMode.StaticPropertiesFile);
				v.getController().setStaticPropertiesFile(new File(getPropertyFileName()));
				v.setShowPanelSlots(true, true, true, true);
				v.setShowToolBar(true);
				v.setShowMenuBar(true);
				// basic ui
				v.registerPlugin(BackgroundColor.class);
				v.registerPlugin(DisplayOptions.class);
				v.registerPlugin(ViewMenuBar.class);
				v.registerPlugin(ViewToolBar.class);
				v.registerPlugin(ExportMenu.class);
				v.registerPlugin(CameraMenu.class);
				// content
				v.addContentSupport(ContentType.Raw);
				v.registerPlugin(ContentTools.class);
				v.registerPlugin(ContentAppearance.class);
				v.registerPlugin(ContentLoader.class);
				// python scripting
				v.addPythonSupport();
			}
		};
		final Set<Class<? extends Plugin>> classes = new HashSet<Class<? extends Plugin>>();
		final Set<Plugin> instances = new HashSet<Plugin>();
		Runnable jobAssemblePlugins = new Runnable() {
			@Override
			public void run() {
				splash.setStatus("assembling plugins...");
				getPlugins(classes, instances);
			}
		};
		
		StartupChain initChain = new StartupChain();
		initChain.appendJob(jobStaticInit);
		initChain.appendJob(jobLookAndFeel);
		initChain.appendJob(jobInitViewer);
		initChain.appendJob(jobAssemblePlugins);
		initChain.startQueuedAndWait();
		
		List<Runnable> jobsRegisterPlugins = new LinkedList<Runnable>();
		for (final Class<? extends Plugin> pc : classes) {
			Runnable job = new Runnable() {
				@Override
				public void run() {
					splash.setStatus("register plug-in " + pc.getSimpleName());
					v.registerPlugin(pc);					
				}
			};
			jobsRegisterPlugins.add(job);
		}
		for (final Plugin instance : instances) {
			Runnable job = new Runnable() {
				@Override
				public void run() {
					splash.setStatus("register plug-in " + instance.getClass().getSimpleName());
					v.registerPlugin(instance);					
				}
			};
			jobsRegisterPlugins.add(job);
		}
		
		StartupChain registrationChain = new StartupChain();
		registrationChain.appendAll(jobsRegisterPlugins);
		registrationChain.startQueuedAndWait();
		
		v.startup();
		
		Runnable postStartupJob = new Runnable() {
			@Override
			public void run() {
				splash.setStatus("running post stratup scripts");
				postStartup(v, splash);
			}
		};
		StartupChain postStartupChain = new StartupChain();
		postStartupChain.appendJob(postStartupJob);
		postStartupChain.startQueuedAndWait();
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				splash.setVisible(false);				
			}
		};
		EventQueue.invokeLater(r);
		System.out.println("Welcome to Varylab.");				
	}
	
}
