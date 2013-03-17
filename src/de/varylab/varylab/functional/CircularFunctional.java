package de.varylab.varylab.functional;

import java.util.ArrayList;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.FunctionalUtils;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.varylab.varylab.halfedge.adapter.VertexDomainValueAdapter;

public class CircularFunctional<
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>> 
	implements Functional<V, E, F> {

	@Override
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>>
	void evaluate(HDS hds, DomainValue x, Energy E, Gradient G, Hessian H) {
		if (E != null) {
			E.set(evaluate(hds, x));
		}
		if (G != null) {
			evaluateGradient(hds, x, G);
		}
	}

	// Calculate the energy of a given configuration
	public double evaluate(HalfEdgeDataStructure<V, E, F> hds, DomainValue x) {
		double result = 0.0;
		
		for (F f : hds.getFaces()) {
			result += circularEnergy(f,new AdapterSet(new VertexDomainValueAdapter(x)));
		}
		return result;
	}

	public double circularEnergy(F f, AdapterSet a) {
		
		double result = 0.0;
		double[]
		       v1 = new double[3], 
		       v2 = new double[3],
		       v3 = new double[3],
		       v4 = new double[3];
		ArrayList<V> bdVerts = new ArrayList<V>(HalfEdgeUtils.boundaryVertices(f));
		if(bdVerts.size() != 4) {
			return result;
		} else {
			v1 = a.get(Position.class, bdVerts.get(0), double[].class);
			v2 = a.get(Position.class, bdVerts.get(1), double[].class);
			v3 = a.get(Position.class, bdVerts.get(2), double[].class);
			v4 = a.get(Position.class, bdVerts.get(3), double[].class);
			double 
				alpha1 = FunctionalUtils.angle(v4, v1, v2),
				alpha2 = FunctionalUtils.angle(v1, v2, v3),
				alpha3 = FunctionalUtils.angle(v2, v3, v4),
				alpha4 = FunctionalUtils.angle(v3, v4, v1);
			result += (Math.PI-alpha1-alpha3)*(Math.PI-alpha1-alpha3);
			result += (Math.PI-alpha2-alpha4)*(Math.PI-alpha2-alpha4);
		}
		return result;
	}

	public void evaluateGradient(
		//input
			HalfEdgeDataStructure<V, E, F> hds,
			DomainValue x,
		//output
			Gradient G
	) {
		double[] 
		       ds = new double[3],
		       dv = new double[3],
		       dt = new double[3];
		G.setZero();
		double[]
		       v1 = new double[3], 
		       v2 = new double[3],
		       v3 = new double[3],
		       v4 = new double[3];
		for (F f : hds.getFaces()) {
			ArrayList<V> bdVerts = new ArrayList<V>(HalfEdgeUtils.boundaryVertices(f));
			if(bdVerts.size() != 4) {
				continue;
			} else {
				FunctionalUtils.getPosition(bdVerts.get(0),x,v1);
				FunctionalUtils.getPosition(bdVerts.get(1),x,v2);
				FunctionalUtils.getPosition(bdVerts.get(2),x,v3);
				FunctionalUtils.getPosition(bdVerts.get(3),x,v4);
				double 
					alpha1 = FunctionalUtils.angle(v4, v1, v2),
					alpha2 = FunctionalUtils.angle(v1, v2, v3),
					alpha3 = FunctionalUtils.angle(v2, v3, v4),
					alpha4 = FunctionalUtils.angle(v3, v4, v1);
				double scale = -2.0*(Math.PI-alpha1-alpha3);
				FunctionalUtils.angleGradient(v4, v1, v2, ds, dv, dt);
				FunctionalUtils.addVectorToGradient(G, 3*bdVerts.get(3).getIndex(), Rn.times(null,scale,ds));
				FunctionalUtils.addVectorToGradient(G, 3*bdVerts.get(0).getIndex(), Rn.times(null,scale,dv));
				FunctionalUtils.addVectorToGradient(G, 3*bdVerts.get(1).getIndex(), Rn.times(null,scale,dt));
				
				FunctionalUtils.angleGradient(v2, v3, v4, ds, dv, dt);
				FunctionalUtils.addVectorToGradient(G, 3*bdVerts.get(1).getIndex(), Rn.times(null,scale,ds));
				FunctionalUtils.addVectorToGradient(G, 3*bdVerts.get(2).getIndex(), Rn.times(null,scale,dv));
				FunctionalUtils.addVectorToGradient(G, 3*bdVerts.get(3).getIndex(), Rn.times(null,scale,dt));
				
				scale = -2.0*(Math.PI-alpha2-alpha4);
				FunctionalUtils.angleGradient(v1, v2, v3, ds, dv, dt);
				FunctionalUtils.addVectorToGradient(G, 3*bdVerts.get(0).getIndex(), Rn.times(null,scale,ds));
				FunctionalUtils.addVectorToGradient(G, 3*bdVerts.get(1).getIndex(), Rn.times(null,scale,dv));
				FunctionalUtils.addVectorToGradient(G, 3*bdVerts.get(2).getIndex(), Rn.times(null,scale,dt));
				
				FunctionalUtils.angleGradient(v3, v4, v1, ds, dv, dt);
				FunctionalUtils.addVectorToGradient(G, 3*bdVerts.get(2).getIndex(), Rn.times(null,scale,ds));
				FunctionalUtils.addVectorToGradient(G, 3*bdVerts.get(3).getIndex(), Rn.times(null,scale,dv));
				FunctionalUtils.addVectorToGradient(G, 3*bdVerts.get(0).getIndex(), Rn.times(null,scale,dt));
				
			}
		}
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int getDimension(HDS hds) {
		return 3*hds.numVertices();
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int[][] getNonZeroPattern(
			HDS hds) {
		return null;
	}

	@Override
	public boolean hasHessian() {
		return false;
	}


}
