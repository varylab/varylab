package de.varylab.varylab.startup.definitions;

import java.awt.Image;
import java.util.Set;

import de.jreality.plugin.basic.ConsolePlugin;
import de.jtem.halfedgetools.plugin.AlgorithmDropdownToolbar;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgePluginFactory;
import de.jtem.halfedgetools.plugin.HalfedgePreferencePage;
import de.jtem.halfedgetools.plugin.data.VisualizationInterface;
import de.jtem.halfedgetools.plugin.misc.VertexEditorPlugin;
import de.jtem.halfedgetools.plugin.visualizers.AngleDefectVisualizer;
import de.jtem.halfedgetools.plugin.visualizers.EdgeLengthVisualizer;
import de.jtem.halfedgetools.plugin.widget.ContextMenuWidget;
import de.jtem.halfedgetools.plugin.widget.MarqueeWidget;
import de.jtem.halfedgetools.plugin.widget.ViewSwitchWidget;
import de.jtem.jrworkspace.plugin.Plugin;
import de.varylab.discreteconformal.plugin.DiscreteConformalPlugin;
import de.varylab.varylab.plugin.VarylabMain;
import de.varylab.varylab.plugin.datasource.GeodesicCircumcircleCurvature;
import de.varylab.varylab.plugin.datasource.GeodesicCurvature;
import de.varylab.varylab.plugin.datasource.OppositeAnglesCurvature;
import de.varylab.varylab.plugin.datasource.OppositeEdgesCurvature;
import de.varylab.varylab.plugin.datasource.ReferenceDistance;
import de.varylab.varylab.plugin.meshoptimizer.CircumcircleCurvatureOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.OppositeAnglesCurvatureOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.OppositeEdgesCurvatureOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.ReferenceSurfaceOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.SpringOptimizer;
import de.varylab.varylab.plugin.optimization.OptimizationPanel;
import de.varylab.varylab.plugin.remeshing.FitTexturePlugin;
import de.varylab.varylab.plugin.remeshing.SurfaceRemeshingPlugin;
import de.varylab.varylab.plugin.visualizers.GaussCurvatureVisualizer;
import de.varylab.varylab.startup.VarylabSplashScreen;
import de.varylab.varylab.startup.VarylabStartupDefinition;
import de.varylab.varylab.startup.image.SplashImageHook;

public class VaryLabGridshells extends VarylabStartupDefinition {

	private VarylabSplashScreen
	splash = null;
	
	@Override
	public String getApplicationName() {
		return "VaryLab[Gridshells]";
	}
	
	@Override
	public String getPropertyFileName() {
		return "VarylabGridshells.xml";
	}
	
	@Override
	public VarylabSplashScreen getSplashScreen() {
		if (splash == null) {
			Image lowRes = SplashImageHook.getImage("varylab_grid_low_res.png");
			Image highRes = SplashImageHook.getImage("varylab_grid_high_res.png");
			splash = new VarylabSplashScreen(lowRes, highRes);
		}
		return splash;
	}
	
	@Override
	public void getPlugins(Set<Class<? extends Plugin>> classes, Set<Plugin> instances) {
		classes.add(VarylabMain.class);
		classes.add(HalfedgeInterface.class);
		classes.add(OptimizationPanel.class);
		classes.add(VertexEditorPlugin.class);
		
		addOptimizationPlugins(classes);
		addVisualizerPlugins(classes);
		
		classes.add(ConsolePlugin.class);
		instances.addAll(HalfedgePluginFactory.createSelectionPlugins());
		instances.addAll(HalfedgePluginFactory.createSubdivisionPlugins());
		instances.addAll(HalfedgePluginFactory.createEditingPlugins());
		instances.addAll(HalfedgePluginFactory.createDataVisualizationPlugins());
		instances.addAll(HalfedgePluginFactory.createEditorModePlugins());
		classes.add(AlgorithmDropdownToolbar.class);
		classes.add(MarqueeWidget.class);
		classes.add(ViewSwitchWidget.class);
		classes.add(ContextMenuWidget.class);
		
		classes.add(DiscreteConformalPlugin.class);
		classes.add(SurfaceRemeshingPlugin.class);
		classes.add(DiscreteConformalPlugin.class);
		classes.add(FitTexturePlugin.class);
		
		classes.add(HalfedgePreferencePage.class);
		classes.add(VisualizationInterface.class);
	}

	private static void addVisualizerPlugins(Set<Class<? extends Plugin>> classes) {
		classes.add(EdgeLengthVisualizer.class);
//		classes.add(FacePlanarityVisualizer.class);
//		classes.add(DirichletEnergyVisualizer.class);
//		classes.add(OddVertexVisualizer.class);
//		classes.add(NormalVisualizer.class);
//		classes.add(StarPlanarityVisualizer.class);
//		classes.add(HyperbolicPatchVisualizer.class);
//		classes.add(NodeIndexVisualizer.class);
//		classes.add(WeightsVisualizer.class);
//		classes.add(GeodesicLabelVisualizer.class);
//		classes.add(CircularityVisualizer.class);
		classes.add(GaussCurvatureVisualizer.class);
		classes.add(AngleDefectVisualizer.class);
		classes.add(GeodesicCircumcircleCurvature.class);
//		classes.add(ConnectionVisualizer.class);
//		classes.add(PositiveEdgeVisualizer.class);
//		classes.add(ConicalityVisualizer.class);
//		classes.add(IncircleVisualizer.class);
//		classes.add(DiagonalLengthVisualizer.class);
		
// data sources
		classes.add(OppositeEdgesCurvature.class);
		classes.add(OppositeAnglesCurvature.class);
		classes.add(GeodesicCurvature.class);
		classes.add(ReferenceDistance.class);
	}


	private static void addOptimizationPlugins(Set<Class<? extends Plugin>> classes) {
//		classes.add(MeanEdgeLengthOptimizer.class);
//		classes.add(EdgeLengthEqualizerOptimizer.class);
//		classes.add(PlanarQuadsOptimizer.class);
//		classes.add(WillmoreOptimizer.class);
		classes.add(OppositeAnglesCurvatureOptimizer.class);
//		classes.add(GeodesicLaplaceOptimizer.class);
//		classes.add(ANetOptimizer.class);
//		classes.add(ConstantDirectionFieldPlugin.class);
//		classes.add(ConstantMeanCurvatureFieldPlugin.class);
		classes.add(SpringOptimizer.class);
//		classes.add(ElectrostaticOptimizer.class);
//		classes.add(ElectrostaticSphereOptimizer.class);
//		classes.add(PlanarNGonsOptimizer.class);
		classes.add(OppositeEdgesCurvatureOptimizer.class);
		classes.add(ReferenceSurfaceOptimizer.class);
//		classes.add(CircularQuadOptimizer.class);
//		classes.add(ConicalOptimizer.class);
//		classes.add(InflateOptimizer.class);
//		classes.add(IncircleOptimizer.class);
//		classes.add(TouchingIncirclesOptimizer.class);
//		classes.add(TouchingIncirclesOptimizerCot.class);
//		classes.add(TouchingIncirclesTan2Optimizer.class);
//		classes.add(EqualDiagonalsOptimizer.class);
		classes.add(CircumcircleCurvatureOptimizer.class);
	}
	

	public static void main(String[] args) throws Exception {
		new VaryLabGridshells().startup();
	}
	
}
