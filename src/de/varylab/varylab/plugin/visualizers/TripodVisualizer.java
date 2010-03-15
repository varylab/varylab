package de.varylab.varylab.plugin.visualizers;

import de.jreality.scene.SceneGraphComponent;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;

public class TripodVisualizer extends VisualizerPlugin {

	private SceneGraphComponent
		root = new SceneGraphComponent("Tripod");
	
	
	public TripodVisualizer() {
		
	}
	
	
	@Override
	public SceneGraphComponent getComponent() {
		return root;
	}
	
	@Override
	public String getName() {
		return "Tripod";
	}

}
