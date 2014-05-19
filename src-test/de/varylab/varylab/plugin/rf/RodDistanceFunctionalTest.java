package de.varylab.varylab.plugin.rf;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import de.jtem.halfedgetools.functional.FunctionalTest;
import de.jtem.halfedgetools.functional.MyDomainValue;
import de.varylab.varylab.functional.adapter.Length;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.halfedge.adapter.ConstantLengthAdapter;

public class RodDistanceFunctionalTest extends FunctionalTest<VVertex, VEdge, VFace> {

	@Override
	public void init() {
		Random rnd = new Random(0);
		VHDS hds = new VHDS();
	
		VVertex 
			v = hds.addNewVertex(),
			w = hds.addNewVertex();
		
		VEdge 
			e1 = hds.addNewEdge(),
			e2 = hds.addNewEdge();
		e1.setIsPositive(true);
		e1.linkOppositeEdge(e2);
		e1.linkNextEdge(e2);
		e2.linkNextEdge(e1);
		e1.setTargetVertex(v);
		e2.setTargetVertex(w);
		
		v.setP(new double[] {0.0, 0.0, 0.0, 1.0});
		w.setP(new double[] {1.0, 0.0, 0.0,1.0});

		VVertex 
			v2 = hds.addNewVertex(),
			w2 = hds.addNewVertex();
		
		VEdge 
			e12 = hds.addNewEdge(),
			e22 = hds.addNewEdge();
		e12.setIsPositive(true);
		e12.linkOppositeEdge(e22);
		e12.linkNextEdge(e22);
		e22.linkNextEdge(e12);
		e12.setTargetVertex(v2);
		e22.setTargetVertex(w2);
		
		v2.setP(new double[] {1.0, 1.0, 1.0, 1.0});
		w2.setP(new double[] {rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble()*1.0, 1.0});

		
		
		Map<VEdge, VEdge> nextRodMap = new HashMap<>();
		nextRodMap.put(e1, e12);
		nextRodMap.put(e12, e1);
		nextRodMap.put(e2, e22);
		nextRodMap.put(e22, e2);
		
		RodConnectivityAdapter rca = new RodConnectivityAdapter(nextRodMap);
		Length<VEdge> l = new ConstantLengthAdapter(0.5);
		
		Vector result = new DenseVector(hds.numVertices() * 3);
		for (VVertex vv : hds.getVertices()) {
			double[] p = vv.getP();
			result.set(vv.getIndex() * 3 + 0, p[0]/p[3]);
			result.set(vv.getIndex() * 3 + 1, p[1]/p[3]);
			result.set(vv.getIndex() * 3 + 2, p[2]/p[3]);
		}	
		MyDomainValue pos = new MyDomainValue(result);

		setXGradient(pos);
		setHDS(hds);
		setFunctional(new RodDistanceFunctional(rca,l));
	}

}
