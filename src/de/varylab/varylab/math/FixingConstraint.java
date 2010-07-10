package de.varylab.varylab.math;

import java.util.Collection;

import de.jreality.math.Rn;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;


public class FixingConstraint implements Constraint{

	private boolean
		fixSelectionX = true,
		fixSelectionY = true,
		fixSelectionZ = true,
		fixBoundaryX = false,
		fixBoundaryY = false,
		fixBoundaryZ = false,
		innerBoundary = false,
		fixX = false,
		fixY = false,
		fixZ = false;
	
	private Collection<? extends Vertex<?,?,?>>
		fixedVerts = null;
	
	public <V extends Vertex<?,?,?>> FixingConstraint(
		Collection<V> fixed, 
		boolean fixSelectionX, 
		boolean fixSelectionY, 
		boolean fixSelectionZ, 
		boolean fixBoundaryX, 
		boolean fixBoundaryY, 
		boolean fixBoundaryZ, 
		boolean innerBoundaryMovements, 
		boolean fixX, 
		boolean fixY, 
		boolean fixZ
	) {
		this.fixSelectionX = fixSelectionX;
		this.fixSelectionY = fixSelectionY;
		this.fixSelectionZ = fixSelectionZ;
		this.fixedVerts = fixed;
		this.fixBoundaryX = fixBoundaryX;
		this.fixBoundaryY = fixBoundaryY;
		this.fixBoundaryZ = fixBoundaryZ;
		this.innerBoundary = innerBoundaryMovements;
		this.fixX = fixX;
		this.fixY = fixY;
		this.fixZ = fixZ;
	}


	@Override
	public void editGradient(VHDS hds, int dim, DomainValue x, Gradient G) {
		for (VVertex v : hds.getVertices()){
			if (!v.isVariable()) {
				G.set(v.getIndex() * 3 + 0, 0.0);
				G.set(v.getIndex() * 3 + 1, 0.0);
				G.set(v.getIndex() * 3 + 2, 0.0);
			}
		}
		for (Vertex<?,?,?> v : fixedVerts){
			int i = v.getIndex();
			if (fixSelectionX) G.set(i * 3 + 0, 0.0);
			if (fixSelectionY) G.set(i * 3 + 1, 0.0);
			if (fixSelectionZ) G.set(i * 3 + 2, 0.0);
		}
		for (VVertex v : hds.getVertices()){
			if (!HalfEdgeUtils.isBoundaryVertex(v)){
				continue;
			}
			if (fixedVerts.contains(v)) {
				continue;
			}
			int i = v.getIndex();
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
				if (fixBoundaryX) G.set(i * 3 + 0, 0.0);
				if (fixBoundaryY) G.set(i * 3 + 1, 0.0);
				if (fixBoundaryZ) G.set(i * 3 + 2, 0.0);
			}
		}
		for (int i = 0; i < dim / 3; i++) {
			if (fixX) G.set(i * 3 + 0, 0.0);
			if (fixY) G.set(i * 3 + 1, 0.0);
			if (fixZ) G.set(i * 3 + 2, 0.0);
		}
	}


	@Override
	public void editHessian(VHDS hds, int dim, DomainValue x, Hessian H) {
		for (Vertex<?,?,?> v : fixedVerts){
			int i = v.getIndex();
			for (int j = 0; j < dim; j++) {
				if (fixSelectionX) H.set(i * 3 + 0,j, 0.0);
				if (fixSelectionY) H.set(i * 3 + 1,j, 0.0);
				if (fixSelectionZ) H.set(i * 3 + 2,j, 0.0);
				if (fixSelectionX) H.set(j,i * 3 + 0, 0.0);
				if (fixSelectionY) H.set(j,i * 3 + 1, 0.0);
				if (fixSelectionZ) H.set(j,i * 3 + 2, 0.0);
			}
		}
		for (VVertex v : hds.getVertices()){
			if (!HalfEdgeUtils.isBoundaryVertex(v)){
				continue;
			}
			if (fixedVerts.contains(v)) {
				continue;
			}
			int i = v.getIndex();
			for (int j = 0; j < dim; j++) {
				if (fixBoundaryX) H.set(i * 3 + 0, j, 0.0);
				if (fixBoundaryY) H.set(i * 3 + 1, j, 0.0);
				if (fixBoundaryZ) H.set(i * 3 + 2, j, 0.0);
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