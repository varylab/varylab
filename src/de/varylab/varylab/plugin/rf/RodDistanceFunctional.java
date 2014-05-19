package de.varylab.varylab.plugin.rf;

import de.jreality.math.Rn;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.FunctionalUtils;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.varylab.varylab.functional.adapter.Length;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VVertex;

public class RodDistanceFunctional implements Functional<VVertex, VEdge, VFace> {

	private RodConnectivityAdapter
		rca = null;
	
	private Length<VEdge>
		length = null;
	
	public RodDistanceFunctional(RodConnectivityAdapter a, Length<VEdge> l) {
		rca = a;
		length = l;
	}
	
	@Override
	public <
		HDS extends HalfEdgeDataStructure<VVertex, VEdge, VFace>
	> void evaluate(HDS hds, DomainValue x, Energy E, Gradient G, Hessian H) {
		double energy = 0.0;
		if(G != null) {
			G.setZero();
		}
		for(VEdge e : hds.getEdges()) {
			
			double[] p1 = FunctionalUtils.getPosition(e.getStartVertex(), x, null);
			double[] p2 = FunctionalUtils.getPosition(e.getTargetVertex(), x, null);
			double[] p2p1 = Rn.subtract(null, p2, p1);
			
			VEdge next = rca.getE(e, null);
			double[] q1 = FunctionalUtils.getPosition(next.getStartVertex(), x, null);
			double[] q2 = FunctionalUtils.getPosition(next.getTargetVertex(), x, null);
			double[] q2q1 = Rn.subtract(null, q2, q1);
			
			double[] normal = Rn.crossProduct(null, p2p1, q2q1);
			
			double norm = Rn.euclideanNorm(normal);
			if(norm == 0) {
				continue;
			}
			double[] q1p1 = Rn.subtract(null, q1, p1);
			double[] q2p2 = Rn.subtract(null, q2, p2);
			
			double d = Rn.innerProduct(q1p1, normal)/norm;
			
			double diffToTarget = d - length.getTargetLength(e);
			
			energy += Math.pow(diffToTarget,2.0);
			
			if(G != null) {
				double s = 1/Math.pow(norm, 3.0);
				
				double[] gp1 = Rn.times(null, -2.0*diffToTarget*s, Rn.crossProduct(null, q2q1, Rn.crossProduct(null, normal, Rn.crossProduct(null, q2p2, normal))));
				double[] gp2 = Rn.times(null, 2.0*diffToTarget*s, Rn.crossProduct(null, q2q1, Rn.crossProduct(null, normal, Rn.crossProduct(null, q1p1, normal))));
				
				double[] gq1 = Rn.times(null, 2.0*diffToTarget*s, Rn.crossProduct(null, p2p1, Rn.crossProduct(null, normal, Rn.crossProduct(null, q2p2, normal))));
				double[] gq2 = Rn.times(null, -2.0*diffToTarget*s, Rn.crossProduct(null, p2p1, Rn.crossProduct(null, normal, Rn.crossProduct(null, q1p1, normal))));
				
				FunctionalUtils.addVectorToGradient(G, 3*e.getStartVertex().getIndex(), gp1);
				FunctionalUtils.addVectorToGradient(G, 3*e.getTargetVertex().getIndex(), gp2);
				FunctionalUtils.addVectorToGradient(G, 3*next.getStartVertex().getIndex(), gq1);
				FunctionalUtils.addVectorToGradient(G, 3*next.getTargetVertex().getIndex(), gq2);
			}
		}
		if(E != null) {
			E.set(energy);
		}
	}

	@Override
	public boolean hasHessian() {
		return false;
	}

	@Override
	public boolean hasGradient() {
		return true;
	}

	@Override
	public int getDimension(HalfEdgeDataStructure hds) {
		return 3*hds.numVertices();
	}

	@Override
	public int[][] getNonZeroPattern(HalfEdgeDataStructure hds) {
		return null;
	}

}
