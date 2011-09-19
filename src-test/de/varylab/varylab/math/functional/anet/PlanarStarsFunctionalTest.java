package de.varylab.varylab.math.functional.anet;

import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.functional.FunctionalTest;
import de.jtem.halfedgetools.functional.MyDomainValue;
import de.jtem.halfedgetools.jreality.node.DefaultJREdge;
import de.jtem.halfedgetools.jreality.node.DefaultJRFace;
import de.jtem.halfedgetools.jreality.node.DefaultJRHDS;
import de.jtem.halfedgetools.jreality.node.DefaultJRVertex;
import de.varylab.varylab.math.functional.PlanarStarFunctional;

public class PlanarStarsFunctionalTest extends
						FunctionalTest<DefaultJRVertex, DefaultJREdge, DefaultJRFace> {

	@Override
	public void init(){
		DefaultJRHDS hds = new DefaultJRHDS();
		HalfEdgeUtils.addOctahedron(hds);
		Random random = new Random(123);
		for (DefaultJRVertex v : hds.getVertices()) {
			v.position = new double[]{random.nextDouble(), random.nextDouble(), random.nextDouble()};
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
		setFunctional(new PlanarStarFunctional<DefaultJRVertex, DefaultJREdge, DefaultJRFace>(1.0));
	}
	
	@Test@Override@Ignore
	public void testHessian() throws Exception {
		super.testHessian();
	}
	
}
