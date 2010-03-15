package de.varylab.varylab.math;

import java.util.Set;

import de.jreality.math.Rn;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;


public class FixingConstraint implements Constraint{

	private boolean
		fixSelection = false,
		fixBoundary = false,
		innerBoundary = false,
		fixX = false,
		fixY = false,
		fixZ = false;
	
	private Set<? extends Vertex<?,?,?>>
		fixedVerts = null;
	
	public <V extends Vertex<?,?,?>> FixingConstraint(Set<V> fixed, boolean fixSelection, boolean fixBoundary, boolean innerBoundaryMovements, boolean fixX, boolean fixY, boolean fixZ) {
		this.fixSelection = fixSelection;
		this.fixedVerts = fixed;
		this.fixBoundary = fixBoundary;
		this.innerBoundary = innerBoundaryMovements;
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
		if(fixSelection) {
			for (Vertex<?,?,?> v : fixedVerts){
				int i = v.getIndex();
				G.set(i * 3 + 0, 0.0);
				G.set(i * 3 + 1, 0.0);
				G.set(i * 3 + 2, 0.0);
			}
		}
		if (fixBoundary) {
			for (VVertex v : hds.getVertices()){
				int i = v.getIndex();
				if (HalfEdgeUtils.isBoundaryVertex(v)) {
					if (innerBoundary) { // project onto boundary
						VVertex v1 = null;
						for (VEdge e : HalfEdgeUtils.incomingEdges(v)) {
							if (e.getLeftFace() == null){
								v1 = e.getStartVertex();
								break;
							}
						}
						assert v1 != null;
						double[] w1 = Rn.subtract(null, v1.position, v.position);
						double[] grad = new double[] {G.get(i * 3 + 0), G.get(i * 3 + 1), G.get(i * 3 + 2)};
						Rn.projectOnto(grad, grad, w1);
						G.set(i * 3 + 0, grad[0]);
						G.set(i * 3 + 1, grad[1]);
						G.set(i * 3 + 2, grad[2]);
					} else { // set zero
						G.set(i * 3 + 0, 0.0);
						G.set(i * 3 + 1, 0.0);
						G.set(i * 3 + 2, 0.0);
					}
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
	public void editHessian(VHDS hds, int dim, Hessian H) {
		if(fixSelection) {
			for (Vertex<?,?,?> v : fixedVerts){
				int i = v.getIndex();
				for (int j = 0; j < dim; j++) {
					H.set(i * 3 + 0,j, 0.0);
					H.set(i * 3 + 1,j, 0.0);
					H.set(i * 3 + 2,j, 0.0);
					H.set(j,i * 3 + 0, 0.0);
					H.set(j,i * 3 + 1, 0.0);
					H.set(j,i * 3 + 2, 0.0);
				}
			}
		}
		if (fixBoundary) {
			for (VVertex v : hds.getVertices()){
				int i = v.getIndex();
				if (HalfEdgeUtils.isBoundaryVertex(v)) {
					for (int j = 0; j < dim; j++) {
						H.set(i * 3 + 0, j, 0.0);
						H.set(i * 3 + 1, j, 0.0);
						H.set(i * 3 + 2, j, 0.0);
					}
				}
			}
		}
		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim / 3; j++) {
				if (fixX) H.set(i, j * 3 + 0, 0.0);
				if (fixY) H.set(i, j * 3 + 1, 0.0);
				if (fixZ) H.set(i, j * 3 + 2, 0.0);
			}
		}
	}
}