package de.varylab.varylab.optimization.constraint;

import java.util.Collection;

import de.jreality.math.Rn;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;


public class FixingConstraint implements Constraint{

	private boolean
		fixSelectionX = true,
		fixSelectionY = true,
		fixSelectionZ = true,
		fixBoundaryX = false,
		fixBoundaryY = false,
		fixBoundaryZ = false,
		innerBoundary = false,
		fixGlobalX = false,
		fixGlobalY = false,
		fixGlobalZ = false;
	
	private Collection<VVertex>
		selectedVertices = null;
	
	public FixingConstraint(
		Collection<VVertex> selectedVertices, 
		boolean fixSelectionX, 
		boolean fixSelectionY, 
		boolean fixSelectionZ, 
		boolean fixBoundaryX, 
		boolean fixBoundaryY, 
		boolean fixBoundaryZ, 
		boolean innerBoundaryMovements, 
		boolean fixGlobalX, 
		boolean fixGlobalY, 
		boolean fixGlobalZ
	) {
		this.fixSelectionX = fixSelectionX;
		this.fixSelectionY = fixSelectionY;
		this.fixSelectionZ = fixSelectionZ;
		this.selectedVertices = selectedVertices;
		this.fixBoundaryX = fixBoundaryX;
		this.fixBoundaryY = fixBoundaryY;
		this.fixBoundaryZ = fixBoundaryZ;
		this.innerBoundary = innerBoundaryMovements;
		this.fixGlobalX = fixGlobalX;
		this.fixGlobalY = fixGlobalY;
		this.fixGlobalZ = fixGlobalZ;
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
		for (VVertex v : selectedVertices){
			int i = v.getIndex();
			if (fixSelectionX) G.set(i * 3 + 0, 0.0);
			if (fixSelectionY) G.set(i * 3 + 1, 0.0);
			if (fixSelectionZ) G.set(i * 3 + 2, 0.0);
		}
		for (VVertex v : hds.getVertices()){
			if (!HalfEdgeUtils.isBoundaryVertex(v)){
				continue;
			}
			if (selectedVertices.contains(v)) {
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
				if (v1 == null) {
					throw new RuntimeException("cannot find next vertex on boundary in editGradient()");
				}
				double[] w1 = Rn.subtract(null, v1.getP(), v.getP());
				double[] grad = new double[] {G.get(i * 3 + 0), G.get(i * 3 + 1), G.get(i * 3 + 2), 0};
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
			if (fixGlobalX) G.set(i * 3 + 0, 0.0);
			if (fixGlobalY) G.set(i * 3 + 1, 0.0);
			if (fixGlobalZ) G.set(i * 3 + 2, 0.0);
		}
	}


	@Override
	public void editHessian(VHDS hds, int dim, DomainValue x, Hessian H) {
		for (Vertex<?,?,?> v : selectedVertices){
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
			if (selectedVertices.contains(v)) {
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
				if (fixGlobalX) H.set(i, j * 3 + 0, 0.0);
				if (fixGlobalY) H.set(i, j * 3 + 1, 0.0);
				if (fixGlobalZ) H.set(i, j * 3 + 2, 0.0);
			}
		}
	}
}