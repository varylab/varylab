package de.varylab.varylab.math.functional;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

import org.junit.Ignore;
import org.junit.Test;

import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.functional.FunctionalTest;
import de.jtem.halfedgetools.functional.MyDomainValue;
import de.varylab.varylab.functional.CircularFunctional;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;

public class CircularFunctionalTest extends FunctionalTest<VVertex, VEdge, VFace> {

	public CircularFunctional<VVertex, VEdge, VFace> 
		functional = new CircularFunctional<VVertex, VEdge, VFace>();
	
	@Override
	public void init() {
		VHDS hds = new VHDS();
	
		VVertex 
			v1 = hds.addNewVertex(),
			v2 = hds.addNewVertex(),
			v3 = hds.addNewVertex(),
			v4 = hds.addNewVertex();
		
		HalfEdgeUtils.constructFaceByVertices(hds, v1,v2,v3,v4);
		v1.P = new double[] {0.0,0.0,0.0};
		v2.P = new double[] {1.0,0.0,0.0};
		v3.P = new double[] {0.0,1.0,0.0};
		v4.P = new double[] {Math.random(), Math.random(), 0.0};

		Vector result = new DenseVector(hds.numVertices() * 3);
		for (VVertex v : hds.getVertices()) {
			result.set(v.getIndex() * 3 + 0, v.P[0]);
			result.set(v.getIndex() * 3 + 1, v.P[1]);
			result.set(v.getIndex() * 3 + 2, v.P[2]);
		}
		
		MyDomainValue pos = new MyDomainValue(result);
		setXGradient(pos);
		setHDS(hds);
		setFunctional(functional);
	}
	
	@Test@Override@Ignore
	public void testHessian() throws Exception {
		super.testHessian();
	}

}
