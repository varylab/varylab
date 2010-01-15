package de.varylab.varylab.math;

import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;


public class FixingConstraint implements Constraint{

	private boolean 
		fixBoundary = false,
		fixX = false,
		fixY = false,
		fixZ = false;
	
	
	public FixingConstraint(boolean fixBoundary, boolean fixX, boolean fixY, boolean fixZ) {
		this.fixBoundary = fixBoundary;
		this.fixX = fixX;
		this.fixY = fixY;
		this.fixZ = fixZ;
	}


	public void editGradient(VHDS hds, int dim, Gradient G) {
		for (VVertex v : hds.getVertices()){
			if (!v.isVariable()) {
				G.set(v.getIndex() * 3 + 0, 0.0);
				G.set(v.getIndex() * 3 + 1, 0.0);
				G.set(v.getIndex() * 3 + 2, 0.0);
			}
		}
		if (fixBoundary) {
			for (VVertex v : hds.getVertices()){
				if (HalfEdgeUtils.isBoundaryVertex(v)) {
					G.set(v.getIndex() * 3 + 0, 0.0);
					G.set(v.getIndex() * 3 + 1, 0.0);
					G.set(v.getIndex() * 3 + 2, 0.0);
				}
			}
		}
		for (int i = 0; i < dim / 3; i++) {
			if (fixX) G.set(i * 3 + 0, 0.0);
			if (fixY) G.set(i * 3 + 1, 0.0);
			if (fixZ) G.set(i * 3 + 2, 0.0);
		}
	}


	@Override
	public void editHessian(VHDS hds, int dim, Hessian G) {
		
	}


}