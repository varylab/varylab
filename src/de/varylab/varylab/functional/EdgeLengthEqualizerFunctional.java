package de.varylab.varylab.functional;

import static de.jtem.halfedge.util.HalfEdgeUtils.incomingEdges;
import static de.jtem.halfedgetools.functional.FunctionalUtils.addVectorToGradient;
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

public class EdgeLengthEqualizerFunctional <
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
		for (V v : hds.getVertices()) {
			getPosition(v, x, pv);
			for (E e : incomingEdges(v)) {
				V vi = e.getStartVertex();
				V vim = e.getOppositeEdge().getPreviousEdge().getStartVertex();
				V vip = e.getNextEdge().getTargetVertex();
				getPosition(vi, x, pvi);
				getPosition(vip, x, pvip);
				double li = Rn.euclideanDistanceSquared(pv, pvi);
				double lip = Rn.euclideanDistanceSquared(pv, pvip);
				double difp = li - lip;
				if (E != null) E.add(difp * difp);
				if (G != null) {
					getPosition(vim, x, pvim);
					double lim = Rn.euclideanDistanceSquared(pv, pvim);
					Rn.subtract(veci, pv, pvi);
					Rn.subtract(vecip, pv, pvip);
					Rn.subtract(tmp, veci, vecip);
					Rn.times(tmp, 4 * difp, tmp);
					addVectorToGradient(G, 3*v.getIndex(), tmp);
					Rn.times(tmp, 4, veci);
					Rn.times(tmp, lip - 2*li + lim, tmp);
					addVectorToGradient(G, 3*vi.getIndex(), tmp);
				}
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

	@Override
	public boolean hasGradient() {
		return true;
	}
}
