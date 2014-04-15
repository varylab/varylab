package de.varylab.varylab.startup.definitions;

import java.awt.Image;
import java.util.Set;

import de.jreality.plugin.basic.ConsolePlugin;
import de.jreality.plugin.basic.Inspector;
import de.jreality.plugin.scripting.PythonConsole;
import de.jreality.plugin.scripting.PythonToolsManager;
import de.jreality.plugin.scripting.gui.NumberSpinnerGUI;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgePluginFactory;
import de.jtem.halfedgetools.plugin.algorithm.subdivision.CatmullClarkPlugin;
import de.jtem.halfedgetools.plugin.algorithm.subdivision.LoopPlugin;
import de.jtem.halfedgetools.plugin.misc.VertexEditorPlugin;
import de.jtem.halfedgetools.plugin.visualizers.AngleDefectVisualizer;
import de.jtem.halfedgetools.plugin.visualizers.DirichletEnergyVisualizer;
import de.jtem.halfedgetools.plugin.visualizers.EdgeLengthVisualizer;
import de.jtem.halfedgetools.plugin.visualizers.FacePlanarityVisualizer;
import de.jtem.halfedgetools.plugin.visualizers.NodeIndexVisualizer;
import de.jtem.halfedgetools.plugin.visualizers.NormalVisualizer;
import de.jtem.halfedgetools.plugin.visualizers.PositiveEdgeVisualizer;
import de.jtem.jrworkspace.plugin.Plugin;
import de.varylab.discreteconformal.ConformalLab;
import de.varylab.discreteconformal.plugin.DiscreteConformalPlugin;
import de.varylab.discreteconformal.plugin.visualizer.FlippedTriangles;
import de.varylab.discreteconformal.plugin.visualizer.IndexMedialGraph;
import de.varylab.varylab.VaryLab;
import de.varylab.varylab.plugin.VarylabMain;
import de.varylab.varylab.plugin.affineminimal.AffineMinimalOptimizerPlugin;
import de.varylab.varylab.plugin.algorithm.geometry.Spherize;
import de.varylab.varylab.plugin.datasource.Conicality;
import de.varylab.varylab.plugin.datasource.EdgeNormalTorsion;
import de.varylab.varylab.plugin.datasource.GeodesicCircumcircleCurvature;
import de.varylab.varylab.plugin.datasource.GeodesicCurvature;
import de.varylab.varylab.plugin.datasource.IncircleCrossRatio;
import de.varylab.varylab.plugin.datasource.IncircleCrossRatio2;
import de.varylab.varylab.plugin.datasource.OppositeAnglesCurvature;
import de.varylab.varylab.plugin.datasource.OppositeEdgesCurvature;
import de.varylab.varylab.plugin.datasource.SCConicalConeDataSource;
import de.varylab.varylab.plugin.ddg.AssociatedFamily;
import de.varylab.varylab.plugin.ddg.CentralExtensionSubdivision;
import de.varylab.varylab.plugin.ddg.ChristoffelTransform;
import de.varylab.varylab.plugin.ddg.GaussMapFromDual;
import de.varylab.varylab.plugin.ddg.KoebeSphereProjection;
import de.varylab.varylab.plugin.ddg.KoenigsDual;
import de.varylab.varylab.plugin.ddg.LeastSquaresSphere;
import de.varylab.varylab.plugin.ddg.VertexSpheres;
import de.varylab.varylab.plugin.dec.TrivialConnectionPlugin;
import de.varylab.varylab.plugin.editor.DehomogenizeTexture;
import de.varylab.varylab.plugin.editor.EdgeCreatorPlugin;
import de.varylab.varylab.plugin.editor.HeightFieldEditor;
import de.varylab.varylab.plugin.editor.Toolbox;
import de.varylab.varylab.plugin.editor.VertexCreatorPlugin;
import de.varylab.varylab.plugin.generator.HexMeshGenerator;
import de.varylab.varylab.plugin.generator.OffsetMeshGenerator;
import de.varylab.varylab.plugin.generator.QuadMeshGenerator;
import de.varylab.varylab.plugin.generator.SimpleRoofGenerator;
import de.varylab.varylab.plugin.generator.TschebyscheffSphereGenerator;
import de.varylab.varylab.plugin.grasshopper.GrasshopperPlugin;
import de.varylab.varylab.plugin.hyperbolicnets.HyperbolicNetsPlugin;
import de.varylab.varylab.plugin.hyperbolicnets.HyperbolicPatchVisualizer;
import de.varylab.varylab.plugin.io.OBJExportPlugin;
import de.varylab.varylab.plugin.lnf.SubstanceLnFMenu;
import de.varylab.varylab.plugin.meshoptimizer.ANetOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.CircularQuadOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.CircumcircleCurvatureOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.ConicalOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.ConstantDirectionFieldPlugin;
import de.varylab.varylab.plugin.meshoptimizer.EdgeLengthEqualizerOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.ElectrostaticOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.ElectrostaticSphereOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.EqualDiagonalsOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.ForceConeOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.GeodesicCurvatureOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.IncircleOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.InflateOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.MeanEdgeLengthOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.MinimalPathsOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.NURBSSurfaceOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.NgonRegularizer;
import de.varylab.varylab.plugin.meshoptimizer.OppositeAnglesCurvatureOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.OppositeEdgesCurvatureOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.PlanarNGonsOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.PlanarQuadsOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.ReferenceSurfaceOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.SpringOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.TouchingIncirclesOptimizerCot;
import de.varylab.varylab.plugin.meshoptimizer.WillmoreOptimizer;
import de.varylab.varylab.plugin.nodeeditor.NodePropertyEditor;
import de.varylab.varylab.plugin.nurbs.plugin.NurbsManagerPlugin;
import de.varylab.varylab.plugin.optimization.OptimizationPanel;
import de.varylab.varylab.plugin.remeshing.FitTexturePlugin;
import de.varylab.varylab.plugin.remeshing.SurfaceRemeshingPlugin;
import de.varylab.varylab.plugin.remeshing.TextureGeometryGenerator;
import de.varylab.varylab.plugin.selection.BoundaryEarsSelection;
import de.varylab.varylab.plugin.selection.GeodesicSelection;
import de.varylab.varylab.plugin.selection.GeodesicVertexSelection;
import de.varylab.varylab.plugin.selection.LatticeSelection;
import de.varylab.varylab.plugin.selection.NGonSelection;
import de.varylab.varylab.plugin.selection.StripSelection;
import de.varylab.varylab.plugin.selection.TextureEdgeSelection;
import de.varylab.varylab.plugin.selection.TextureVertexSelection;
import de.varylab.varylab.plugin.subdivision.RemoveGeodesicPlugin;
import de.varylab.varylab.plugin.subdivision.RoofSubdivisionPlugin;
import de.varylab.varylab.plugin.subdivision.SplitFacePlugin;
import de.varylab.varylab.plugin.subdivision.StripSubdivisionPlugin;
import de.varylab.varylab.plugin.topology.Collapse2ValentPlugin;
import de.varylab.varylab.plugin.topology.CollapseToNeighborPlugin;
import de.varylab.varylab.plugin.topology.CollapseTrianglesPlugin;
import de.varylab.varylab.plugin.topology.ExplodePlugin;
import de.varylab.varylab.plugin.topology.IdentifyVerticesPlugin;
import de.varylab.varylab.plugin.topology.StitchCutPathPlugin;
import de.varylab.varylab.plugin.topology.StitchingPlugin;
import de.varylab.varylab.plugin.visualizers.CircularityVisualizer;
import de.varylab.varylab.plugin.visualizers.ConicalityVisualizer;
import de.varylab.varylab.plugin.visualizers.ConnectionVisualizer;
import de.varylab.varylab.plugin.visualizers.CurvatureLinesQualityVisualizer;
import de.varylab.varylab.plugin.visualizers.DiagonalLengthVisualizer;
import de.varylab.varylab.plugin.visualizers.GaussCurvatureVisualizer;
import de.varylab.varylab.plugin.visualizers.GeodesicLabelVisualizer;
import de.varylab.varylab.plugin.visualizers.IncircleVisualizer;
import de.varylab.varylab.plugin.visualizers.OddVertexVisualizer;
import de.varylab.varylab.plugin.visualizers.StarPlanarityVisualizer;
import de.varylab.varylab.plugin.visualizers.WeightsVisualizer;
import de.varylab.varylab.startup.SplashImageHook;
import de.varylab.varylab.startup.VarylabSplashScreen;
import de.varylab.varylab.startup.VarylabStartupDefinition;

public class VaryLabUltimate extends VarylabStartupDefinition {

	private VarylabSplashScreen
		splash = null;
	
	@Override
	public String getApplicationName() {
		return "VaryLab[Ultimate]";
	}
	
	@Override
	public String getPropertyFileName() {
		return "VaryLab.xml";
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
	public void getPlugins(Set<Class<? extends Plugin>> pSet, Set<Plugin> instances) {
		pSet.add(VarylabMain.class);
		
		instances.addAll(HalfedgePluginFactory.createPlugins());
		instances.addAll(ConformalLab.createConformalPlugins());
		
		addGeneratorPlugins(pSet);
		addOptimizationPlugins(pSet);
		addLnFPlugins(pSet);
		addVisualizerPlugins(pSet);
		addDDGPlugins(pSet);
		
		pSet.add(HalfedgeInterface.class);
		pSet.add(OptimizationPanel.class);
		pSet.add(VertexEditorPlugin.class);
		pSet.add(IdentifyVerticesPlugin.class);
		pSet.add(TrivialConnectionPlugin.class);
		
		pSet.add(HeightFieldEditor.class);
		pSet.add(CatmullClarkPlugin.class);
		pSet.add(LoopPlugin.class);
		pSet.add(Spherize.class);
		pSet.add(ConsolePlugin.class);
		pSet.add(RoofSubdivisionPlugin.class);
		pSet.add(StripSubdivisionPlugin.class);
		pSet.add(SplitFacePlugin.class);
		pSet.add(Toolbox.class);
		
		pSet.add(VertexCreatorPlugin.class);
		pSet.add(EdgeCreatorPlugin.class);
		
		pSet.add(SurfaceRemeshingPlugin.class);
		pSet.add(DiscreteConformalPlugin.class);
		
		pSet.add(OBJExportPlugin.class);
		
		pSet.add(NurbsManagerPlugin.class);
		pSet.add(NodePropertyEditor.class);
		pSet.add(RemoveGeodesicPlugin.class);
		pSet.add(GeodesicSelection.class);
		pSet.add(GeodesicVertexSelection.class);
		pSet.add(NGonSelection.class);
		pSet.add(LatticeSelection.class);
		pSet.add(StripSelection.class);
		pSet.add(TextureVertexSelection.class);
		pSet.add(TextureEdgeSelection.class);
		pSet.add(BoundaryEarsSelection.class);
		pSet.add(CollapseToNeighborPlugin.class);
		pSet.add(TextureGeometryGenerator.class);
		pSet.add(FitTexturePlugin.class);
		pSet.add(StitchingPlugin.class);
		pSet.add(StitchCutPathPlugin.class);
		pSet.add(CollapseTrianglesPlugin.class);
		pSet.add(Collapse2ValentPlugin.class);
		
		pSet.add(DehomogenizeTexture.class);
//		pSet.add(VertexSpheres.class);
		
		pSet.add(PythonToolsManager.class);
		pSet.add(NumberSpinnerGUI.class);
		pSet.add(PythonConsole.class);
		pSet.add(Inspector.class);
		pSet.add(GrasshopperPlugin.class);
		
		//Wanda facade
		pSet.add(ExplodePlugin.class);
	}


	private static void addGeneratorPlugins(Set<Class<? extends Plugin>> pSet) {
		pSet.add(QuadMeshGenerator.class);
		pSet.add(HexMeshGenerator.class);
		pSet.add(SimpleRoofGenerator.class);
		pSet.add(TschebyscheffSphereGenerator.class);
		pSet.add(OffsetMeshGenerator.class);
	}


	private static void addVisualizerPlugins(Set<Class<? extends Plugin>> pSet) {
		pSet.add(EdgeLengthVisualizer.class);
		pSet.add(FacePlanarityVisualizer.class);
		pSet.add(DirichletEnergyVisualizer.class);
		pSet.add(OddVertexVisualizer.class);
		pSet.add(NormalVisualizer.class);
		pSet.add(StarPlanarityVisualizer.class);
		pSet.add(HyperbolicPatchVisualizer.class);
		pSet.add(NodeIndexVisualizer.class);
		pSet.add(WeightsVisualizer.class);
		pSet.add(GeodesicLabelVisualizer.class);
		pSet.add(CircularityVisualizer.class);
		pSet.add(GaussCurvatureVisualizer.class);
		pSet.add(AngleDefectVisualizer.class);
		pSet.add(ConnectionVisualizer.class);
		pSet.add(PositiveEdgeVisualizer.class);
		pSet.add(ConicalityVisualizer.class);
		pSet.add(IncircleVisualizer.class);
		pSet.add(DiagonalLengthVisualizer.class);
		
		// new data visualization data sources
		pSet.add(OppositeEdgesCurvature.class);
		pSet.add(OppositeAnglesCurvature.class);
		pSet.add(GeodesicCurvature.class);
		pSet.add(GeodesicCircumcircleCurvature.class);
		pSet.add(FlippedTriangles.class);
		pSet.add(IncircleCrossRatio.class);
		pSet.add(IncircleCrossRatio2.class);
		pSet.add(IndexMedialGraph.class);
		pSet.add(EdgeNormalTorsion.class);
		pSet.add(Conicality.class);
		pSet.add(HyperbolicNetsPlugin.class);
	}


	private static void addOptimizationPlugins(Set<Class<? extends Plugin>> pSet) {
		pSet.add(MeanEdgeLengthOptimizer.class);
		pSet.add(PlanarQuadsOptimizer.class);
		pSet.add(WillmoreOptimizer.class);
		pSet.add(OppositeAnglesCurvatureOptimizer.class);
//		pSet.add(GeodesicLaplaceOptimizer.class); // gradient not yet correctly implemented
		pSet.add(ANetOptimizer.class);
		pSet.add(ConstantDirectionFieldPlugin.class);
//		pSet.add(ConstantMeanCurvatureFieldPlugin.class);
		pSet.add(SpringOptimizer.class);
		pSet.add(ElectrostaticOptimizer.class);
		pSet.add(ElectrostaticSphereOptimizer.class);
		pSet.add(PlanarNGonsOptimizer.class);
		pSet.add(OppositeEdgesCurvatureOptimizer.class);
		pSet.add(ReferenceSurfaceOptimizer.class);
		pSet.add(CircularQuadOptimizer.class);
		pSet.add(ConicalOptimizer.class);
		pSet.add(InflateOptimizer.class);
		pSet.add(IncircleOptimizer.class);
//		pSet.add(TouchingIncirclesOptimizer.class);
		pSet.add(TouchingIncirclesOptimizerCot.class);
//		pSet.add(TouchingIncirclesTan2Optimizer.class);
		pSet.add(EqualDiagonalsOptimizer.class);
		pSet.add(EdgeLengthEqualizerOptimizer.class);
		pSet.add(GeodesicCurvatureOptimizer.class);
		pSet.add(CircumcircleCurvatureOptimizer.class);
		pSet.add(NURBSSurfaceOptimizer.class);
		pSet.add(MinimalPathsOptimizer.class);
		pSet.add(ForceConeOptimizer.class);
		pSet.add(NgonRegularizer.class);
		pSet.add(AffineMinimalOptimizerPlugin.class);
		pSet.add(SCConicalConeDataSource.class);
	}
	
	private static void addLnFPlugins(Set<Class<? extends Plugin>> pSet) {
//		pSet.add(LookAndFeelSwitch.class);
//		pSet.add(FHLookAndFeel.class);
//		pSet.add(TinyLookAndFeel.class);
//		pSet.add(CrossPlatformLnF.class);
//		pSet.add(NimbusLnF.class);
//		pSet.add(SystemLookAndFeel.class);
//		pSet.add(SyntheticaStandardLnf.class);
//		pSet.add(SyntheticaBlackEyeLnf.class);
//		pSet.add(SubstanceLnF.class);
		pSet.add(SubstanceLnFMenu.class);
	}
	
	private static void addDDGPlugins(Set<Class<? extends Plugin>> pSet) {
		pSet.add(ChristoffelTransform.class);
		pSet.add(CentralExtensionSubdivision.class);
		pSet.add(GaussMapFromDual.class);
		pSet.add(AssociatedFamily.class);
		pSet.add(KoebeSphereProjection.class);
		pSet.add(LeastSquaresSphere.class);
		pSet.add(CurvatureLinesQualityVisualizer.class);
		pSet.add(VertexSpheres.class);
		pSet.add(KoenigsDual.class);
	}
	
	
	public static void main(String[] args) throws Exception {
		new VaryLab().startup();
	}

}
