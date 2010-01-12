package de.varylab.varylab.hds;

import de.jtem.halfedge.HalfEdgeDataStructure;

public class VHDS extends HalfEdgeDataStructure<VVertex, VEdge, VFace> {

	public VHDS() {
		super(VVertex.class, VEdge.class, VFace.class);
	}

}
