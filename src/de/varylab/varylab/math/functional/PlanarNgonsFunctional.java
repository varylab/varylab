package de.varylab.varylab.math.functional;

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
import de.varylab.varylab.math.functional.PlanarFacesAdapters.VolumeWeight;

public class PlanarNgonsFunctional <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> implements Functional<V, E, F> {
	
	
	@SuppressWarnings("unused")
	private VolumeWeight<F>
		weight = null;
	@SuppressWarnings("unused")
	private double
		scale = 1.0,
		alpha = 0.0;
	
	public PlanarNgonsFunctional(VolumeWeight<F> weight, double scale, double alpha) {
		this.weight = weight;
		this.scale = scale;
		this.alpha = alpha;
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
			for (F f : hds.getFaces()) { // flatness
				E.add(VolumeFunctionalUtils.calculateSumDetSquared(x,HalfEdgeUtils.boundaryVertices(f)));
			}
		}
		if(G != null) {
			G.setZero();
			evaluateGradient(hds, x, G);
		}

	}


	private <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void evaluateGradient(HDS hds, DomainValue x, Gradient G) {
		if(G != null) {
			for(F f : hds.getFaces()) {
				VolumeFunctionalUtils.addSumDetSquaredGradient(x, G, HalfEdgeUtils.boundaryVertices(f),scale);					
			}
		}
	}
	
	@Override
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> int getDimension(HDS hds) {
		return hds.numVertices() * 3;
	}

	@Override
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> int[][] getNonZeroPattern(HDS hds) {
		return null;
	}

	@Override
	public boolean hasHessian() {
		return false;
	}
	
}
