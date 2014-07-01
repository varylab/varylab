package de.varylab.varylab.math.functional;

import java.util.Random;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

import org.junit.Ignore;
import org.junit.Test;

import de.jtem.halfedgetools.functional.FunctionalTest;
import de.jtem.halfedgetools.functional.FunctionalTestData;
import de.jtem.halfedgetools.functional.MyDomainValue;
import de.jtem.halfedgetools.jreality.node.DefaultJREdge;
import de.jtem.halfedgetools.jreality.node.DefaultJRFace;
import de.jtem.halfedgetools.jreality.node.DefaultJRHDS;
import de.jtem.halfedgetools.jreality.node.DefaultJRVertex;
import de.varylab.varylab.functional.DirichletEnergyFunctional;

public class DirichletEnergyFunctionalTest extends FunctionalTest<DefaultJRVertex, DefaultJREdge, DefaultJRFace> {

	@SuppressWarnings("unused")
	@Override
	public void init() {
		DefaultJRHDS hds = new DefaultJRHDS();
	
		FunctionalTestData.createCombinatorialPyrWithBnd(hds);
		Random random = new Random(123);
		double[][] positions = new double[][]{
				{1.5,1.5,0},{-1,-1,0},{-1,1,0},{1,-1,0},{0,0,0}	
		};
		int i = 0;
		for (DefaultJRVertex v : hds.getVertices()) {
//			v.position = positions[i++]; 
			v.position = new double[] {random.nextDouble(), random.nextDouble(), 0.0};
		}
		
		Vector result = new DenseVector(hds.numVertices() * 3);
		for (DefaultJRVertex v : hds.getVertices()) {
			result.set(v.getIndex() * 3 + 0, v.position[0]);
			result.set(v.getIndex() * 3 + 1, v.position[1]);
			result.set(v.getIndex() * 3 + 2, v.position[2]);
		}	
		MyDomainValue pos = new MyDomainValue(result);
		
		setXGradient(pos);
		setHDS(hds);
		setFunctional(new DirichletEnergyFunctional<DefaultJRVertex, DefaultJREdge, DefaultJRFace>());		
	}
	
	
	@Override
	@Test
	public void testGradient() throws Exception {
		init();
		super.testGradient();
	}
	
	@Override
	@Test
	@Ignore
	public void testHessian() throws Exception {
		super.testHessian();
	}

}
