package de.varylab.varylab.startup.gridshells;

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
import de.jtem.halfedgetools.plugin.data.HalfedgeDataInterface;
import de.jtem.halfedgetools.plugin.visualizers.AngleDefectVisualizer;
import de.jtem.halfedgetools.plugin.visualizers.EdgeLengthVisualizer;
import de.jtem.halfedgetools.plugin.widget.ContextMenuWidget;
import de.jtem.halfedgetools.plugin.widget.MarqueeWidget;
import de.jtem.halfedgetools.plugin.widget.ViewSwitchWidget;
import de.jtem.jrworkspace.plugin.lnfswitch.LookAndFeelSwitch;
import de.jtem.jrworkspace.plugin.lnfswitch.plugin.CrossPlatformLnF;
import de.jtem.jrworkspace.plugin.lnfswitch.plugin.NimbusLnF;
import de.jtem.jrworkspace.plugin.lnfswitch.plugin.SystemLookAndFeel;
import de.varylab.discreteconformal.plugin.DiscreteConformalPlugin;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.adapter.VPositionAdapter;
import de.varylab.varylab.hds.adapter.VTexturePositionAdapter;
import de.varylab.varylab.plugin.meshoptimizer.EdgeLengthEqualizerOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.ExteriorGeodesicOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.GeodesicAngleOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.ReferenceSurfaceOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.SpringOptimizer;
import de.varylab.varylab.plugin.remeshing.SurfaceRemeshingPlugin;
import de.varylab.varylab.plugin.ui.OptimizationPanel;
import de.varylab.varylab.plugin.ui.image.ImageHook;
import de.varylab.varylab.plugin.visualizers.GaussCurvatureVisualizer;

public class VaryLabGridShells {

	private static void addVaryLabPlugins(JRViewer v) {
		HalfedgeInterface hif = new HalfedgeInterface();
		hif.addAdapter(new VPositionAdapter(), true);
		hif.addAdapter(new VTexturePositionAdapter(), true);
//		hif.addAdapter(new NodeWeigthAdapter(), true);
//		hif.addAdapter(new GeodesicLabelAdapter(), true);
//		hif.addAdapter(new SingularityAdapter(), true);
		hif.addAdapter(new UndirectedEdgeIndex(), true);
		
		v.registerPlugin(hif);
		v.registerPlugin(OptimizationPanel.class);
//		v.registerPlugin(VertexEditorPlugin.class);
//		v.registerPlugin(IdentifyVerticesPlugin.class);

//		v.registerPlugin(TrivialConnectionPlugin.class);
		
		addGeneratorPlugins(v);
		
//		v.registerPlugin(IdentifyVerticesPlugin.class);
		
//		v.registerPlugin(HeightFieldEditor.class);
		
		addOptimizationPlugins(v);
		addLnFPlugins(v);
		addVisualizerPlugins(v);
		addDDGPlugins(v);
		
//		v.registerPlugin(CatmullClarkPlugin.class);
//		v.registerPlugin(LoopPlugin.class);
		
		v.registerPlugin(ConsolePlugin.class);
//		v.registerPlugins(HalfedgePluginFactory.createPlugins());
		v.registerPlugins(HalfedgePluginFactory.createSelectionPlugins());
		v.registerPlugins(HalfedgePluginFactory.createSubdivisionPlugins());
		v.registerPlugins(HalfedgePluginFactory.createEditingPlugins());
		v.registerPlugin(new MarqueeWidget());
		v.registerPlugin(new ViewSwitchWidget());
		v.registerPlugin(new ContextMenuWidget());
		
		v.registerPlugin(DiscreteConformalPlugin.class);
//		v.registerPlugin(RoofSubdivisionPlugin.class);
//		v.registerPlugin(StripSubdivisionPlugin.class);
//		v.registerPlugin(SplitFacePlugin.class);
//		v.registerPlugin(Toolbox.class);
		
		v.registerPlugin(SurfaceRemeshingPlugin.class);
		v.registerPlugin(DiscreteConformalPlugin.class);
		
//		v.registerPlugin(OBJExportPlugin.class);
		
//		v.registerPlugin(NurbsManagerPlugin.class);
//		v.registerPlugin(HalfedgeDebuggerPlugin.class);
//		v.registerPlugin(WebContentLoader.class);
		
//		v.registerPlugin(Sky.class);
//		v.registerPlugin(AngleCalculatorPlugin.class);
//		v.registerPlugin(NodePropertyEditor.class);
//		v.registerPlugin(RemoveGeodesicPlugin.class);
//		v.registerPlugin(GeodesicSelection.class);
//		v.registerPlugin(LatticeSelection.class);
//		v.registerPlugin(StripSelection.class);
//		v.registerPlugin(TextureVertexSelection.class);
//		v.registerPlugin(TextureEdgeSelection.class);
//		v.registerPlugin(BoundaryEarsSelection.class);
//		v.registerPlugin(CollapseToNeighborPlugin.class);
//		v.registerPlugin(TextureGeometryGenerator.class);
//		v.registerPlugin(FitTexturePlugin.class);
//		v.registerPlugin(StitchingPlugin.class);
//		v.registerPlugin(StitchCutPathPlugin.class);
//		v.registerPlugin(CollapseTrianglesPlugin.class);
//		v.registerPlugin(Collapse2ValentPlugin.class);
		
//		v.registerPlugin(DehomogenizeTexture.class);
		
		v.registerPlugin(HalfedgePreferencePage.class);
		v.registerPlugin(HalfedgeDataInterface.class);
	}


	private static void addGeneratorPlugins(JRViewer v) {
//		v.registerPlugin(QuadMeshGenerator.class);
//		v.registerPlugin(HexMeshGenerator.class);
//		v.registerPlugin(SimpleRoofGenerator.class);
//		v.registerPlugin(PrimitivesGenerator.class);
	}


	private static void addVisualizerPlugins(JRViewer v) {
		v.registerPlugin(EdgeLengthVisualizer.class);
//		v.registerPlugin(FacePlanarityVisualizer.class);
//		v.registerPlugin(DirichletEnergyVisualizer.class);
//		v.registerPlugin(OddVertexVisualizer.class);
//		v.registerPlugin(NormalVisualizer.class);
//		v.registerPlugin(StarPlanarityVisualizer.class);
//		v.registerPlugin(HyperbolicPatchVisualizer.class);
//		v.registerPlugin(NodeIndexVisualizer.class);
//		v.registerPlugin(WeightsVisualizer.class);
//		v.registerPlugin(GeodesicLabelVisualizer.class);
//		v.registerPlugin(CircularityVisualizer.class);
		v.registerPlugin(GaussCurvatureVisualizer.class);
		v.registerPlugin(AngleDefectVisualizer.class);
//		v.registerPlugin(ConnectionVisualizer.class);
//		v.registerPlugin(PositiveEdgeVisualizer.class);
//		v.registerPlugin(ConicalityVisualizer.class);
//		v.registerPlugin(IncircleVisualizer.class);
//		v.registerPlugin(DiagonalLengthVisualizer.class);
	}


	private static void addOptimizationPlugins(JRViewer v) {
//		v.registerPlugin(MeanEdgeLengthOptimizer.class);
		v.registerPlugin(EdgeLengthEqualizerOptimizer.class);
//		v.registerPlugin(PlanarQuadsOptimizer.class);
//		v.registerPlugin(WillmoreOptimizer.class);
		v.registerPlugin(GeodesicAngleOptimizer.class);
//		v.registerPlugin(GeodesicLaplaceOptimizer.class);
//		v.registerPlugin(ANetOptimizer.class);
//		v.registerPlugin(ConstantDirectionFieldPlugin.class);
//		v.registerPlugin(ConstantMeanCurvatureFieldPlugin.class);
		v.registerPlugin(SpringOptimizer.class);
//		v.registerPlugin(ElectrostaticOptimizer.class);
//		v.registerPlugin(ElectrostaticSphereOptimizer.class);
//		v.registerPlugin(PlanarNGonsOptimizer.class);
		v.registerPlugin(ExteriorGeodesicOptimizer.class);
		v.registerPlugin(ReferenceSurfaceOptimizer.class);
//		v.registerPlugin(CircularQuadOptimizer.class);
//		v.registerPlugin(ConicalOptimizer.class);
//		v.registerPlugin(InflateOptimizer.class);
//		v.registerPlugin(IncircleOptimizer.class);
//		v.registerPlugin(TouchingIncirclesOptimizer.class);
//		v.registerPlugin(TouchingIncirclesOptimizerCot.class);
//		v.registerPlugin(TouchingIncirclesTan2Optimizer.class);
//		v.registerPlugin(EqualDiagonalsOptimizer.class);
	}
	
	private static void addLnFPlugins(JRViewer v) {
		v.registerPlugin(LookAndFeelSwitch.class);
//		v.registerPlugin(FHLookAndFeel.class);
//		v.registerPlugin(TinyLookAndFeel.class);
		v.registerPlugin(CrossPlatformLnF.class);
		v.registerPlugin(NimbusLnF.class);
		v.registerPlugin(SystemLookAndFeel.class);
//		v.registerPlugin(SyntheticaStandardLnf.class);
//		v.registerPlugin(SyntheticaBlackEyeLnf.class);
	}
	
	private static void addDDGPlugins(JRViewer v) {
//		v.registerPlugin(ChristoffelTransform.class);
//		v.registerPlugin(CentralExtensionSubdivision.class);
//		v.registerPlugin(GaussMapFromDual.class);
//		v.registerPlugin(AssociatedFamily.class);
//		v.registerPlugin(KoebeSphereProjection.class);
//		v.registerPlugin(LeastSquaresSphere.class);
//		v.registerPlugin(CurvatureLinesQualityVisualizer.class);
	}
	
	
	public static void main(String[] args) throws Exception {
		JRHalfedgeViewer.initHalfedgeFronted();
		NativePathUtility.set("native");
		View.setIcon(ImageHook.getIcon("surface.png"));
		View.setTitle("VaryLab");
		JRViewer v = new JRViewer();
		v.getController().setManageLookAndFeel(true);
		v.setPropertiesFile("VaryLabGridShells.xml");
		v.setPropertiesResource(VaryLabGridShells.class, "VaryLabGridShells.xml");
		v.setShowPanelSlots(true, true, true, true);
		v.addContentSupport(ContentType.Raw);
		v.setShowToolBar(true);
		v.setShowMenuBar(true);
		v.addBasicUI();
		v.addContentUI();
		addVaryLabPlugins(v);
		v.startup();
		v.getPlugin(HalfedgeInterface.class).set(new VHDS());
	}

}
