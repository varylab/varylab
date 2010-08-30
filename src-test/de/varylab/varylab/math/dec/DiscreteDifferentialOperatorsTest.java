package de.varylab.varylab.math.dec;

import junit.framework.Assert;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Matrix.Norm;
import no.uib.cipr.matrix.sparse.CompDiagMatrix;

import org.junit.Test;

import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.functional.FunctionalTestData;
import de.jtem.halfedgetools.jreality.adapter.JRPositionAdapter;
import de.jtem.halfedgetools.jreality.node.DefaultJRHDS;
import de.jtem.halfedgetools.jreality.node.DefaultJRVertex;
import de.jtem.halfedgetools.util.BoundaryOperators;

public class DiscreteDifferentialOperatorsTest {

	DefaultJRHDS hds = new DefaultJRHDS();

	@Test
	public void testHodgeStar() {
		DefaultJRVertex 
		v1 = hds.addNewVertex(),
		v2 = hds.addNewVertex(),
		v3 = hds.addNewVertex();

		HalfEdgeUtils.constructFaceByVertices(hds, v1,v2,v3);
		v1.position = new double[] {0.0,0.0,0.0};
		v2.position = new double[] {1.0,0.0,0.0};
		v3.position = new double[] {0.0,1.0,0.0};
		Matrix 
			s0 = new CompDiagMatrix(3,3),
			s1 = new CompDiagMatrix(3,3),
			s2 = new CompDiagMatrix(1,1);
		s0.set(0,0,.25);
		s0.set(1,1,.125);
		s0.set(2,2,.125);
		
		s1.set(0,0,.5);
		s1.set(1,1,0.0);
		s1.set(2,2,.5);
		
		s2.set(0,0,2);
		
		DiscreteDifferentialOperators ddOp = new DiscreteDifferentialOperators(hds);
		
		AdapterSet as = new AdapterSet();
		as.add(new JRPositionAdapter());
		s0.add(-1.0,ddOp.getHodgeStar(0, as));
		s1.add(-1.0,ddOp.getHodgeStar(1, as));
		s2.add(-1.0,ddOp.getHodgeStar(2, as));
		Assert.assertEquals(s0.norm(Norm.Maxvalue), 0.0 , 1E-6);
		Assert.assertEquals(s1.norm(Norm.Maxvalue), 0.0 , 1E-6);
		Assert.assertEquals(s2.norm(Norm.Maxvalue), 0.0 , 1E-6);
		
	}

	@Test
	public void testTetrahedron() {
		FunctionalTestData.createCombinatorialTetrahedron(hds);
		BoundaryOperators bdOp = new BoundaryOperators(hds);
		Matrix 
			d0 = bdOp.getOperatorMatrix(0),
			d1 = bdOp.getOperatorMatrix(1);
		Matrix product = new DenseMatrix(hds.numVertices(),hds.numFaces());
		d0.mult(d1, product);
		Assert.assertEquals(product.norm(Matrix.Norm.Maxvalue),0.0);
	}

	@Test
	public void testBiPyrWithBd() {
		DefaultJRHDS hds = new DefaultJRHDS();
		
		FunctionalTestData.createCombinatorialPyrWithBnd(hds);
		BoundaryOperators bdOp = new BoundaryOperators(hds);
		Matrix 
			d0 = bdOp.getOperatorMatrix(0),
			d1 = bdOp.getOperatorMatrix(1);
		Matrix product = new DenseMatrix(hds.numVertices(),hds.numFaces());
		d0.mult(d1, product);
		Assert.assertEquals(product.norm(Matrix.Norm.Maxvalue),0.0);
	}
}
