package de.varylab.varylab.math.functional;

import java.util.Random;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

import org.junit.Ignore;
import org.junit.Test;

import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.functional.FunctionalTest;
import de.jtem.halfedgetools.functional.MyDomainValue;
import de.varylab.varylab.functional.RegularNgonsFunctional;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;

public class RegularNgonsFunctionalTest extends FunctionalTest<VVertex, VEdge, VFace> {

	private Random
		rnd = new Random(0);
	
	@Override
	public void init() {
		VHDS hds = new VHDS();
	
		VVertex 
			v1 = hds.addNewVertex(),
			v2 = hds.addNewVertex(),
			v3 = hds.addNewVertex(),
			v4 = hds.addNewVertex();
		
		HalfEdgeUtils.constructFaceByVertices(hds, v1,v2,v4,v3);
		v1.setP(new double[] {0.0,0.0,0.0,0.0});
		v2.setP(new double[] {1.0,0.0,0.0,0.0});
		v3.setP(new double[] {0.0, 1.0, 0.0,0.0});
		v4.setP(new double[] {rnd.nextDouble(),rnd.nextDouble(),0.0,0.0});
				
		Vector result = new DenseVector(hds.numVertices() * 3);
		for (VVertex v : hds.getVertices()) {
			double[] P = v.getP();
			result.set(v.getIndex() * 3 + 0, P[0]);
			result.set(v.getIndex() * 3 + 1, P[1]);
			result.set(v.getIndex() * 3 + 2, P[2]);
		}
		MyDomainValue pos = new MyDomainValue(result);
		setXGradient(pos);
		setHDS(hds);
		RegularNgonsFunctional<VVertex, VEdge, VFace> functional = new RegularNgonsFunctional<>();
		functional.setSizes(4);
		setEps(1E-8);
		setFunctional(functional);
	}
	
	@Test@Override@Ignore
	public void testHessian() throws Exception {
		super.testHessian();
	}
	
}
