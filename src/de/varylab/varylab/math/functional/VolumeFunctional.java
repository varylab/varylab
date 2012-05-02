package de.varylab.varylab.math.functional;

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
import de.varylab.varylab.math.functional.PlanarFacesAdapters.VolumeWeight;

public class VolumeFunctional <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> implements Functional<V, E, F> {

	private VolumeWeight<F>
		weight = null;
	
	private double
		scale = 1.0,
		alpha = 0.0;
	
	public VolumeFunctional(VolumeWeight<F> weight, double scale, double alpha) {
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
		if (E != null) {
			E.setZero();
			for (F f : hds.getFaces()) { // flatness
				List<V> boundaryVertices = HalfEdgeUtils.boundaryVertices(f);
				if (boundaryVertices.size() != 4) {
					continue;
				}
				double detS = VolumeFunctionalUtils.detSquared(x, boundaryVertices);
				double weightS = weight.getWeight(f) * weight.getWeight(f);
				E.add(detS * weightS * scale);
			}
		}
		if (G != null) {
			evaluateGradient(hds, x, G, null, alpha);
		}
//		if (alpha != 0.0) {
//			for (V v : hds.getVertices()) { // minimal movement
//				if (HalfEdgeUtils.isBoundaryVertex(v)) continue;
//				int i = v.getIndex();
//				double[] startPoint = {startData[i * 3 + 0], startData[i * 3 + 1], startData[i * 3 + 2], 1.0};
//				result += alpha * Pn.distanceBetween(startPoint, v.getPosition(), Pn.EUCLIDEAN);
//			}
//		}
	}

	public void evaluateGradient(
		HalfEdgeDataStructure<V, E, F> data, 
		DomainValue x, 
		Gradient G, 
		double[] startData, 
		double alpha
	){
		G.setZero();
		for (F f : data.getFaces()){ // flatness
			List<V> bdVerts = HalfEdgeUtils.boundaryVertices(f);
			
			if (bdVerts.size() != 4)
				continue;
			
			double weight2 = weight.getWeight(f) * weight.getWeight(f);
			VolumeFunctionalUtils.addDetSquaredGradient(x, G, bdVerts, weight2*scale);
		}
//		if (alpha != 0.0) {
//			for (V v : data.getVertices()) { // minimal movement
//				if (HalfEdgeUtils.isBoundaryVertex(v)) continue;
//				int i = v.getIndex();
//				double[] pos = v.getPosition();
//				double[] startPoint = {startData[i * 3 + 0], startData[i * 3 + 1], startData[i * 3 + 2], 1.0};
//				double factor = Pn.distanceBetween(startPoint, v.getPosition(), Pn.EUCLIDEAN);
//				if (factor > 1E-4) {
//					result[i * 3 + 0] += alpha * (pos[0] - startPoint[0]) / factor;
//					result[i * 3 + 1] += alpha * (pos[1] - startPoint[1]) / factor;
//					result[i * 3 + 2] += alpha * (pos[2] - startPoint[2]) / factor;
//				}
//			}
//		}
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
	
	public void setWeight(VolumeWeight<F> weightFunction) {
		this.weight = weightFunction;
	}
	

    
}
