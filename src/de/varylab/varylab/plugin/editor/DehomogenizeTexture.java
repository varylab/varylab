package de.varylab.varylab.plugin.editor;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.TexturePosition;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition2d;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;

public class DehomogenizeTexture extends AlgorithmPlugin {

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Editing;
	}

	@Override
	public String getAlgorithmName() {
		return "Dehomogenize Texture";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		for (V v : hds.getVertices()) {
			double[] t = a.getD(TexturePosition2d.class, v);
			a.set(TexturePosition.class, v, t);
		}
		hi.update();
	}

}
