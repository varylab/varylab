package de.varylab.varylab.plugin.nurbs.algorithm;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.TexturePosition;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsUVAdapter;

public class UVUnroll extends AlgorithmPlugin {

	@Override
	public String getCategory() {
		return "NURBS";
	}
	
	@Override
	public String getAlgorithmName() {
		return "Unroll using uv coordinates";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		AdapterSet as = hi.getAdapters();
		as.addAll(hi.getActiveVolatileAdapters());
		NurbsUVAdapter nurbsAdapter = as.query(NurbsUVAdapter.class);
		if(nurbsAdapter != null) {
			for(V v : hds.getVertices()) {
				as.set(TexturePosition.class,v,nurbsAdapter.getV(v, null));
			}
		} else {
			throw new RuntimeException("No nurbs surface on active layer.");
		}
		hi.update();
	}

}
