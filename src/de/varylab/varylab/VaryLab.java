package de.varylab.varylab;

import de.jreality.plugin.JRViewer;
import de.jreality.plugin.experimental.WebContentLoader;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.halfedgetools.plugin.buildin.topology.TopologyOperations;
import de.jtem.halfedgetools.plugin.visualizers.EdgeLengthVisualizer;
import de.jtem.halfedgetools.plugin.visualizers.FacePlanarityVisualizer;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.hds.adapter.VertexPositionAdapter;
import de.varylab.varylab.plugin.editor.HeightFieldEditor;
import de.varylab.varylab.plugin.generator.QuadMeshGenerator;
import de.varylab.varylab.plugin.meshoptimizer.EdgeLengthOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.GeodesicAngleOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.GeodesicLaplaceOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.GravitySpringOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.PlanarQuadsOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.WillmoreOptimizer;
import de.varylab.varylab.plugin.visualizers.OddVertexVisualizer;

public class VaryLab {

	private static void addVaryLabPlugins(JRViewer v) {
		
		v.registerPlugin(new HalfedgeInterfacePlugin<VVertex, VEdge, VFace, VHDS>(VHDS.class, new VertexPositionAdapter()));
		v.registerPlugin(new QuadMeshGenerator());
		v.registerPlugin(new HeightFieldEditor());
		
		v.registerPlugins(TopologyOperations.topologicalEditingJR(new VVertex()));
		
		v.registerPlugin(new EdgeLengthOptimizer());
		v.registerPlugin(new PlanarQuadsOptimizer());
		v.registerPlugin(new WillmoreOptimizer());
		v.registerPlugin(new GeodesicAngleOptimizer());
		v.registerPlugin(new GeodesicLaplaceOptimizer());
		v.registerPlugin(new GravitySpringOptimizer());
		
		v.registerPlugin(new EdgeLengthVisualizer());
		v.registerPlugin(new FacePlanarityVisualizer());
		v.registerPlugin(new OddVertexVisualizer());
		v.registerPlugin(new WebContentLoader());
	}
	
	
	public static void main(String[] args) {
		JRViewer v = new JRViewer();
		v.setPropertiesFile("VaryLab.xml");
		v.setPropertiesResource(VaryLab.class, "VaryLab.xml");
		v.setShowPanelSlots(true, true, true, true);
		v.setShowToolBar(true);
		v.setShowMenuBar(true);
		v.addBasicUI();
		v.addContentUI();
		addVaryLabPlugins(v);
		v.startup();
	}

}
