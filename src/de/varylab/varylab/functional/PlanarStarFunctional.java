package de.varylab.varylab.functional;

import java.util.List;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;

public class PlanarStarFunctional <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> implements Functional<V, E, F> {

	private double
		scale = 1.0;
	
	public PlanarStarFunctional(double scale) {
		this.scale = scale;
	}
	
	
	@Override
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void evaluate(
		HDS hds,
		DomainValue x, 
		Energy E, 
		Gradient G, 
		Hessian H
	) {
		if(E != null) {
			E.setZero();
			for (V v: hds.getVertices()) { // flatness
				List<V> neighbors = HalfEdgeUtils.neighboringVertices(v);
				neighbors.add(v);
				E.add(VolumeFunctionalUtils.calculateSumDetSquared(x, neighbors));
				if(G != null) {
					VolumeFunctionalUtils.addSumDetSquaredGradient(x, G, neighbors,scale);
				}
			}
		}
		if(G != null) {
			evaluateGradient(hds, x, G);
		}
	}


	private <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void evaluateGradient(HDS hds, DomainValue x, Gradient G) {
		G.setZero();
		for (V v: hds.getVertices()) { // flatness
			List<V> neighbors = HalfEdgeUtils.neighboringVertices(v);
			neighbors.add(v);
			VolumeFunctionalUtils.addSumDetSquaredGradient(x, G, neighbors,scale);
		}
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int getDimension(HDS hds) {
		return hds.numVertices() * 3;
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int[][] getNonZeroPattern(HDS hds) {
		return null;
	}
	
    
    @Override
	public boolean hasHessian() {
    	return false;
    }


	public void setScale(double s) {
		scale = s;
	}
}
