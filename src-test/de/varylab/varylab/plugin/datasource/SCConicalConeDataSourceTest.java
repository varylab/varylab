package de.varylab.varylab.plugin.datasource;

import org.junit.Assert;
import org.junit.Test;

import de.jreality.math.Pn;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.halfedge.adapter.VPositionAdapter;
import de.varylab.varylab.utilities.MathUtility;
import de.varylab.varylab.utilities.TestUtility;

public class SCConicalConeDataSourceTest {

	@Test
	public void testCalculateDiagonalIntersection() throws Exception {
		VHDS g = TestUtility.readOBJ(getClass(), "sc-minimal-test02.obj");
		VVertex v0 = g.getVertex(0);
		AdapterSet a = AdapterSet.createGenericAdapters();
		a.add(new VPositionAdapter());
		for (VFace f : HalfEdgeUtilsExtra.getFaceStar(v0)) {
			double[] di = MathUtility.calculateDiagonalIntersection(f, a);
			double dist =Pn.distanceBetween(di, v0.getP(), Pn.EUCLIDEAN);
			Assert.assertEquals(0.0276330690702, dist, 1E-10);
		}
	}
	
}
