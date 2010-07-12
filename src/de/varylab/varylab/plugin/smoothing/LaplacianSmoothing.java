package de.varylab.varylab.plugin.smoothing;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;

public class LaplacianSmoothing {

	public static <V extends Vertex<V,E,F>,E extends Edge<V,E,F>, F extends Face<V,E,F>, HDS extends HalfEdgeDataStructure<V,E,F> > void smoothCombinatorially(HDS hds, Set<V> vertices, AdapterSet as, boolean ignoreBoundary) {
		HashMap<V, double[]> oldPositionMap = new HashMap<V, double[]>();
		for(V v : hds.getVertices()) {
			oldPositionMap.put(v, as.get(Position.class, v, double[].class));
		}
		if(vertices.size() == 0) {
			vertices.addAll(hds.getVertices());
		}
		for(V v : vertices) {
			if(ignoreBoundary && HalfEdgeUtils.isBoundaryVertex(v)) {
				continue;
			}
			double[] newPos = oldPositionMap.get(v);
			List<V> neighs = HalfEdgeUtils.neighboringVertices(v);
			Rn.times(newPos, neighs.size() , newPos);
			for(V nv : neighs) {
				Rn.add(newPos,newPos,oldPositionMap.get(nv));
			}
			Rn.times(newPos, 1.0/(2*neighs.size()), newPos);
			System.out.println(Arrays.toString(newPos));
			as.set(Position.class, v, newPos);
		}
	}
	
}
