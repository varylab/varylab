package de.varylab.varylab.math.functional;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.FunctionalUtils;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;

public class ElectrostaticSphereFunctional <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> implements Functional<V, E, F> {

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
		}
		if (G != null) {
			G.setZero();
		}
		int nsq = hds.numVertices() * hds.numVertices();
		double[] vPos = new double[3];
		double[] wPos = new double[3];
		double[] dir = new double[3];
		double[] g = new double[3];
		for (V v : hds.getVertices()) {
			FunctionalUtils.getPosition(v, x, vPos);
			// electrostatic term
			for (V w : hds.getVertices()) {
				if (v == w) continue;
				FunctionalUtils.getPosition(w, x, wPos);
				Rn.subtract(dir, wPos, vPos);
				double dsq = Rn.innerProduct(dir, dir);
				if (E != null) {
					E.add(1 / dsq);
				}
				if (G != null) {
					Rn.times(g, 4 / (dsq * dsq), dir);
					FunctionalUtils.addVectorToGradient(G, v.getIndex() * 3, g);
				}
			}
			// spherical term
			double vdotv = Rn.innerProduct(vPos, vPos) - 1;
			if (E != null) {
				double ds = vdotv * vdotv;
				E.add(ds * nsq);
			}
			if (G != null) {
				double factor = 4 * nsq * vdotv;
				Rn.times(g, factor, vPos);
				FunctionalUtils.addVectorToGradient(G, v.getIndex() * 3, g);
			}
		}
	}
	
	
	@Override
	public boolean hasHessian() {
		return false;
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
	
}
