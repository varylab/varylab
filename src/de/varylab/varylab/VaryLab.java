package de.varylab.varylab;

import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.experimental.WebContentLoader;
import de.jreality.util.NativePathUtility;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.SelectionInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmFactory;
import de.jtem.halfedgetools.plugin.algorithm.subdivision.CatmullClarkPlugin;
import de.jtem.halfedgetools.plugin.algorithm.subdivision.LoopPlugin;
import de.jtem.halfedgetools.plugin.visualizers.DirichletEnergyVisualizer;
import de.jtem.halfedgetools.plugin.visualizers.EdgeLengthVisualizer;
import de.jtem.halfedgetools.plugin.visualizers.FacePlanarityVisualizer;
import de.jtem.halfedgetools.plugin.visualizers.NormalVisualizer;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.adapter.VPositionAdapter;
import de.varylab.varylab.hds.calculator.VPositionCalculator;
import de.varylab.varylab.hds.calculator.VSubdivisionCalculator;
import de.varylab.varylab.plugin.ConsolePlugin;
import de.varylab.varylab.plugin.editor.HeightFieldEditor;
import de.varylab.varylab.plugin.generator.QuadMeshGenerator;
import de.varylab.varylab.plugin.meshoptimizer.ANetOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.ConstantDirectionFieldPlugin;
import de.varylab.varylab.plugin.meshoptimizer.EdgeLengthOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.ElectrostaticOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.GeodesicAngleOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.GeodesicLaplaceOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.PlanarQuadsOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.SpringOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.WillmoreOptimizer;
import de.varylab.varylab.plugin.ui.OptimizationPanel;
import de.varylab.varylab.plugin.visualizers.CurvatureVisualizer;
import de.varylab.varylab.plugin.visualizers.OddVertexVisualizer;
import de.varylab.varylab.plugin.visualizers.StarPlanarityVisualizer;

public class VaryLab {

	private static void addVaryLabPlugins(JRViewer v) {
		HalfedgeInterface hif = new HalfedgeInterface();
		hif.addAdapter(new VPositionAdapter());
		hif.addCalculator(new VPositionCalculator());
		hif.addCalculator(new VSubdivisionCalculator());
		hif.set(new VHDS());
		v.registerPlugin(hif);
		v.registerPlugin(new SelectionInterface());
		v.registerPlugin(new OptimizationPanel());
		
		v.registerPlugin(new QuadMeshGenerator());
		v.registerPlugin(new HeightFieldEditor());
		v.registerPlugin(new EdgeLengthOptimizer());
		v.registerPlugin(new PlanarQuadsOptimizer());
		v.registerPlugin(new WillmoreOptimizer());
		v.registerPlugin(new GeodesicAngleOptimizer());
		v.registerPlugin(new GeodesicLaplaceOptimizer());
		v.registerPlugin(new ANetOptimizer());
		v.registerPlugin(new ConstantDirectionFieldPlugin());
		v.registerPlugin(new SpringOptimizer());
		v.registerPlugin(new ElectrostaticOptimizer());

		v.registerPlugin(new EdgeLengthVisualizer());
		v.registerPlugin(new FacePlanarityVisualizer());
		v.registerPlugin(new DirichletEnergyVisualizer());
		v.registerPlugin(new OddVertexVisualizer());
		v.registerPlugin(new CurvatureVisualizer());
		v.registerPlugin(new NormalVisualizer());
		v.registerPlugin(new StarPlanarityVisualizer());
		
		v.registerPlugin(new CatmullClarkPlugin());
		v.registerPlugin(new LoopPlugin());
		v.registerPlugin(new WebContentLoader());
		v.registerPlugin(new ConsolePlugin());
		v.registerPlugins(AlgorithmFactory.createGeometryPlugins());
		v.registerPlugins(AlgorithmFactory.createSubdivisionPlugins());
		v.registerPlugins(AlgorithmFactory.createTopologyPlugins());
	}
	
	
	public static void main(String[] args) {
		NativePathUtility.set("native");
		JRViewer v = new JRViewer();
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
