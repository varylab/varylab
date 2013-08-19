package de.varylab.varylab.functional;

import static de.jtem.halfedgetools.functional.FunctionalUtils.getPosition;
import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;

public class ForceConeFunctional <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> implements Functional<V, E, F> {

	double[] 
		pv = new double[3],
		pvi = new double[3],
		pvim = new double[3],
		pvip = new double[3],
		veci = new double[3],
		vecip = new double[3],
		tmp = new double[3];
	
	@Override
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void evaluate(HDS hds,
		DomainValue x, 
		Energy E, 
		Gradient G, 
		Hessian H
	) {
		for (E e : hds.getPositiveEdges()) {
			V v = e.getStartVertex();
			V vi = e.getTargetVertex();
			getPosition(v, x, pv);
			getPosition(vi, x, pvi);
			Rn.subtract(veci, pv, pvi);
			if (E != null) {
				E.add(veci[0]*veci[0] + veci[1]*veci[1] - veci[2]*veci[2]);
			}
			if (G != null) {
				G.add(3*v.getIndex(), 2*veci[0]);
				G.add(3*v.getIndex()+1, 2*veci[1]);
				G.add(3*v.getIndex()+2, -2*veci[2]);
				
				G.add(3*vi.getIndex(), -2*veci[0]);
				G.add(3*vi.getIndex()+1, -2*veci[1]);
				G.add(3*vi.getIndex()+2, 2*veci[2]);
			}
		}
	}
	
	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int getDimension(HDS hds) {
		return hds.numVertices() * 3;
	}

	@Override
	public <HDS extends de.jtem.halfedge.HalfEdgeDataStructure<V,E,F>> int[][] getNonZeroPattern(HDS hds) {
		return null;
	};

	@Override
    public boolean hasHessian() {
    	return false;
    }
	
}
