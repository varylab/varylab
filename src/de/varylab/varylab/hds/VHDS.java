package de.varylab.varylab.hds;

import de.jreality.math.Matrix;
import de.jtem.halfedge.HalfEdgeDataStructure;

public class VHDS extends HalfEdgeDataStructure<VVertex, VEdge, VFace> {

	public VHDS() {
		super(VVertex.class, VEdge.class, VFace.class);
	}

	public void applyTransformation(Matrix t) {
		for(VVertex v : getVertices()) {
			v.applyTransformation(t);
		}
	}
	
}
