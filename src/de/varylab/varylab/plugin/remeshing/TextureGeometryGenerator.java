package de.varylab.varylab.plugin.remeshing;

import de.jreality.math.Matrix;
import de.jreality.plugin.content.ContentAppearance;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.plugin.image.ImageHook;

public class TextureGeometryGenerator extends AlgorithmPlugin {

	private ContentAppearance contentAppearance;

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.TextureRemeshing;
	}

	@Override
	public String getAlgorithmName() {
		return "TextureGeometry";
	}
	
	@Override
	public double getPriority() {
		return 6.0;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		
		Matrix texMatrix = contentAppearance.getAppearanceInspector().getTextureMatrix();
		
		HDS newHDS = QuadTextureUtility.createTextureGeometry(hds, a, hif, texMatrix);

		HalfedgeLayer hel = new HalfedgeLayer(hif);
		hel.set(newHDS);
		hel.setName("Texture Geometry of " + hif.getActiveLayer().getName());
		hif.addLayer(hel);
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.icon = ImageHook.getIcon("textureEdge.png",16,16);
		return info;
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		contentAppearance = c.getPlugin(ContentAppearance.class);
	}
}
