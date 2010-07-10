package de.varylab.varylab.plugin.smoothing;

import java.util.HashMap;
import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;

public class LaplacianSmoothing {

	public static void smoothCombinatorially(VHDS hds, AdapterSet as, boolean ignoreBoundary) {
		HashMap<VVertex, double[]> oldPositionMap = new HashMap<VVertex, double[]>();
		for(VVertex v : hds.getVertices()) {
			oldPositionMap.put(v, as.get(Position.class, v, double[].class));
		}
		for(VVertex v : hds.getVertices()) {
			if(ignoreBoundary && HalfEdgeUtils.isBoundaryVertex(v)) {
				continue;
			}
			double[] newPos = oldPositionMap.get(v);
			List<VVertex> neighs = HalfEdgeUtils.neighboringVertices(v);
			Rn.times(newPos, neighs.size() , newPos);
			for(VVertex nv : neighs) {
				Rn.add(newPos,newPos,oldPositionMap.get(nv));
			}
			Rn.times(newPos, 1.0/(2*neighs.size()), newPos);
			
			as.set(Position.class, v, newPos);
		}
	}
	
}
