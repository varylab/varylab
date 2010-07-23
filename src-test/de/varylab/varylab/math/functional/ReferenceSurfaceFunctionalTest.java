package de.varylab.varylab.math.functional;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

import org.junit.Test;

import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.generic.NormalAdapter;
import de.jtem.halfedgetools.functional.FunctionalTest;
import de.jtem.halfedgetools.functional.MyDomainValue;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.hds.adapter.VPositionAdapter;

public class ReferenceSurfaceFunctionalTest extends FunctionalTest<VVertex, VEdge, VFace> {

	@Override
	public void init() {
		VHDS refSurface = new VHDS();
	
		VVertex 
			v1 = refSurface.addNewVertex(),
			v2 = refSurface.addNewVertex(),
			v3 = refSurface.addNewVertex();
		
		HalfEdgeUtils.constructFaceByVertices(refSurface, v1,v2,v3);
		v1.position = new double[] {0.0,0.0,0.0};
		v2.position = new double[] {1.0,0.0,0.0};
		v3.position = new double[] {0.0,1.0,0.0};
		
		VHDS hds = new VHDS();
		VVertex vh = hds.addNewVertex();
		vh.position = new double[] {Math.random(), Math.random(), 1.0};

		Vector result = new DenseVector(hds.numVertices() * 3);
		for (VVertex v : hds.getVertices()) {
			result.set(v.getIndex() * 3 + 0, v.position[0]);
			result.set(v.getIndex() * 3 + 1, v.position[1]);
			result.set(v.getIndex() * 3 + 2, v.position[2]);
		}
		
		MyDomainValue pos = new MyDomainValue(result);
		
		setXGradient(pos);
//		setXHessian(pos);
		setHDS(hds);
		ReferenceSurfaceFunctional<VVertex, VEdge, VFace> functional =
			new ReferenceSurfaceFunctional<VVertex, VEdge, VFace>();
		AdapterSet as = new AdapterSet(new VPositionAdapter(), new NormalAdapter());
		functional.setReferenceSurface(refSurface, as);
		setFuctional(functional);
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
