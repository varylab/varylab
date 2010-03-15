package de.varylab.varylab.math.functional.formfinding;

import java.util.Random;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

import org.junit.Test;

import de.jtem.halfedgetools.functional.FunctionalTest;
import de.jtem.halfedgetools.functional.FunctionalTestData;
import de.jtem.halfedgetools.functional.MyDomainValue;
import de.jtem.halfedgetools.jreality.node.DefaultJREdge;
import de.jtem.halfedgetools.jreality.node.DefaultJRFace;
import de.jtem.halfedgetools.jreality.node.DefaultJRHDS;
import de.jtem.halfedgetools.jreality.node.DefaultJRVertex;
import de.varylab.varylab.math.functional.SpringFunctional;
import de.varylab.varylab.math.functional.edgelength.ConstantWeightAdapter;
import de.varylab.varylab.math.functional.edgelength.LengthAdapter;

public class SpringFunctionalTest extends FunctionalTest<DefaultJRVertex, DefaultJREdge, DefaultJRFace> {

	@Override
	public void init() {
		DefaultJRHDS hds = new DefaultJRHDS();
	
		FunctionalTestData.createCombinatorialTetrahedron(hds);
		Random random = new Random(123);
		for (DefaultJRVertex v : hds.getVertices()) {
			v.position = new double[] {random.nextDouble(), random.nextDouble(), random.nextDouble()};
		}
		
		Vector result = new DenseVector(hds.numVertices() * 3);
		for (DefaultJRVertex v : hds.getVertices()) {
			result.set(v.getIndex() * 3 + 0, v.position[0]);
			result.set(v.getIndex() * 3 + 1, v.position[1]);
			result.set(v.getIndex() * 3 + 2, v.position[2]);
		}	
		MyDomainValue pos = new MyDomainValue(result);
		
		setXGradient(pos);
		setXHessian(pos);
		setHDS(hds);
		LengthAdapter la = new LengthAdapter(0.0);
		setFuctional(new SpringFunctional<DefaultJRVertex, DefaultJREdge, DefaultJRFace>(
			la, new ConstantWeightAdapter(1))
		);
	}
	
	@Override
	@Test
	public void testGradient() throws Exception {
		super.testGradient();
	}
	
	@Override
	@Test
	public void testHessian() throws Exception {
		super.testHessian();
	}
	
	

}
