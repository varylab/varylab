package de.varylab.varylab;

import javax.swing.UIManager;

import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.ConsolePlugin;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.scene.Sky;
import de.jreality.util.NativePathUtility;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgePluginFactory;
import de.jtem.halfedgetools.plugin.algorithm.subdivision.CatmullClarkPlugin;
import de.jtem.halfedgetools.plugin.algorithm.subdivision.LoopPlugin;
import de.jtem.halfedgetools.plugin.visualizers.DirichletEnergyVisualizer;
import de.jtem.halfedgetools.plugin.visualizers.EdgeLengthVisualizer;
import de.jtem.halfedgetools.plugin.visualizers.FacePlanarityVisualizer;
import de.jtem.halfedgetools.plugin.visualizers.NodeIndexVisualizer;
import de.jtem.halfedgetools.plugin.visualizers.NormalVisualizer;
import de.jtem.jrworkspace.plugin.lnfswitch.LookAndFeelSwitch;
import de.jtem.jrworkspace.plugin.lnfswitch.plugin.CrossPlatformLnF;
import de.jtem.jrworkspace.plugin.lnfswitch.plugin.NimbusLnF;
import de.jtem.jrworkspace.plugin.lnfswitch.plugin.SystemLookAndFeel;
import de.varylab.discreteconformal.plugin.DiscreteConformalPlugin;
import de.varylab.varylab.hds.adapter.GeodesicLabelAdapter;
import de.varylab.varylab.hds.adapter.NodeWeigthAdapter;
import de.varylab.varylab.hds.adapter.VPositionAdapter;
import de.varylab.varylab.hds.adapter.VTexCoordAdapter;
import de.varylab.varylab.hds.calculator.VPositionCalculator;
import de.varylab.varylab.hds.calculator.VSubdivisionCalculator;
import de.varylab.varylab.plugin.editor.HeightFieldEditor;
import de.varylab.varylab.plugin.editor.Toolbox;
import de.varylab.varylab.plugin.editor.VertexEditorPlugin;
import de.varylab.varylab.plugin.generator.HexMeshGenerator;
import de.varylab.varylab.plugin.generator.QuadMeshGenerator;
import de.varylab.varylab.plugin.generator.SimpleRoofGenerator;
import de.varylab.varylab.plugin.io.OBJExportPlugin;
import de.varylab.varylab.plugin.lnf.FHLookAndFeel;
import de.varylab.varylab.plugin.lnf.TinyLookAndFeel;
import de.varylab.varylab.plugin.meshoptimizer.ANetOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.CircularQuadOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.ConstantDirectionFieldPlugin;
import de.varylab.varylab.plugin.meshoptimizer.EdgeLengthOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.ElectrostaticOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.ExteriorGeodesicOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.GeodesicAngleOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.GeodesicLaplaceOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.PlanarNGonsOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.PlanarQuadsOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.ReferenceSurfaceOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.SpringOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.WillmoreOptimizer;
import de.varylab.varylab.plugin.remeshing.SurfaceRemeshingPlugin;
import de.varylab.varylab.plugin.selection.GeodesicSelection;
import de.varylab.varylab.plugin.selection.LatticeSelection;
import de.varylab.varylab.plugin.selection.StripSelection;
import de.varylab.varylab.plugin.subdivision.RemoveGeodesicPlugin;
import de.varylab.varylab.plugin.subdivision.RoofSubdivisionPlugin;
import de.varylab.varylab.plugin.subdivision.SplitFacePlugin;
import de.varylab.varylab.plugin.subdivision.StripSubdivisionPlugin;
import de.varylab.varylab.plugin.ui.AngleCalculatorPlugin;
import de.varylab.varylab.plugin.ui.OptimizationPanel;
import de.varylab.varylab.plugin.ui.image.ImageHook;
import de.varylab.varylab.plugin.ui.nodeeditor.NodePropertyEditor;
import de.varylab.varylab.plugin.visualizers.CircularityVisualizer;
import de.varylab.varylab.plugin.visualizers.CurvatureVisualizer;
import de.varylab.varylab.plugin.visualizers.GeodesicLabelVisualizer;
import de.varylab.varylab.plugin.visualizers.HyperbolicPatchVisualizer;
import de.varylab.varylab.plugin.visualizers.OddVertexVisualizer;
import de.varylab.varylab.plugin.visualizers.StarPlanarityVisualizer;
import de.varylab.varylab.plugin.visualizers.WeightsVisualizer;

public class VaryLab {

	private static void addVaryLabPlugins(JRViewer v) {
		HalfedgeInterface hif = new HalfedgeInterface();
		hif.addAdapter(new VPositionAdapter());
		hif.addAdapter(new VTexCoordAdapter());
		hif.addAdapter(new NodeWeigthAdapter());
		hif.addAdapter(new GeodesicLabelAdapter());
		hif.addCalculator(new VPositionCalculator());
		hif.addCalculator(new VSubdivisionCalculator());
		v.registerPlugin(hif);
		v.registerPlugin(new OptimizationPanel());
		v.registerPlugin(new VertexEditorPlugin());
		
		v.registerPlugin(new QuadMeshGenerator());
		v.registerPlugin(new HexMeshGenerator());
		v.registerPlugin(new SimpleRoofGenerator());
		
		v.registerPlugin(new HeightFieldEditor());
		
		addOptimizationPlugins(v);
		addLnFPlugins(v);
		addVisualizerPlugins(v);
		
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
		
//		v.registerPlugin(new HalfedgeDebuggerPlugin());
//		v.registerPlugin(new WebContentLoader());
		
		v.registerPlugin(new Sky());
		v.registerPlugin(new AngleCalculatorPlugin());
		v.registerPlugin(new NodePropertyEditor());
		v.registerPlugin(new RemoveGeodesicPlugin());
		v.registerPlugin(new GeodesicSelection());
		v.registerPlugin(new LatticeSelection());
		v.registerPlugin(new StripSelection());
	}


	private static void addVisualizerPlugins(JRViewer v) {
		v.registerPlugin(new EdgeLengthVisualizer());
		v.registerPlugin(new FacePlanarityVisualizer());
		v.registerPlugin(new DirichletEnergyVisualizer());
		v.registerPlugin(new OddVertexVisualizer());
		v.registerPlugin(new CurvatureVisualizer());
		v.registerPlugin(new NormalVisualizer());
		v.registerPlugin(new StarPlanarityVisualizer());
		v.registerPlugin(new HyperbolicPatchVisualizer());
		v.registerPlugin(new NodeIndexVisualizer());
		v.registerPlugin(new WeightsVisualizer());
		v.registerPlugin(new GeodesicLabelVisualizer());
		v.registerPlugin(new CircularityVisualizer());
	}


	private static void addOptimizationPlugins(JRViewer v) {
		v.registerPlugin(new EdgeLengthOptimizer());
		v.registerPlugin(new PlanarQuadsOptimizer());
		v.registerPlugin(new WillmoreOptimizer());
		v.registerPlugin(new GeodesicAngleOptimizer());
		v.registerPlugin(new GeodesicLaplaceOptimizer());
		v.registerPlugin(new ANetOptimizer());
		v.registerPlugin(new ConstantDirectionFieldPlugin());
		v.registerPlugin(new SpringOptimizer());
		v.registerPlugin(new ElectrostaticOptimizer());
		v.registerPlugin(new PlanarNGonsOptimizer());
		v.registerPlugin(new ExteriorGeodesicOptimizer());
		v.registerPlugin(new ReferenceSurfaceOptimizer());
		v.registerPlugin(new CircularQuadOptimizer());
	}
	
	private static void addLnFPlugins(JRViewer v) {
		v.registerPlugin(new LookAndFeelSwitch());
		v.registerPlugin(new FHLookAndFeel());
		v.registerPlugin(new TinyLookAndFeel());
		v.registerPlugin(new CrossPlatformLnF());
		v.registerPlugin(new NimbusLnF());
		v.registerPlugin(new SystemLookAndFeel());
	}
	
	
	public static void main(String[] args) throws Exception {
		System.setProperty("de.jreality.scene.Viewer", "de.jreality.jogl.GLJPanelViewer");
		UIManager.getDefaults().put("Slider.paintValue", false);
		NativePathUtility.set("native");
		View.setIcon(ImageHook.getIcon("surface.png"));
		View.setTitle("VaryLab");
		JRViewer v = new JRViewer();
		v.getController().setManageLookAndFeel(true);
		v.setPropertiesFile("VaryLab.xml");
		v.setPropertiesResource(VaryLab.class, "VaryLab.xml");
		v.setShowPanelSlots(true, true, true, true);
		v.addContentSupport(ContentType.CenteredAndScaled);
		v.setShowToolBar(true);
		v.setShowMenuBar(true);
		v.addBasicUI();
		v.addContentUI();
		addVaryLabPlugins(v);
		v.startup();
	}

}
