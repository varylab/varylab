package de.varylab.varylab.functional;

import static de.jtem.halfedgetools.functional.FunctionalUtils.getPosition;
import de.jreality.math.Rn;
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

public class ForceConeFunctional <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> implements Functional<V, E, F> {

	double[] 
		pv = new double[3],
		pvi = new double[3],
		pv1 = new double[3],
		pvi1 = new double[3],
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
		double fac;
		for (E e : hds.getPositiveEdges()) {
			V v = e.getStartVertex();
			V vi = e.getTargetVertex();
			getPosition(v, x, pv);
			getPosition(vi, x, pvi);
			Rn.subtract(veci, pv, pvi);
			if (E != null) {
				E.add(Math.pow(veci[0]*veci[0] + veci[1]*veci[1] - veci[2]*veci[2],2));
			}
			if (G != null) {
				fac = 2*(veci[0]*veci[0] + veci[1]*veci[1] - veci[2]*veci[2]);
				G.add(3*v.getIndex(), 2*veci[0]*fac);
				G.add(3*v.getIndex()+1, 2*veci[1]*fac);
				G.add(3*v.getIndex()+2, -2*veci[2]*fac);
				
				G.add(3*vi.getIndex(), -2*veci[0]*fac);
				G.add(3*vi.getIndex()+1, -2*veci[1]*fac);
				G.add(3*vi.getIndex()+2, 2*veci[2]*fac);
			}
		}
		
		double ip;
		//Special case of valence 6 edges (?)
		double phi = 2*Math.PI*Math.sin(Math.PI/4)/3;
		double denom;
		for (V v : hds.getVertices()) {
			for (E e1:HalfEdgeUtils.incomingEdges(v)){
				for (E e2:HalfEdgeUtils.incomingEdges(v)){
					if(e1==e2)
						continue;
					
					V v1 = e1.getStartVertex();
					V v2 = e2.getStartVertex();
					
					getPosition(v1, x, pvi1);
					getPosition(v2, x, pvi);
					getPosition(v, x,  pv);
					Rn.subtract(veci, pv, pvi);
					Rn.subtract(vecip, pv, pvi1);
					ip = Rn.innerProduct(veci, vecip);
					denom = Rn.euclideanNorm(veci)*Rn.euclideanNorm(vecip);
					if (E != null) {			
						E.add( Math.pow(Math.cos(phi) - (ip/denom),2));
					}
					
					if (G != null) {
						fac = 2*(Math.cos(phi) - (ip/(Rn.euclideanNorm(veci)*Rn.euclideanNorm(vecip))));
						G.add(3*v.getIndex(), fac * ( (-veci[0] -vecip[0])/denom  + ip*(veci[0]/(denom*Rn.euclideanNormSquared(veci)) + veci[0]/(denom*Rn.euclideanNormSquared(veci)))  ) );
						G.add(3*v.getIndex()+1, fac * ( (-veci[1] -vecip[1])/denom  + ip*(veci[1]/(denom*Rn.euclideanNormSquared(veci)) + veci[1]/(denom*Rn.euclideanNormSquared(veci)))  ) );
						G.add(3*v.getIndex()+2, fac * ( (-veci[2] -vecip[2])/denom  + ip*(veci[2]/(denom*Rn.euclideanNormSquared(veci)) + veci[2]/(denom*Rn.euclideanNormSquared(veci)))  ) );
						
						G.add(3*v1.getIndex(), fac * ( (veci[0])/denom  - ip*(vecip[0]/(denom*Rn.euclideanNormSquared(vecip)) )  ) );
						G.add(3*v1.getIndex()+1, fac * ( (veci[1])/denom  - ip*(vecip[1]/(denom*Rn.euclideanNormSquared(vecip)) )  ));
						G.add(3*v1.getIndex()+2,fac * ( (veci[2])/denom  - ip*(vecip[2]/(denom*Rn.euclideanNormSquared(vecip)) )  ));
						
						G.add(3*v2.getIndex(), fac * ( (vecip[0])/denom  - ip*(veci[0]/(denom*Rn.euclideanNormSquared(veci)) )  ) );
						G.add(3*v2.getIndex()+1,fac * ( (vecip[1])/denom  - ip*(veci[1]/(denom*Rn.euclideanNormSquared(veci)) )  ) );
						G.add(3*v2.getIndex()+2, fac * ( (vecip[2])/denom  - ip*(veci[2]/(denom*Rn.euclideanNormSquared(veci)) )  ) );
					}
					
					
				}
			}
		}
		System.out.println(E);
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
