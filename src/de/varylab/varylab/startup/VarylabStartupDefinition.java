package de.varylab.varylab.startup;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
import de.jtem.jrworkspace.plugin.simplecontroller.StartupChain;
import de.jtem.jtao.Tao;
import de.varylab.varylab.plugin.ui.image.ImageHook;

public abstract class VarylabStartupDefinition {

	private VarylabSplashScreen
		splash = null;
	private JRViewer 
		v = null;
	
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
		final VarylabSplashScreen splash = getSplashScreen();
		splash.setStatus(getApplicationName() + " startup");
		splash.setVisible(true);
		Runnable jobStaticInit = new Runnable() {
			@Override
			public void run() {
				splash.setStatus("static init...");
				JRHalfedgeViewer.initHalfedgeFronted();
				StaticSetup.includePluginJars();
				StaticSetup.includeLibraryJars();
				View.setIcon(ImageHook.getIcon("main_03.png"));
				View.setTitle(getApplicationName());
				installLookAndFeel();
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
				v.getController().setAskBeforeSaveOnExit(false);
				v.getController().setLoadFromUserPropertyFile(true);
				v.setPropertiesFile(getPropertyFileName());
				v.setPropertiesResource(VarylabStartupDefinition.this.getClass(), getPropertyFileName());
				v.setShowPanelSlots(true, true, true, true);
				v.addContentSupport(ContentType.Raw);
				v.setShowToolBar(true);
				v.setShowMenuBar(true);
				v.addBasicUI();
				v.addContentUI();
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
		
		splash.setVisible(false);
		System.out.println("Welcome to Varylab.");				
	}
	
}
