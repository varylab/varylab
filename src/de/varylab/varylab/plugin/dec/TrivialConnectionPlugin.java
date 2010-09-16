package de.varylab.varylab.plugin.dec;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.CalculatorException;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.varylab.varylab.hds.adapter.GaussianCurvatureAdapter;
import de.varylab.varylab.math.dec.Connection;
import de.varylab.varylab.math.dec.ConnectionAdapter;
import de.varylab.varylab.math.dec.DiscreteDifferentialOperators;
import de.varylab.varylab.math.dec.Singularity;
import de.varylab.varylab.math.dec.VectorFieldAdapter;

public class TrivialConnectionPlugin extends AlgorithmPlugin {

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.VectorField;
	}

	@Override
	public String getAlgorithmName() {
		return "Trivial connection";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, CalculatorSet c, HalfedgeInterface hcp) throws CalculatorException 
	{
		DiscreteDifferentialOperators<V,E,F,HDS> dop = new DiscreteDifferentialOperators<V,E,F,HDS>(hds, hcp.getAdapters());
		ConnectionAdapter trivialConnectionAdapter = dop.getTrivialConnectionAdapter();
		VectorFieldAdapter vfAdapter = dop.getTrivialConnectionVectorField();
		hcp.addLayerAdapter(trivialConnectionAdapter,false);
		hcp.addLayerAdapter(vfAdapter,false);
		if(!checkCurvature(hds,hcp.getAdapters())) {
			System.out.println("Connection does not match curvature!");
		}
	}

	private <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> boolean checkCurvature(HDS hds, AdapterSet adapters) {
		boolean ok = true;
		for(V v: hds.getVertices()) {
			ok &= checkVertex(v,adapters); 
		}
		return ok;
	}
	
	private <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F> 
	> boolean checkVertex(V v, AdapterSet adapters) {
		GaussianCurvatureAdapter gca = new GaussianCurvatureAdapter();
		E e = v.getIncomingEdge();
		double curvature = 0.0;
		do {
			curvature += adapters.get(Connection.class, e, Double.class);
			e = e.getNextEdge().getOppositeEdge();
		} while(e != v.getIncomingEdge());
		double singCurvature = adapters.get(Singularity.class, v, Double.class);
		double gaussCurvature = gca.getV(v, adapters);
		boolean vOK = (Math.abs(curvature + gaussCurvature - singCurvature*2*Math.PI ) <= 1E-6);
		if(!vOK) {
			System.out.println(v +":"+curvature +"!="+singCurvature +"+"+gaussCurvature);
		}
		return vOK;
	}

}
