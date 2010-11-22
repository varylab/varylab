package de.varylab.varylab.math.dec;

import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import de.jreality.math.Rn;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.CircumCenterAdapter;
import de.jtem.halfedgetools.adapter.type.CircumCenter;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.functional.FunctionalTestData;
import de.jtem.halfedgetools.jreality.adapter.JRPositionAdapter;
import de.jtem.halfedgetools.jreality.node.DefaultJRFace;
import de.jtem.halfedgetools.jreality.node.DefaultJRHDS;
import de.jtem.halfedgetools.jreality.node.DefaultJRVertex;


public class CircumcenterAdapterTest {

	@Test
	public void testGetF() {
		DefaultJRHDS hds = new DefaultJRHDS();
		FunctionalTestData.createCombinatorialTriangle(hds);
		
		Random random = new Random();
		for (DefaultJRVertex v : hds.getVertices()) {
			v.position = new double[] {random.nextDouble(), random.nextDouble(), random.nextDouble()};
		}
//		DefaultJRVertex 
//			v1 = hds.addNewVertex(),
//			v2 = hds.addNewVertex(),
//			v3 = hds.addNewVertex();
//	
//		HalfEdgeUtils.constructFaceByVertices(hds, v1,v2,v3);
//		v1.position = new double[] {0.0,0.0,0.0};
//		v2.position = new double[] {1.0,0.0,0.0};
//		v3.position = new double[] {0.0,1.0,0.0};
		
		AdapterSet as = new AdapterSet();
		as.add(new JRPositionAdapter());
		as.add(new CircumCenterAdapter());
		for(DefaultJRFace f :  hds.getFaces()) {
			double[] ccc = as.get(CircumCenter.class, f, double[].class);
			List<DefaultJRVertex> bdVerts = HalfEdgeUtils.boundaryVertices(f);
			double radius = Rn.euclideanDistanceSquared(ccc, as.get(Position.class, bdVerts.get(0), double[].class));
			for(int i = 1; i < 3; ++i) {
				double[] vpos = as.get(Position.class, bdVerts.get(i), double[].class);

				double actRadius = Rn.euclideanDistanceSquared(ccc, vpos);
				Assert.assertEquals(radius,actRadius,1E-6);
			}
		}
	}
}
