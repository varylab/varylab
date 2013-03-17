package de.varylab.varylab.plugin.selection;

import de.jreality.plugin.content.ContentAppearance;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.plugin.image.ImageHook;
import de.varylab.varylab.plugin.remeshing.QuadTextureUtility;

public class TextureVertexSelection extends AlgorithmPlugin {

	private ContentAppearance contentAppearance;

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.TextureRemeshing;
	}

	@Override
	public String getAlgorithmName() {
		return "TextureVertex";
	}
	
	@Override
	public double getPriority() {
		return 1.0;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		HalfedgeSelection hes = hif.getSelection();
		hes.addAll(QuadTextureUtility.findTextureVertices(hds, a, contentAppearance.getAppearanceInspector().getTextureMatrix(), false));
		hif.setSelection(hes);
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.icon = ImageHook.getIcon("textureVertex.png",16,16);
		return info;
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		contentAppearance = c.getPlugin(ContentAppearance.class);
	}
}
