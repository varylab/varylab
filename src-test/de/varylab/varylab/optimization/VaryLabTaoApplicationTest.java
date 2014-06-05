package de.varylab.varylab.optimization;

import static java.lang.Math.PI;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import de.jreality.util.NativePathUtility;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedgetools.functional.AbstractFunctional;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.jtem.jpetsc.Mat;
import de.jtem.jpetsc.Vec;
import de.jtem.jtao.Tao;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;

public class VaryLabTaoApplicationTest {

	static {
		NativePathUtility.set("native");
		Tao.Initialize();
	}
	
	public class FDTestFunctional extends AbstractFunctional<VVertex, VEdge, VFace> {
		@Override
		public <
			HDS extends HalfEdgeDataStructure<VVertex, VEdge, VFace>
		> void evaluate(HDS hds, DomainValue x, Energy E, Gradient G, Hessian H) {
			E.setZero();
			for (VVertex v : hds.getVertices()) {
				for (int i = 0; i < 3; i++) {
					double xv = x.get(v.getIndex()*3 + i);
					E.add((xv - PI) * (xv - PI));	
				}
			}
		}
		
		@Override
		public <
			HDS extends HalfEdgeDataStructure<VVertex, VEdge, VFace>
		> int getDimension(HDS hds) {
			return hds.numVertices() * 3;
		}
		
	}
	
	
	@Test
	public void testFDGradient() throws Exception {
		FDTestFunctional f = new FDTestFunctional();
		List<Functional<VVertex, VEdge, VFace>> fList = new LinkedList<>();
		Map<Functional<?, ?, ?>, Double> cMap = new HashMap<>();
		fList.add(f);
		cMap.put(f, 1.0);
		VaryLabFunctional vf = new VaryLabFunctional(fList, cMap, 2);
		VHDS hds = new VHDS();
		hds.addNewVertex();
		hds.addNewVertex();
		VaryLabTaoApplication vta = new VaryLabTaoApplication(hds, vf);
		Vec x = new Vec(vta.getDomainDimension());
		x.zeroEntries();
		Vec g = new Vec(vta.getDomainDimension());
		vta.evaluateObjectiveAndGradient(x, g);
		for (int i = 0; i < vta.getDomainDimension(); i++) {
			Assert.assertEquals(-2*PI, g.getValue(i), 1E-10);
		}
	}
	
	@Test
	public void testFDHessian() throws Exception {
		FDTestFunctional f = new FDTestFunctional();
		List<Functional<VVertex, VEdge, VFace>> fList = new LinkedList<>();
		Map<Functional<?, ?, ?>, Double> cMap = new HashMap<>();
		fList.add(f);
		cMap.put(f, 1.0);
		VaryLabFunctional vf = new VaryLabFunctional(fList, cMap, 2);
		VHDS hds = new VHDS();
		hds.addNewVertex();
		hds.addNewVertex();
		VaryLabTaoApplication vta = new VaryLabTaoApplication(hds, vf);
		Vec x = new Vec(vta.getDomainDimension());
		x.zeroEntries();
		Mat h = new Mat(vta.getDomainDimension(), vta.getDomainDimension());
		vta.evaluateHessian(x, h, h);
		for (int i = 0; i < vta.getDomainDimension(); i++) {
			for (int j = 0; j < vta.getDomainDimension(); j++) {
				if (i == j) {
					Assert.assertEquals(2.0, h.getValue(i, j), 1E-9);
				} else {
					Assert.assertEquals(0.0, h.getValue(i, j), 1E-9);
				}
			}
		}
	}
	
}
