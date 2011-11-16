package de.varylab.varylab.startup.nurbs;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jvnet.substance.skin.SubstanceRavenGraphiteGlassLookAndFeel;

import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.ConsolePlugin;
import de.jreality.plugin.basic.View;
import de.jreality.util.NativePathUtility;
import de.jtem.halfedgetools.JRHalfedgeViewer;
import de.jtem.halfedgetools.adapter.generic.UndirectedEdgeIndex;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgePluginFactory;
import de.jtem.halfedgetools.plugin.HalfedgePreferencePage;
import de.jtem.halfedgetools.plugin.data.VisualizationInterface;
import de.jtem.halfedgetools.plugin.widget.ContextMenuWidget;
import de.jtem.halfedgetools.plugin.widget.MarqueeWidget;
import de.jtem.halfedgetools.plugin.widget.ViewSwitchWidget;
import de.varylab.varylab.hds.adapter.GeodesicLabelAdapter;
import de.varylab.varylab.hds.adapter.NodeWeigthAdapter;
import de.varylab.varylab.hds.adapter.SingularityAdapter;
import de.varylab.varylab.hds.adapter.VPositionAdapter;
import de.varylab.varylab.hds.adapter.VTexturePositionAdapter;
import de.varylab.varylab.plugin.nurbs.plugin.NurbsManagerPlugin;
import de.varylab.varylab.plugin.ui.VarylabMain;
import de.varylab.varylab.plugin.ui.image.ImageHook;
import de.varylab.varylab.startup.StaticSetup;
import de.varylab.varylab.startup.VarylabSplashScreen;

public class VarylabNurbs {

	private static void addVaryLabPlugins(JRViewer v) {
		v.registerPlugin(VarylabMain.class);
		
		HalfedgeInterface hif = new HalfedgeInterface();
		hif.addAdapter(new VPositionAdapter(), true);
		hif.addAdapter(new VTexturePositionAdapter(), true);
		hif.addAdapter(new NodeWeigthAdapter(), true);
		hif.addAdapter(new GeodesicLabelAdapter(), true);
		hif.addAdapter(new SingularityAdapter(), true);
		hif.addAdapter(new UndirectedEdgeIndex(), true);
		
		v.registerPlugin(hif);
		
		v.registerPlugin(ConsolePlugin.class);
//		v.registerPlugins(HalfedgePluginFactory.createPlugins());
		v.registerPlugins(HalfedgePluginFactory.createSelectionPlugins());
		v.registerPlugins(HalfedgePluginFactory.createSubdivisionPlugins());
		v.registerPlugins(HalfedgePluginFactory.createEditingPlugins());
		v.registerPlugins(HalfedgePluginFactory.createDataVisualizationPlugins());
		v.registerPlugin(new MarqueeWidget());
		v.registerPlugin(new ViewSwitchWidget());
		v.registerPlugin(new ContextMenuWidget());
		
		v.registerPlugin(NurbsManagerPlugin.class);
		v.registerPlugin(HalfedgePreferencePage.class);
		v.registerPlugin(VisualizationInterface.class);
	}

	
	public static void installLookAndFeel() {
		try {
			UIManager.setLookAndFeel(new SubstanceRavenGraphiteGlassLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void startup() {
		NativePathUtility.set("native");
		JRHalfedgeViewer.initHalfedgeFronted();
		StaticSetup.includePluginJars();
		StaticSetup.includeLibraryJars();
		View.setIcon(ImageHook.getIcon("surface.png"));
		View.setTitle("VaryLab[NURBS]");
		JRViewer v = new JRViewer();
		installLookAndFeel();
		VarylabSplashScreen splash = new VarylabSplashScreen();
		splash.setVisible(true);
		v.setSplashScreen(splash);
		v.getController().setManageLookAndFeel(false);
		v.getController().setSaveOnExit(true);
		v.getController().setAskBeforeSaveOnExit(false);
		v.getController().setLoadFromUserPropertyFile(true);
		v.setPropertiesFile("VarylabNurbs.xml");
		v.setPropertiesResource(VarylabNurbs.class, "VarylabNurbs.xml");
		v.setShowPanelSlots(true, true, true, true);
		v.addContentSupport(ContentType.Raw);
		v.setShowToolBar(true);
		v.setShowMenuBar(true);
		v.addBasicUI();
		v.addContentUI();
		addVaryLabPlugins(v);
		v.startup();
		splash.setVisible(false);
	}
	
	
	public static void main(String[] args) throws Exception {
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "VaryLab[NURBS]");
		JRViewer.setApplicationIcon(ImageHook.getImage("surface.png"));
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				startup();
			}
		});
	}

}
