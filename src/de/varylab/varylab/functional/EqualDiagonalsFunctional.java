package de.varylab.varylab.functional;

import static de.jtem.halfedge.util.HalfEdgeUtils.boundaryVertices;
import static de.jtem.halfedgetools.functional.FunctionalUtils.addVectorToGradient;
import static de.jtem.halfedgetools.functional.FunctionalUtils.getPosition;

import java.util.Iterator;
import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Weight;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;

public class EqualDiagonalsFunctional <
	V extends Vertex<V, E, F>, 
	E extends Edge<V, E, F>, 
	F extends Face<V, E, F>> 
implements Functional<V, E, F> {
	
	private double[]
	    v1 = new double[3],
	    v2 = new double[3],
	    v3 = new double[3],
	    v4 = new double[3],
	    vecm = new double[3],
	    vecl = new double[3];
	private AdapterSet 
		aSet = null;
		
	public EqualDiagonalsFunctional(AdapterSet a) {
		aSet = a;
	}
	
	@Override
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>>
	void evaluate(HDS hds, DomainValue x, Energy E, Gradient G, Hessian H) {
		if (E != null) E.setZero();
		if (G != null) G.setZero();
		for (F f : hds.getFaces()) {
			Double w = aSet.getDefault(Weight.class, f, 1.0);
			if (w == 0.0) continue;
			List<V> b = boundaryVertices(f);
			if (b.size() != 4) continue;
			Iterator<V> vIt = b.iterator();
			V vv1 = vIt.next();
			V vv2 = vIt.next();
			V vv3 = vIt.next();
			V vv4 = vIt.next();
			getPosition(vv1, x, v1);
			getPosition(vv2, x, v2);
			getPosition(vv3, x, v3);
			getPosition(vv4, x, v4);
			double l = Rn.euclideanDistanceSquared(v1, v3);
			double m = Rn.euclideanDistanceSquared(v2, v4);
			double lDif = l - m;
			if (E != null) {
				E.add(w * lDif * lDif);
			}
			if (G != null) {
				Rn.subtract(vecm, v1, v3);
				Rn.subtract(vecl, v2, v4);
				Rn.times(vecm, w*4*(l - m), vecm);
				Rn.times(vecl, -w*4*(l - m), vecl);
				addVectorToGradient(G, 3*vv1.getIndex(), vecm);
				addVectorToGradient(G, 3*vv2.getIndex(), vecl);
				Rn.times(vecm, -1, vecm);
				Rn.times(vecl, -1, vecl);
				addVectorToGradient(G, 3*vv3.getIndex(), vecm);
				addVectorToGradient(G, 3*vv4.getIndex(), vecl);
			}
		}
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int getDimension(HDS hds) {
		return 3*hds.numVertices();
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int[][] getNonZeroPattern(HDS hds) {
		return null;
	}

	@Override
	public boolean hasHessian() {
		return false;
	}

}
