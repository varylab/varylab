package de.varylab.varylab.math.functional;

import java.util.Random;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

import org.junit.Ignore;
import org.junit.Test;

import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.functional.FunctionalTest;
import de.jtem.halfedgetools.functional.MyDomainValue;
import de.varylab.varylab.functional.TouchingIncirclesFunctional;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;

public class TouchingIncirclesFunctionalTest extends FunctionalTest<VVertex, VEdge, VFace> {

	private Random
		rnd = new Random(0);
	
	@Override
	public void init() {
		VHDS hds = new VHDS();
	
		VVertex 
			v1 = hds.addNewVertex(),
			v2 = hds.addNewVertex(),
			v3 = hds.addNewVertex(),
			v4 = hds.addNewVertex(),
			v5 = hds.addNewVertex(),
			v6 = hds.addNewVertex();
		
		HalfEdgeUtils.constructFaceByVertices(hds, v1,v2,v4,v3);
		HalfEdgeUtils.constructFaceByVertices(hds, v5,v6,v2,v1);
		v1.setP(new double[] {0.0,0.0,rnd.nextDouble(),1.0});
		v2.setP(new double[] {1.0,0.25,0.0,1.0});
		v3.setP(new double[] {0.0,1.0,0.0,1.0});
		v4.setP(new double[] {1.0,1.0,0.0,1.0});
		v5.setP(new double[] {0.0,-1.0,0.0,1.0});
		v6.setP(new double[] {1.0,-1.0,rnd.nextDouble(),1.0});
		
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
		setFunctional(new TouchingIncirclesFunctional());
	}
	
	@Test@Override@Ignore
	public void testHessian() throws Exception {
		super.testHessian();
	}
	
}
