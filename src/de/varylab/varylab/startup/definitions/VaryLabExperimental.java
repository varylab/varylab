package de.varylab.varylab.startup.definitions;

import java.awt.Image;
import java.util.Set;

import de.jtem.halfedgetools.plugin.HalfedgePluginFactory;
import de.jtem.halfedgetools.plugin.widget.ContextMenuWidget;
import de.jtem.halfedgetools.plugin.widget.MarqueeWidget;
import de.jtem.jrworkspace.plugin.Plugin;
import de.varylab.varylab.plugin.VarylabMain;
import de.varylab.varylab.plugin.meshoptimizer.CircumcircleCurvatureOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.OppositeAnglesCurvatureOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.OppositeEdgesCurvatureOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.ReferenceSurfaceOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.SpringOptimizer;
import de.varylab.varylab.plugin.optimization.IterationProtocolPanel;
import de.varylab.varylab.plugin.optimization.OptimizationPanel;
import de.varylab.varylab.startup.SplashImageHook;
import de.varylab.varylab.startup.VarylabSplashScreen;
import de.varylab.varylab.startup.VarylabStartupDefinition;

public class VaryLabExperimental extends VarylabStartupDefinition {

	private VarylabSplashScreen
		splash = null;
	
	@Override
	public String getApplicationName() {
		return "VaryLab[Experimental]";
	}
	
	@Override
	public String getPropertyFileName() {
		return "VaryLabExperimental.xml";
	}
	
	@Override
	public VarylabSplashScreen getSplashScreen() {
		if (splash == null) {
			Image lowRes = SplashImageHook.getImage("varylab_experimental_low_res.png");
			Image highRes = SplashImageHook.getImage("varylab_experimental_high_res.png");
			splash = new VarylabSplashScreen(lowRes, highRes);
		}
		return splash;
	}
	
	@Override
	public void getPlugins(Set<Class<? extends Plugin>> classes, Set<Plugin> instances) {
		classes.add(VarylabMain.class);
		classes.add(IterationProtocolPanel.class);
		addOptimizationPlugins(classes);
		instances.addAll(HalfedgePluginFactory.createEditorModePlugins());
		classes.add(MarqueeWidget.class);
		classes.add(ContextMenuWidget.class);
		
	}

	private static void addOptimizationPlugins(Set<Class<? extends Plugin>> classes) {
		classes.add(OptimizationPanel.class);
		classes.add(OppositeAnglesCurvatureOptimizer.class);
		classes.add(SpringOptimizer.class);
		classes.add(OppositeEdgesCurvatureOptimizer.class);
		classes.add(ReferenceSurfaceOptimizer.class);
		classes.add(CircumcircleCurvatureOptimizer.class);
	}
	
	public static void main(String[] args) throws Exception {
		new VaryLabExperimental().startup();
	}

}
