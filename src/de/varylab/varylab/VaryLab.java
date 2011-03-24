package de.varylab.varylab;

import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.ConsolePlugin;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.scene.Sky;
import de.jreality.util.NativePathUtility;
import de.jtem.halfedgetools.JRHalfedgeViewer;
import de.jtem.halfedgetools.adapter.generic.UndirectedEdgeIndex;
import de.jtem.halfedgetools.nurbs.NurbsManagerPlugin;
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
import de.jtem.jrworkspace.plugin.lnfswitch.LookAndFeelSwitch;
import de.jtem.jrworkspace.plugin.lnfswitch.plugin.CrossPlatformLnF;
import de.jtem.jrworkspace.plugin.lnfswitch.plugin.NimbusLnF;
import de.jtem.jrworkspace.plugin.lnfswitch.plugin.SystemLookAndFeel;
import de.varylab.discreteconformal.ConformalLab;
import de.varylab.discreteconformal.plugin.DiscreteConformalPlugin;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.adapter.GeodesicLabelAdapter;
import de.varylab.varylab.hds.adapter.NodeWeigthAdapter;
import de.varylab.varylab.hds.adapter.SingularityAdapter;
import de.varylab.varylab.hds.adapter.VPositionAdapter;
import de.varylab.varylab.hds.adapter.VTexturePositionAdapter;
import de.varylab.varylab.plugin.ddg.AssociatedFamily;
import de.varylab.varylab.plugin.ddg.ChristoffelTransfom;
import de.varylab.varylab.plugin.dec.TrivialConnectionPlugin;
import de.varylab.varylab.plugin.editor.HeightFieldEditor;
import de.varylab.varylab.plugin.editor.Toolbox;
import de.varylab.varylab.plugin.generator.HexMeshGenerator;
import de.varylab.varylab.plugin.generator.PrimitivesGenerator;
import de.varylab.varylab.plugin.generator.QuadMeshGenerator;
import de.varylab.varylab.plugin.generator.SimpleRoofGenerator;
import de.varylab.varylab.plugin.io.OBJExportPlugin;
import de.varylab.varylab.plugin.lnf.FHLookAndFeel;
import de.varylab.varylab.plugin.lnf.TinyLookAndFeel;
import de.varylab.varylab.plugin.meshoptimizer.ANetOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.CircularQuadOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.ConicalOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.ConstantDirectionFieldPlugin;
import de.varylab.varylab.plugin.meshoptimizer.EdgeLengthOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.ElectrostaticOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.ElectrostaticSphereOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.ExteriorGeodesicOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.GeodesicAngleOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.GeodesicLaplaceOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.InflateOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.PlanarNGonsOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.PlanarQuadsOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.ReferenceSurfaceOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.SpringOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.WillmoreOptimizer;
import de.varylab.varylab.plugin.remeshing.SurfaceRemeshingPlugin;
import de.varylab.varylab.plugin.selection.GeodesicSelection;
import de.varylab.varylab.plugin.selection.LatticeSelection;
import de.varylab.varylab.plugin.selection.StripSelection;
import de.varylab.varylab.plugin.selection.TextureEdgeSelection;
import de.varylab.varylab.plugin.selection.TextureVertexSelection;
import de.varylab.varylab.plugin.subdivision.RemoveGeodesicPlugin;
import de.varylab.varylab.plugin.subdivision.RoofSubdivisionPlugin;
import de.varylab.varylab.plugin.subdivision.SplitFacePlugin;
import de.varylab.varylab.plugin.subdivision.StripSubdivisionPlugin;
import de.varylab.varylab.plugin.topology.CollapseTrianglesPlugin;
import de.varylab.varylab.plugin.topology.IdentifyVerticesPlugin;
import de.varylab.varylab.plugin.topology.StitchingPlugin;
import de.varylab.varylab.plugin.ui.AngleCalculatorPlugin;
import de.varylab.varylab.plugin.ui.OptimizationPanel;
import de.varylab.varylab.plugin.ui.image.ImageHook;
import de.varylab.varylab.plugin.ui.nodeeditor.NodePropertyEditor;
import de.varylab.varylab.plugin.visualizers.CircularityVisualizer;
import de.varylab.varylab.plugin.visualizers.ConicalityVisualizer;
import de.varylab.varylab.plugin.visualizers.ConnectionVisualizer;
import de.varylab.varylab.plugin.visualizers.GaussCurvatureVisualizer;
import de.varylab.varylab.plugin.visualizers.GeodesicLabelVisualizer;
import de.varylab.varylab.plugin.visualizers.HyperbolicPatchVisualizer;
import de.varylab.varylab.plugin.visualizers.OddVertexVisualizer;
import de.varylab.varylab.plugin.visualizers.StarPlanarityVisualizer;
import de.varylab.varylab.plugin.visualizers.WeightsVisualizer;

public class VaryLab {

	private static void addVaryLabPlugins(JRViewer v) {
		HalfedgeInterface hif = new HalfedgeInterface();
		hif.addGlobalAdapter(new VPositionAdapter(), true);
		hif.addGlobalAdapter(new VTexturePositionAdapter(), true);
		hif.addGlobalAdapter(new NodeWeigthAdapter(), true);
		hif.addGlobalAdapter(new GeodesicLabelAdapter(), true);
		hif.addGlobalAdapter(new SingularityAdapter(), true);
		hif.addGlobalAdapter(new UndirectedEdgeIndex(), true);
		
		v.registerPlugin(hif);
		v.registerPlugin(new OptimizationPanel());
		v.registerPlugin(new VertexEditorPlugin());
		v.registerPlugin(new IdentifyVerticesPlugin());

		v.registerPlugin(new TrivialConnectionPlugin());
		
		addGeneratorPlugins(v);
		
		v.registerPlugin(new IdentifyVerticesPlugin());
		
		v.registerPlugin(new HeightFieldEditor());
		
		addOptimizationPlugins(v);
		addLnFPlugins(v);
		addVisualizerPlugins(v);
		addDDGPlugins(v);
		
		v.registerPlugin(new CatmullClarkPlugin());
		v.registerPlugin(new LoopPlugin());
		
		v.registerPlugin(new ConsolePlugin());
		v.registerPlugins(HalfedgePluginFactory.createPlugins());
		v.registerPlugin(new RoofSubdivisionPlugin());
		v.registerPlugin(new StripSubdivisionPlugin());
		v.registerPlugin(new SplitFacePlugin());
		v.registerPlugin(new Toolbox());
		
		v.registerPlugin(new SurfaceRemeshingPlugin());
		v.registerPlugin(new DiscreteConformalPlugin());
		
		v.registerPlugin(new OBJExportPlugin());
		
		v.registerPlugin(new NurbsManagerPlugin());
//		v.registerPlugin(new HalfedgeDebuggerPlugin());
//		v.registerPlugin(new WebContentLoader());
		
		v.registerPlugin(new Sky());
		v.registerPlugin(new AngleCalculatorPlugin());
		v.registerPlugin(new NodePropertyEditor());
		v.registerPlugin(new RemoveGeodesicPlugin());
		v.registerPlugin(new GeodesicSelection());
		v.registerPlugin(new LatticeSelection());
		v.registerPlugin(new StripSelection());
		v.registerPlugin(new TextureVertexSelection());
		v.registerPlugin(new TextureEdgeSelection());
		v.registerPlugin(new StitchingPlugin());
		v.registerPlugin(new CollapseTrianglesPlugin());
	}


	private static void addGeneratorPlugins(JRViewer v) {
		v.registerPlugin(new QuadMeshGenerator());
		v.registerPlugin(new HexMeshGenerator());
		v.registerPlugin(new SimpleRoofGenerator());
		v.registerPlugin(new PrimitivesGenerator());
	}


	private static void addVisualizerPlugins(JRViewer v) {
		v.registerPlugin(new EdgeLengthVisualizer());
		v.registerPlugin(new FacePlanarityVisualizer());
		v.registerPlugin(new DirichletEnergyVisualizer());
		v.registerPlugin(new OddVertexVisualizer());
		v.registerPlugin(new NormalVisualizer());
		v.registerPlugin(new StarPlanarityVisualizer());
		v.registerPlugin(new HyperbolicPatchVisualizer());
		v.registerPlugin(new NodeIndexVisualizer());
		v.registerPlugin(new WeightsVisualizer());
		v.registerPlugin(new GeodesicLabelVisualizer());
		v.registerPlugin(new CircularityVisualizer());
		v.registerPlugin(new GaussCurvatureVisualizer());
		v.registerPlugin(new AngleDefectVisualizer());
		v.registerPlugin(new ConnectionVisualizer());
		v.registerPlugin(new PositiveEdgeVisualizer());
		v.registerPlugin(new ConicalityVisualizer());
	}


	private static void addOptimizationPlugins(JRViewer v) {
		v.registerPlugin(new EdgeLengthOptimizer());
		v.registerPlugin(new PlanarQuadsOptimizer());
		v.registerPlugin(new WillmoreOptimizer());
		v.registerPlugin(new GeodesicAngleOptimizer());
		v.registerPlugin(new GeodesicLaplaceOptimizer());
		v.registerPlugin(new ANetOptimizer());
		v.registerPlugin(new ConstantDirectionFieldPlugin());
//		v.registerPlugin(new ConstantMeanCurvatureFieldPlugin());
		v.registerPlugin(new SpringOptimizer());
		v.registerPlugin(new ElectrostaticOptimizer());
		v.registerPlugin(new ElectrostaticSphereOptimizer());
		v.registerPlugin(new PlanarNGonsOptimizer());
		v.registerPlugin(new ExteriorGeodesicOptimizer());
		v.registerPlugin(new ReferenceSurfaceOptimizer());
		v.registerPlugin(new CircularQuadOptimizer());
		v.registerPlugin(new ConicalOptimizer());
		v.registerPlugin(new InflateOptimizer());
	}
	
	private static void addLnFPlugins(JRViewer v) {
		v.registerPlugin(new LookAndFeelSwitch());
		v.registerPlugin(new FHLookAndFeel());
		v.registerPlugin(new TinyLookAndFeel());
		v.registerPlugin(new CrossPlatformLnF());
		v.registerPlugin(new NimbusLnF());
		v.registerPlugin(new SystemLookAndFeel());
	}
	
	private static void addDDGPlugins(JRViewer v) {
		v.registerPlugin(new ChristoffelTransfom());
		v.registerPlugin(AssociatedFamily.class);
	}
	
	
	public static void main(String[] args) throws Exception {
		JRHalfedgeViewer.initHalfedgeFronted();
		NativePathUtility.set("native");
		View.setIcon(ImageHook.getIcon("surface.png"));
		View.setTitle("VaryLab");
		JRViewer v = new JRViewer();
		v.getController().setManageLookAndFeel(true);
		v.setPropertiesFile("VaryLab.xml");
		v.setPropertiesResource(VaryLab.class, "VaryLab.xml");
		v.setShowPanelSlots(true, true, true, true);
		v.addContentSupport(ContentType.Raw);
		v.setShowToolBar(true);
		v.setShowMenuBar(true);
		v.addBasicUI();
		v.addContentUI();
		addVaryLabPlugins(v);
		v.registerPlugins(ConformalLab.createConformalPlugins());
		v.startup();
		v.getPlugin(HalfedgeInterface.class).set(new VHDS());
	}

}
